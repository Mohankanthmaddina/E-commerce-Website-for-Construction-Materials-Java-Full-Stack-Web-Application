package com.example.buildpro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentGatewayService {

    @Value("${payment.phonepe.merchant.id:BUILDPRO123456789}")
    private String merchantId;

    @Value("${payment.phonepe.salt.key:your-salt-key}")
    private String saltKey;

    @Value("${payment.phonepe.base.url:https://api-preprod.phonepe.com/apis/pg-sandbox}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> initiatePayment(String orderId, double amount, String customerId, String customerPhone) {
        try {
            // Create payment request
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("merchantId", merchantId);
            paymentRequest.put("merchantTransactionId", orderId);
            paymentRequest.put("amount", (long)(amount * 100)); // Amount in paise
            paymentRequest.put("currency", "INR");
            paymentRequest.put("redirectUrl", "https://your-domain.com/payment/callback");
            paymentRequest.put("redirectMode", "POST");
            paymentRequest.put("callbackUrl", "https://your-domain.com/payment/callback");
            
            // Customer details
            Map<String, Object> customer = new HashMap<>();
            customer.put("id", customerId);
            customer.put("phone", customerPhone);
            paymentRequest.put("user", customer);

            // Device context
            Map<String, Object> deviceContext = new HashMap<>();
            deviceContext.put("deviceOS", "WEB");
            paymentRequest.put("deviceContext", deviceContext);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("request", paymentRequest);

            // For demo purposes, we'll simulate the payment gateway response
            return simulatePaymentGatewayResponse(orderId, amount);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Payment initiation failed: " + e.getMessage());
            return errorResponse;
        }
    }

    private Map<String, Object> simulatePaymentGatewayResponse(String orderId, double amount) {
        // Simulate payment gateway response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("paymentId", "PAY_" + System.currentTimeMillis());
        response.put("transactionId", "TXN_" + UUID.randomUUID().toString().substring(0, 8));
        response.put("amount", amount);
        response.put("currency", "INR");
        response.put("status", "PENDING");
        response.put("paymentUrl", "https://simulated-payment-gateway.com/pay?orderId=" + orderId);
        response.put("message", "Payment initiated successfully");
        
        return response;
    }

    public Map<String, Object> verifyPayment(String transactionId) {
        // Simulate payment verification
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("transactionId", transactionId);
        response.put("status", "SUCCESS");
        response.put("message", "Payment verified successfully");
        
        return response;
    }

    // Receiver account details for payments
    public Map<String, Object> getReceiverAccountDetails() {
        Map<String, Object> accountDetails = new HashMap<>();
        accountDetails.put("accountNumber", "058112010000848");
        accountDetails.put("accountHolderName", "MADDINA MOHANKANTH");
        accountDetails.put("bankName", "Union Bank of India");
        accountDetails.put("ifscCode", "UBIN0805815");
        accountDetails.put("upiId", "mohankanth1784@oksbi");
        accountDetails.put("phoneNumber", "+91-8019566265");
        
        return accountDetails;
    }
}
