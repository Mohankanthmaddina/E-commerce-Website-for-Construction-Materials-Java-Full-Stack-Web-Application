package com.example.buildpro.controller;

import com.example.buildpro.dto.ProductDTO;
import com.example.buildpro.dto.OrderDTO;
import com.example.buildpro.dto.UserDTO;
import com.example.buildpro.model.Order;
import com.example.buildpro.model.User;
import com.example.buildpro.service.AdminService;
import com.example.buildpro.service.OrderService;
import com.example.buildpro.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "Application is running!";
    }

    @GetMapping("/error")
    public String error() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/products")
    public String getProducts(Model model) {
        model.addAttribute("products", productService.getAllProductsDTO()); // send DTOs
        return "admin-products"; // Thymeleaf template
    }

    @GetMapping("/products/debug")
    @ResponseBody
    public String debugProducts() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total products: ").append(productService.getAllProducts().size()).append("\n");
        sb.append("Total categories: ").append(productService.getAllCategories().size()).append("\n");
        sb.append("Products:\n");
        productService.getAllProducts().forEach(product -> {
            sb.append("ID: ").append(product.getId())
                    .append(", Name: ").append(product.getName())
                    .append(", Category: ")
                    .append(product.getCategory() != null ? product.getCategory().getName() : "null")
                    .append("\n");
        });
        return sb.toString();
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {

        model.addAttribute("product", new ProductDTO());
        model.addAttribute("categories", productService.getAllCategories());
        return "admin-add-product"; // Thymeleaf template
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute ProductDTO productDTO, Model model) {
        try {
            // Basic validation
            if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
                model.addAttribute("error", "Product name is required");
                model.addAttribute("product", productDTO);
                model.addAttribute("categories", productService.getAllCategories());
                return "admin-add-product";
            }

            if (productDTO.getPrice() == null || productDTO.getPrice() <= 0) {
                model.addAttribute("error", "Valid price is required");
                model.addAttribute("product", productDTO);
                model.addAttribute("categories", productService.getAllCategories());
                return "admin-add-product";
            }

            if (productDTO.getCategoryId() == null) {
                model.addAttribute("error", "Category is required");
                model.addAttribute("product", productDTO);
                model.addAttribute("categories", productService.getAllCategories());
                return "admin-add-product";
            }

            productService.saveProduct(productDTO);
            return "redirect:/admin/products?success=Product added successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Error adding product: " + e.getMessage());
            model.addAttribute("product", productDTO);
            model.addAttribute("categories", productService.getAllCategories());
            return "admin-add-product";
        }
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        try {
            System.out.println("Attempting to load product with ID: " + id);
            ProductDTO productDTO = productService.getProductDTOById(id);
            if (productDTO == null) {
                System.out.println("Product with ID " + id + " not found");
                return "redirect:/admin/products?error=Product with ID " + id + " not found";
            }
            System.out.println("Product found: " + productDTO.getName());
            model.addAttribute("product", productDTO);
            model.addAttribute("categories", productService.getAllCategories());
            return "admin-edit-product";
        } catch (Exception e) {
            System.err.println("Error in editProductForm: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/products?error=Error loading product: " + e.getMessage();
        }
    }

    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDTO productDTO, Model model) {
        try {
            // Basic validation
            if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
                model.addAttribute("error", "Product name is required");
                model.addAttribute("product", productDTO);
                model.addAttribute("categories", productService.getAllCategories());
                return "admin-edit-product";
            }

            if (productDTO.getPrice() == null || productDTO.getPrice() <= 0) {
                model.addAttribute("error", "Valid price is required");
                model.addAttribute("product", productDTO);
                model.addAttribute("categories", productService.getAllCategories());
                return "admin-edit-product";
            }

            if (productDTO.getCategoryId() == null) {
                model.addAttribute("error", "Category is required");
                model.addAttribute("product", productDTO);
                model.addAttribute("categories", productService.getAllCategories());
                return "admin-edit-product";
            }

            productService.updateProduct(id, productDTO);
            return "redirect:/admin/products?success=Product updated successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating product: " + e.getMessage());
            model.addAttribute("product", productDTO);
            model.addAttribute("categories", productService.getAllCategories());
            return "admin-edit-product";
        }
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                return "redirect:/admin/products?success=Product deleted successfully";
            } else {
                return "redirect:/admin/products?error=Product not found";
            }
        } catch (Exception e) {
            return "redirect:/admin/products?error=Error deleting product: " + e.getMessage();
        }
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public String getAllUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsersDTO());
        return "admin-users";
    }

    @GetMapping("/users/api")
    public ResponseEntity<List<UserDTO>> getAllUsersAPI() {
        return ResponseEntity.ok(adminService.getAllUsersDTO());
    }

    @GetMapping("/users/{id}")
    public String getUserDetails(@PathVariable Long id, Model model) {
        UserDTO user = adminService.getUserDTOById(id);
        if (user == null) {
            return "redirect:/admin/users?error=User not found";
        }
        model.addAttribute("user", user);
        return "admin-user-details";
    }

    @PostMapping("/users/{userId}/verification")
    public String toggleUserVerification(
            @PathVariable Long userId,
            @RequestParam String isVerified) {
        try {
            System.out.println("=== VERIFICATION UPDATE REQUEST ===");
            System.out.println("User ID: " + userId);
            System.out.println("isVerified parameter: " + isVerified);

            // Convert string to boolean properly
            Boolean verifiedStatus = Boolean.parseBoolean(isVerified);
            System.out.println("Converted to boolean: " + verifiedStatus);

            User updatedUser = adminService.toggleUserVerification(userId, verifiedStatus);
            if (updatedUser == null) {
                System.out.println("User not found with ID: " + userId);
                return "redirect:/admin/dashboard?error=User not found";
            }

            System.out.println("User " + userId + " verification status updated to: " + updatedUser.getIsVerified());
            System.out.println("=== VERIFICATION UPDATE SUCCESS ===");
            return "redirect:/admin/dashboard?success=User verification status updated successfully";
        } catch (Exception e) {
            System.err.println("Error updating user verification: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/dashboard?error=Error updating user verification: " + e.getMessage();
        }
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUserWeb(@PathVariable Long userId, @RequestParam(required = false) String ajax) {
        try {
            System.out.println("=== DELETE USER REQUEST ===");
            System.out.println("User ID: " + userId);
            System.out.println("AJAX request: " + ajax);

            boolean deleted = adminService.deleteUser(userId);
            if (!deleted) {
                System.out.println("User not found with ID: " + userId);
                return "redirect:/admin/dashboard?error=User not found";
            }

            System.out.println("User " + userId + " deleted successfully");
            System.out.println("=== DELETE USER SUCCESS ===");

            // If it's an AJAX request, redirect back to dashboard to reload the page
            if ("true".equals(ajax)) {
                return "redirect:/admin/dashboard?success=User deleted successfully";
            }

            return "redirect:/admin/dashboard?success=User deleted successfully";
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();

            // Use a simple error message to avoid URL encoding issues
            return "redirect:/admin/dashboard?error=Error deleting user";
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        boolean deleted = adminService.deleteUser(userId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model) {
        model.addAttribute("orders", adminService.getAllOrdersDTO());
        return "admin-orders";
    }

    @GetMapping("/orders/api")
    public ResponseEntity<List<OrderDTO>> getAllOrdersAPI() {
        return ResponseEntity.ok(adminService.getAllOrdersDTO());
    }

    @GetMapping("/orders/{id}")
    public String getOrderDetails(@PathVariable Long id, Model model) {
        OrderDTO order = adminService.getOrderDTOById(id);
        if (order == null) {
            return "redirect:/admin/orders?error=Order not found";
        }
        model.addAttribute("order", order);
        return "admin-order-details";
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatusWeb(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {
        try {
            Order updatedOrder = adminService.updateOrderStatus(orderId, status);
            if (updatedOrder == null) {
                return "redirect:/admin/orders?error=Order not found";
            }
            return "redirect:/admin/orders?success=Order status updated successfully";
        } catch (Exception e) {
            return "redirect:/admin/orders?error=Error updating order status: " + e.getMessage();
        }
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status) {

        Order updatedOrder = adminService.updateOrderStatus(orderId, status);
        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/sales-report")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);

        return ResponseEntity.ok(adminService.getSalesReport(start, end));
    }

    @GetMapping("/orders/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.getAllOrders().stream()
                .filter(order -> order.getStatus() == status)
                .toList());
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate the session and removing the current session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear authentication using Spring Security
        SecurityContextHolder.clearContext();

        return "/login";
    }

}
