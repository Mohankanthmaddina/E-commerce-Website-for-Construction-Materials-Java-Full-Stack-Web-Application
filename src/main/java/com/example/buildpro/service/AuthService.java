package com.example.buildpro.service;

import com.example.buildpro.model.User;
import com.example.buildpro.model.OTP;
import com.example.buildpro.repository.UserRepository;
import com.example.buildpro.repository.OTPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OTPRepository otpRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsVerified(true);
        User savedUser = userRepository.save(user);
        
        // Generate and send OTP
        String otpCode = generateOTP();
        OTP otp = new OTP();
        otp.setUser(savedUser);
        otp.setOtpCode(otpCode);
        otp.setPurpose(OTP.Purpose.REGISTRATION);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otp);
        
        emailService.sendOTPEmail(user.getEmail(), otpCode, OTP.Purpose.REGISTRATION);
        
        return savedUser;
    }
    
    public boolean verifyOTP(String email, String otpCode, OTP.Purpose purpose) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;
        
        User user = userOpt.get();
        Optional<OTP> otpOpt = otpRepository.findByUserAndOtpCodeAndExpiresAtAfter(
            user, otpCode, LocalDateTime.now());
        
        if (otpOpt.isPresent()) {
            if (purpose == OTP.Purpose.REGISTRATION) {
                user.setIsVerified(true);
                userRepository.save(user);
            }
            otpRepository.delete(otpOpt.get());
            return true;
        }
        return false;
    }
    /*@GetMapping("/homepage")
    public String redirectToHomepage() {
        return "homepage";
    }*/
    public boolean initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmailAndIsVerifiedTrue(email);
        //print User
        System.out.println("User found: " + userOpt.isPresent());
        if (userOpt.isEmpty()){
            System.out.println("is returning empty");
            return false;
        }

        User user = userOpt.get();
        String otpCode = generateOTP();
        System.out.println("Generated OTP Code: ******************************************" );
        OTP otp = new OTP();
        otp.setUser(user);
        otp.setOtpCode(otpCode);
        otp.setPurpose(OTP.Purpose.PASSWORD_RESET);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otp);
        System.out.println("OTP Code: sent*****************************************"+email+" otp: "+otpCode+" OTP.purose: "+OTP.Purpose.PASSWORD_RESET);     
        emailService.sendOTPEmail(email, otpCode, OTP.Purpose.PASSWORD_RESET);
        System.out.println("OTP Code: sent to email true*****************************************");
        return true;
    }
    
    public boolean resetPassword(String email, String otpCode, String newPassword) {
        //System.out.println("otp -verificaiton"+verifyOTP(email, otpCode, OTP.Purpose.PASSWORD_RESET));
        if (verifyOTP(email, otpCode, OTP.Purpose.PASSWORD_RESET)) {
            System.out.println(userRepository.findByEmail(email).isEmpty()+"********************************");
            Optional<User> userOpt = userRepository.findByEmail(email);
            System.out.println("user verificcation inside reset password if condition"+userOpt.isPresent());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
    
    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
