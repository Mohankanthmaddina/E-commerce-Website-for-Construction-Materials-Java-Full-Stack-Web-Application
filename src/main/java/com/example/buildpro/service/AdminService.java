
package com.example.buildpro.service;

import com.example.buildpro.model.*;
import com.example.buildpro.dto.OrderDTO;
import com.example.buildpro.dto.OrderItemDTO;
import com.example.buildpro.dto.UserDTO;
import com.example.buildpro.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OTPRepository otpRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalProducts", productRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalCategories", categoryRepository.count());

        // Calculate total revenue
        Double totalRevenue = orderRepository.findAll().stream()
                .mapToDouble(Order::getFinalAmount)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        // Recent orders count
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        Long recentOrders = orderRepository.findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(lastWeek))
                .count();
        stats.put("recentOrders", recentOrders);

        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User toggleUserVerification(Long userId, Boolean isVerified) {
        return userRepository.findById(userId).map(user -> {
            // Ensure proper boolean conversion
            user.setIsVerified(isVerified != null ? isVerified : false);
            // Let @PreUpdate handle the timestamp automatically
            return userRepository.save(user);
        }).orElse(null);
    }

    public boolean deleteUser(Long userId) {
        try {
            // Get the user once to avoid multiple database calls
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                System.out.println("User not found with ID: " + userId);
                return false;
            }

            User user = userOpt.get();
            System.out.println("Starting cascade delete for user ID: " + userId + " (" + user.getName() + ")");

            // 1. Delete OTP records first (no dependencies)
            try {
                otpRepository.deleteByUser(user);
                System.out.println("✓ Deleted OTP records for user " + userId);
            } catch (Exception e) {
                System.out.println("No OTP records found for user " + userId);
            }

            // 2. Delete cart items first (they reference cart)
            try {
                Optional<Cart> userCart = cartRepository.findByUserId(userId);
                if (userCart.isPresent()) {
                    cartItemRepository.deleteByCart(userCart.get());
                    System.out.println("✓ Deleted cart items for user " + userId);
                } else {
                    System.out.println("No cart found for user " + userId);
                }
            } catch (Exception e) {
                System.out.println("No cart items found for user " + userId);
            }

            // 3. Delete cart (after cart items are deleted)
            try {
                cartRepository.deleteByUser(user);
                System.out.println("✓ Deleted cart for user " + userId);
            } catch (Exception e) {
                System.out.println("No cart to delete for user " + userId);
            }

            // 4. Delete addresses (no dependencies)
            try {
                addressRepository.deleteByUser(user);
                System.out.println("✓ Deleted addresses for user " + userId);
            } catch (Exception e) {
                System.out.println("No addresses found for user " + userId);
            }

            // 5. Delete orders (this will also delete order items due to cascade)
            try {
                List<Order> userOrders = orderRepository.findByUser(user);
                for (Order order : userOrders) {
                    orderRepository.delete(order);
                }
                System.out.println("✓ Deleted " + userOrders.size() + " orders for user " + userId);
            } catch (Exception e) {
                System.out.println("No orders found for user " + userId);
            }

            // 6. Finally delete the user (all dependencies removed)
            userRepository.deleteById(userId);
            System.out.println("✓ Successfully deleted user " + userId + " (" + user.getName() + ")");

            return true;
        } catch (Exception e) {
            System.err.println("Error deleting user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        return orderRepository.findById(orderId).map(order -> {
            order.setStatus(status);
            if (status == Order.OrderStatus.DELIVERED) {
                order.setDeliveryDate(LocalDateTime.now());
            }
            return orderRepository.save(order);
        }).orElse(null);
    }

    public Map<String, Object> getSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();

        List<Order> ordersInPeriod = orderRepository.findByOrderDateBetween(startDate, endDate);

        double totalSales = ordersInPeriod.stream()
                .mapToDouble(Order::getFinalAmount)
                .sum();

        long totalOrders = ordersInPeriod.size();
        long deliveredOrders = ordersInPeriod.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .count();

        report.put("totalSales", totalSales);
        report.put("totalOrders", totalOrders);
        report.put("deliveredOrders", deliveredOrders);
        report.put("periodStart", startDate);
        report.put("periodEnd", endDate);
        report.put("orders", ordersInPeriod);

        return report;
    }

    public List<OrderDTO> getAllOrdersDTO() {
        return orderRepository.findAll().stream().map(this::convertToOrderDTO).collect(Collectors.toList());
    }

    public OrderDTO getOrderDTOById(Long orderId) {
        return orderRepository.findById(orderId).map(this::convertToOrderDTO).orElse(null);
    }

    private OrderDTO convertToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserName(order.getUser().getName());
        dto.setUserEmail(order.getUser().getEmail());
        dto.setAddress(formatAddress(order.getAddress()));
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDeliveryCharge(order.getDeliveryCharge());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliveryDate(order.getDeliveryDate());

        // Format display strings
        dto.setStatusDisplay(order.getStatus().toString());
        dto.setOrderDateDisplay(order.getOrderDate() != null
                ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "");
        dto.setDeliveryDateDisplay(order.getDeliveryDate() != null
                ? order.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "");

        // Convert order items
        dto.setOrderItems(order.getOrderItems().stream().map(this::convertToOrderItemDTO).collect(Collectors.toList()));

        return dto;
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setProductBrand(orderItem.getProduct().getBrand());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getSubtotal());
        return dto;
    }

    private String formatAddress(Address address) {
        return String.format("%s, %s, %s - %s",
                address.getAddressLine1(),
                address.getCity(),
                address.getState(),
                address.getPostalCode());
    }

    public List<UserDTO> getAllUsersDTO() {
        return userRepository.findAll().stream().map(this::convertToUserDTO).collect(Collectors.toList());
    }

    public UserDTO getUserDTOById(Long userId) {
        return userRepository.findById(userId).map(this::convertToUserDTO).orElse(null);
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setIsVerified(user.getIsVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Format display strings
        dto.setRoleDisplay(user.getRole().toString());
        // Ensure proper boolean handling for display
        boolean isVerified = user.getIsVerified() != null ? user.getIsVerified() : false;
        dto.setStatusDisplay(isVerified ? "Verified" : "Unverified");
        try {
            dto.setCreatedAtDisplay(user.getCreatedAt() != null
                    ? user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "");
            dto.setUpdatedAtDisplay(user.getUpdatedAt() != null
                    ? user.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "");
        } catch (Exception e) {
            System.err.println("Error formatting timestamps: " + e.getMessage());
            dto.setCreatedAtDisplay("");
            dto.setUpdatedAtDisplay("");
        }

        return dto;
    }
}
