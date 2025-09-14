package com.example.buildpro.controller;

import com.example.buildpro.model.Address;
import com.example.buildpro.model.User;
import com.example.buildpro.service.AddressService;
import com.example.buildpro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/addresses")
@CrossOrigin(origins = "*")
public class AddressController {
    
    @Autowired
    private AddressService addressService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getUserAddresses(@PathVariable Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(addressService.getUserAddresses(userOpt.get()));
    }
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<Address> createAddress(@PathVariable Long userId, @RequestBody Address address) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(addressService.createAddress(userOpt.get(), address));
    }
    
    @PutMapping("/{addressId}/user/{userId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            @RequestBody Address address) {
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Address updatedAddress = addressService.updateAddress(addressId, userOpt.get(), address);
        if (updatedAddress == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(updatedAddress);
    }
    
    @DeleteMapping("/{addressId}/user/{userId}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId) {
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        boolean deleted = addressService.deleteAddress(addressId, userOpt.get());
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<Address> getDefaultAddress(@PathVariable Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound(). build();
        }
        
        Address defaultAddress = addressService.getDefaultAddress(userOpt.get());
        if (defaultAddress == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(defaultAddress);
    }
}
