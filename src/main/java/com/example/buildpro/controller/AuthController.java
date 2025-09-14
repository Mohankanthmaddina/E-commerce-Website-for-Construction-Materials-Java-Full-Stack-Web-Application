package com.example.buildpro.controller;

import com.example.buildpro.model.OTP;
import com.example.buildpro.model.User;
import com.example.buildpro.dto.LoginRequest;
import com.example.buildpro.dto.RegisterRequest;
import com.example.buildpro.dto.OTPRequest;
import com.example.buildpro.service.AuthService;
import com.example.buildpro.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isEmpty() || !userOpt.get().getIsVerified()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials or account not verified"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        }

        //  Check role from request
        String requestedRole = request.getRole(); // Add 'role' field in LoginRequest DTO
        if (requestedRole == null || !requestedRole.equalsIgnoreCase(user.getRole().name())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role mismatch or not allowed"));
        }

        //  Redirect based on role
        String redirectUrl;
        if (user.getRole() == User.Role.ADMIN) {
            redirectUrl = "/admin/dashboard";
        } else {
            redirectUrl = "/homepage";
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", user,
                "redirectUrl", redirectUrl
        ));
    }


    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

   
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    @ResponseBody
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        boolean sent = authService.initiatePasswordReset(email);
        if (sent) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("email", email);
            response.put("type", "reset");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Email not found or not verified"));
        }
    }


    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpCode = request.get("otpCode");
        String newPassword = request.get("newPassword");

        boolean success = authService.resetPassword(email, otpCode, newPassword);
        if (success) {
            return ResponseEntity.ok("Password reset successful");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP or email");
        }
    }
    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOTP(@RequestBody OTPRequest request) {
        boolean isValid = authService.verifyOTP(request.getEmail(), request.getOtpCode(), request.getPurpose());
        if (isValid) {
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    @GetMapping("/otp-verification")
    public String otpVerificationPage(@RequestParam String type, @RequestParam String email, Map<String, Object> model) {
        model.put("type", type);
        model.put("email", email);
        return "otp-verification";
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // check if email already exists if exists not possible to change 
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
        }

    //  Create user  setters
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword())); // encode password
    user.setName(request.getName());

    //  set role based on request
    try {
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase())); // USER or ADMIN
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid role provided"));
    }

    user.setIsVerified(false); // start as not verified until OTP

    //  save user 
    User savedUser = authService.registerUser(user);

    // send OTP logic 
    // emailService.sendOtp(savedUser.getEmail());

    return ResponseEntity.ok(Map.of(
            "message", "OTP sent to email",
            "email", savedUser.getEmail(),
            "role", savedUser.getRole().name()
    ));
}


    @GetMapping("/registration-verification")
    public String registrationVerificationPage(@RequestParam String email, Map<String, Object> model) {
        model.put("type", "registration");
        model.put("email", email);
        return "otp-verification";
    }   
    @PostMapping("/registration-verification")
    @ResponseBody
    public String registrationVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpCode = request.get("otpCode");

        boolean verified = authService.verifyOTP(email, otpCode, OTP.Purpose.REGISTRATION);

        if (verified) {
            ResponseEntity.ok(Map.of("message", "Account verified successfully"));
            return "login";
        } else {
            ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
            return "registration-verification";
        }
    }
    @GetMapping("/homepage")
    public String redirectToHomepage() {
        return "homepage";
    }


}
