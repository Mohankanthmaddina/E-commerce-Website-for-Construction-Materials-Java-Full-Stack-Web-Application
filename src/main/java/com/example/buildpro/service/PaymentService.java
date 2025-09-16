package com.example.buildpro.service;

import com.example.buildpro.model.Order;
import com.example.buildpro.model.User;
import com.example.buildpro.model.Cart;
import com.example.buildpro.model.CartItem;
import com.example.buildpro.model.Address;
import com.example.buildpro.repository.AddressRepository;
import com.example.buildpro.repository.OrderRepository;
import com.example.buildpro.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CartRepository cartRepository;

    @Transactional
    public Order processPayment(Long userId, Long addressId, String paymentMethod, String paymentId) {
        try {
            // Get user and address
            User user = new User();
            user.setId(userId);
            
            Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

            // Get user's cart
            Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

            if (cart.getCartItems().isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }

            // Calculate total amount
            double totalAmount = cart.getCartItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();

            // Add delivery charges (5% of total)
            double deliveryCharge = totalAmount * 0.05;
            
            // Apply cluster discount (15% of delivery charge if total > 1000)
            double discount = 0;
            if (totalAmount > 1000) {
                discount = deliveryCharge * 0.15;
            }

            double finalAmount = totalAmount + deliveryCharge - discount;

            // Create order
            Order order = new Order();
            order.setUser(user);
            order.setAddress(address);
            order.setTotalAmount(totalAmount);
            order.setDeliveryCharge(deliveryCharge);
            order.setDiscountAmount(discount);
            order.setFinalAmount(finalAmount);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            order.setOrderDate(LocalDateTime.now());
            order.setPaymentMethod(paymentMethod);
            order.setPaymentId(paymentId);
            order.setOrderNumber(generateOrderNumber());

            // Create order items from cart items
            List<com.example.buildpro.model.OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : cart.getCartItems()) {
                com.example.buildpro.model.OrderItem orderItem = new com.example.buildpro.model.OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getProduct().getPrice());
                orderItem.setSubtotal(cartItem.getProduct().getPrice() * cartItem.getQuantity());
                orderItems.add(orderItem);
            }
            order.setOrderItems(orderItems);

            // Save order
            Order savedOrder = orderRepository.save(order);

            // Clear cart after successful order
            cart.getCartItems().clear();
            cartRepository.save(cart);

            return savedOrder;

        } catch (Exception e) {
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<Order> getUserOrders(Long userId) {
        User user = new User();
        user.setId(userId);
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
}
