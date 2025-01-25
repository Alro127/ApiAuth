package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public void registerUser(String password, String email) throws Exception {
        User user = new User();
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setActive(false);

        String otp = generateOtp();
        user.setOtp(otp);

        userRepository.save(user);
        emailService.sendOtp(email, otp);
    }

    public String login(String email, String password) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtTokenProvider.createToken(user);
            } else {
                throw new Exception("Thông tin đăng nhập không hợp lệ");
            }
        } else {
            throw new Exception("Không tìm thấy người dùng");
        }
    }

    public void resetPassword(String email) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String otp = generateOtp();
            user.setOtp(otp);
            userRepository.save(user);
            emailService.sendOtp(email, otp);
        } else {
            throw new Exception("Không tìm thấy người dùng");
        }
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }
}