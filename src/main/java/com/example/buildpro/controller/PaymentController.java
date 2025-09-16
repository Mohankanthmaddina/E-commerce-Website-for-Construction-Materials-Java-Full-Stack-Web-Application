package com.example.buildpro.controller;

import com.example.buildpro.model.Order;
import com.example.buildpro.service.PaymentService;
import com.example.buildpro.service.CartService;
import com.example.buildpro.service.UserService;
import com.example.buildpro.service.AddressService;
import com.example.buildpro.service.PaymentGatewayService;
import com.example.buildpro.model.User;
import com.example.buildpro.model.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @GetMapping("/checkout")
    public String checkoutPage(Model model, @RequestParam Long userId, @RequestParam(required = false) Long addressId) {
        Optional<User> userOpt = userService.findById(userId);
        
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        Cart cart = cartService.getCartByUser(user);
        
        if (cart == null || cart.getCartItems().isEmpty()) {
            return "redirect:/cart/view?userId=" + user.getId();
        }

        // Get selected address
        com.example.buildpro.model.Address selectedAddress = null;
        if (addressId != null) {
            selectedAddress = addressService.getUserAddresses(user).stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElse(null);
        }

        // Calculate totals
        double subtotal = cart.getCartItems().stream()
            .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
            .sum();
        
        double deliveryCharge = subtotal * 0.05;
        double discount = subtotal > 1000 ? deliveryCharge * 0.15 : 0;
        double total = subtotal + deliveryCharge - discount;

        model.addAttribute("cart", cart);
        model.addAttribute("selectedAddress", selectedAddress);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("deliveryCharge", deliveryCharge);
        model.addAttribute("discount", discount);
        model.addAttribute("total", total);
        model.addAttribute("userId", user.getId());
        model.addAttribute("addressId", addressId);

        return "payment";
    }

    // Directly place an order (no payment/verification), then redirect to success
    @GetMapping("/direct-place")
    public String placeOrderDirectly(@RequestParam Long userId, @RequestParam Long addressId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login?error=User not found";
        }

        User user = userOpt.get();
        Cart cart = cartService.getCartByUser(user);
        if (cart == null || cart.getCartItems().isEmpty()) {
            return "redirect:/cart/view?userId=" + user.getId() + "&error=Your cart is empty";
        }

        Order order = paymentService.processPayment(userId, addressId, "cod", "COD_" + System.currentTimeMillis());
        cartService.clearCart(user);

        return "redirect:/payment/success?orderId=" + order.getId();
    }
    

    @PostMapping("/process")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processPayment(
            @RequestParam Long userId,
            @RequestParam Long addressId,
            @RequestParam String paymentMethod) {
        
        try {
            // Get user details
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOpt.get();
            
            // Get cart to calculate amount
            Cart cart = cartService.getCartByUser(user);
            if (cart == null || cart.getCartItems().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Cart is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Calculate total amount
            double totalAmount = cart.getCartItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
            
            double deliveryCharge = totalAmount * 0.05;
            double discount = totalAmount > 1000 ? deliveryCharge * 0.15 : 0;
            double finalAmount = totalAmount + deliveryCharge - discount;

            // Generate order ID
            String orderId = "ORD_" + System.currentTimeMillis();
            
            Map<String, Object> response = new HashMap<>();
            
            // Handle different payment methods
            if ("phonepe".equalsIgnoreCase(paymentMethod) || "gpay".equalsIgnoreCase(paymentMethod) || "online".equalsIgnoreCase(paymentMethod)) {
                // Initiate online payment
                Map<String, Object> paymentResponse = paymentGatewayService.initiatePayment(
                    orderId, 
                    finalAmount, 
                    user.getId().toString(), 
                    "9999999999"
                );

                if ((Boolean) paymentResponse.get("success")) {
                    // Process the order
                    Order order = paymentService.processPayment(userId, addressId, paymentMethod, (String) paymentResponse.get("paymentId"));
                    
                    response.put("success", true);
                    response.put("orderId", order.getId());
                    response.put("orderNumber", order.getOrderNumber());
                    response.put("paymentId", paymentResponse.get("paymentId"));
                    response.put("transactionId", paymentResponse.get("transactionId"));
                    response.put("amount", finalAmount);
                    response.put("paymentUrl", paymentResponse.get("paymentUrl"));
                    response.put("paymentMethod", paymentMethod);
                    response.put("redirectUrl", "/payment/success?orderId=" + order.getId());
                    response.put("message", "Payment initiated successfully! Redirecting to payment gateway...");
                    
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Payment initiation failed: " + paymentResponse.get("message"));
                    return ResponseEntity.badRequest().body(response);
                }
            } else if ("cod".equalsIgnoreCase(paymentMethod)) {
                // Cash on Delivery
                Order order = paymentService.processPayment(userId, addressId, paymentMethod, "COD_" + orderId);
                
                // Clear the cart after successful order
                cartService.clearCart(user);
                
                response.put("success", true);
                response.put("orderId", order.getId());
                response.put("orderNumber", order.getOrderNumber());
                response.put("paymentMethod", "Cash on Delivery");
                response.put("redirectUrl", "/user/dashboard?userId=" + userId + "&orderId=" + order.getId() + "&success=Order placed successfully!");
                response.put("message", "Order placed successfully! You will pay on delivery.");
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid payment method");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Payment failed: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam Long orderId, Model model) {
        try {
            Order order = paymentService.getOrderById(orderId);
            
            // Calculate order totals for display
            double subtotal = order.getTotalAmount();
            double deliveryCharge = order.getDeliveryCharge();
            double discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0;
            double finalAmount = order.getFinalAmount();
            
            model.addAttribute("order", order);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("deliveryCharge", deliveryCharge);
            model.addAttribute("discount", discount);
            model.addAttribute("finalAmount", finalAmount);
            model.addAttribute("orderNumber", order.getOrderNumber());
            model.addAttribute("paymentMethod", order.getPaymentMethod());
            model.addAttribute("orderDate", order.getOrderDate());
            
            return "payment-success";
        } catch (Exception e) {
            model.addAttribute("error", "Order not found: " + e.getMessage());
            return "payment-error";
        }
    }

    @GetMapping("/cancel")
    public String paymentCancel() {
        return "redirect:/cart/view";
    }

    @GetMapping("/receiver-details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReceiverAccountDetails() {
        Map<String, Object> accountDetails = paymentGatewayService.getReceiverAccountDetails();
        return ResponseEntity.ok(accountDetails);
    }
}
