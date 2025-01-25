package com.example.auth.controller;

import com.example.auth.dto.PasswordResetDTO;
import com.example.auth.dto.UserDTO;
import com.example.auth.entity.User;
import com.example.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        try {
            // Đăng ký người dùng và gửi OTP đến email
            String otp = userService.registerAndSendOtp(userDTO.getPassword(), userDTO.getEmail());
            
            return ResponseEntity.ok("Registration successful. OTP sent to email. Please verify your account.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Registration failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        try {
            // Xác minh OTP
            userService.verifyOtp(email, otp);
            
            return ResponseEntity.ok("Account activated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        try {
            String jwt = userService.login(userDTO.getEmail(), userDTO.getPassword());
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            userService.forgotPassword(email);
            return ResponseEntity.ok("OTP sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody PasswordResetDTO request) {
        try {
            // Gọi phương thức từ UserService để thay đổi mật khẩu
            userService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return "Mật khẩu của bạn đã được thay đổi thành công";
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }
}
