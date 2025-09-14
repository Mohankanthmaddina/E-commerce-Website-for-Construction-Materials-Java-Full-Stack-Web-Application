package com.example.buildpro.controller;

import com.example.buildpro.model.User;
import com.example.buildpro.model.Order;
import com.example.buildpro.service.UserService;
import com.example.buildpro.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public String profilePage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);
        
        // Get user's recent orders
        List<Order> orders = paymentService.getUserOrders(user.getId());
        model.addAttribute("orders", orders);
        model.addAttribute("orderCount", orders.size());

        return "user-profile";
    }

    @GetMapping("/orders")
    public String ordersPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        List<Order> orders = paymentService.getUserOrders(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);

        return "user-orders";
    }

    @GetMapping("/edit")
    public String editProfilePage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("user", userOpt.get());
        return "edit-profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User user, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User existingUser = userOpt.get();
        existingUser.setName(user.getName());
        
        userService.updateUser(existingUser);
        
        return "redirect:/profile?success=Profile updated successfully";
    }
}
