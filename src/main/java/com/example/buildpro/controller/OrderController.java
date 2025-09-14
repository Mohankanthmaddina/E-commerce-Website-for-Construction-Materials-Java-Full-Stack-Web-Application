package com.example.buildpro.controller;


import com.example.buildpro.model.Order;
import com.example.buildpro.model.User;
import com.example.buildpro.service.OrderService;
import com.example.buildpro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestParam Long userId,
            @RequestParam Long addressId) {
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            Order order = orderService.createOrder(userOpt.get(), addressId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(orderService.getUserOrders(userOpt.get()));
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Optional<Order> orderOpt = orderService.getOrderById(orderId, userOpt.get());
        return orderOpt.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
}
