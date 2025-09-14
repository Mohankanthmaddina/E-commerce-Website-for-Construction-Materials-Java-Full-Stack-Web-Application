package com.example.buildpro.service;


import com.example.buildpro.model.*;
import com.example.buildpro.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    public Order createOrder(User user, Long addressId) {
        Optional<Address> addressOpt = addressRepository.findByIdAndUser(addressId, user);
        if (addressOpt.isEmpty()) {
            throw new RuntimeException("Address not found");
        }
        
        Cart cart = cartService.getOrCreateCart(user);
        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Calculate order totals
        double subtotal = cart.getCartItems().stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
        
        double deliveryCharge = calculateDeliveryCharge(subtotal, addressOpt.get());
        double discount = calculateClusterDiscount(user, addressOpt.get(), deliveryCharge);
        double finalAmount = subtotal + deliveryCharge - discount;
        
        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(addressOpt.get());
        order.setTotalAmount(subtotal);
        order.setDeliveryCharge(deliveryCharge);
        order.setDiscountAmount(discount);
        order.setFinalAmount(finalAmount);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        
        // Create order items
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setSubtotal(cartItem.getProduct().getPrice() * cartItem.getQuantity());
            order.getOrderItems().add(orderItem);
            
            // Update product stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart
        cartService.clearCart(user);
        
        // Apply cluster discount benefits
        applyClusterBenefits(user, discount);
        
        return savedOrder;
    }
    
    private double calculateDeliveryCharge(double subtotal, Address address) {
        // Base delivery charge logic
        double baseCharge = 200.0; // ₹200 base charge
        if (subtotal > 5000) {
            baseCharge = 100.0; // Reduced charge for large orders
        }
        return baseCharge;
    }
    
    private double calculateClusterDiscount(User user, Address address, double deliveryCharge) {
        // Implement cluster discount logic
        // Check for nearby orders within 20km radius
        List<Order> nearbyOrders = findNearbyOrders(address, 20.0);
        
        if (!nearbyOrders.isEmpty()) {
            // Apply 5% discount on delivery charge
            return deliveryCharge * 0.05;
        }
        return 0.0;
    }
    
    private List<Order> findNearbyOrders(Address address, double radiusKm) {
        // This would typically use geospatial queries
        // For demo purposes, return some orders
        return orderRepository.findAll().subList(0, Math.min(5, orderRepository.findAll().size()));
    }
    
    private void applyClusterBenefits(User user, double discountAmount) {
        if (discountAmount > 0) {
            // For demo: Add discount amount to wallet
            Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    return walletRepository.save(newWallet);
                });
            
            wallet.setBalance(wallet.getBalance() + discountAmount);
            walletRepository.save(wallet);
            
            // Create wallet transaction
            WalletTransaction transaction = new WalletTransaction();
            transaction.setWallet(wallet);
            transaction.setAmount(discountAmount);
            transaction.setType(WalletTransaction.TransactionType.CREDIT);
            transaction.setDescription("Cluster discount benefit");
            // wallet.getTransactions().add(transaction);
        }
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    public Optional<Order> getOrderById(Long orderId, User user) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent() && orderOpt.get().getUser().getId().equals(user.getId())) {
            return orderOpt;
        }
        return Optional.empty();
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            if (status == Order.OrderStatus.DELIVERED) {
                order.setDeliveryDate(LocalDateTime.now());
            }
            return orderRepository.save(order);
        }
        return null;
    }
}
