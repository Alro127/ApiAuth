package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    public String registerAndSendOtp(String password, String email) throws MessagingException {
        // Mã hóa mật khẩu
        String encodedPassword = passwordEncoder.encode(password);

        // Tạo OTP ngẫu nhiên
        String otp = generateOtp();

        // Lưu người dùng vào cơ sở dữ liệu với mật khẩu đã mã hóa
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword); // Mã hóa mật khẩu trước khi lưu
        user.setOtp(otp);
        user.setActive(false);
        userRepository.save(user);

        // Gửi OTP qua email
        emailService.sendOtp(email, otp);

        return otp;
    }
    
    public void resetPassword(String email, String otp, String newPassword) throws Exception {
        Optional<User> userOptional = userRepository.findByEmail(email);
        String encodedPassword = passwordEncoder.encode(newPassword);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Kiểm tra OTP
            if (otp.equals(user.getOtp())) {
                // Đặt lại mật khẩu
                user.setPassword(encodedPassword); // Bạn có thể mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu
                user.setOtp(null); // Xóa OTP sau khi đổi mật khẩu
                userRepository.save(user);
            } else {
                throw new Exception("OTP không chính xác");
            }
        } else {
            throw new Exception("Không tìm thấy người dùng");
        }
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

    public void forgotPassword(String email) throws Exception {
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
    
    

    
    public void verifyOtp(String email, String otp) {
        // Lấy người dùng từ cơ sở dữ liệu
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra OTP
        if (!user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // Kích hoạt tài khoản
        user.setActive(true);
        user.setOtp(null); // Xóa OTP sau khi xác minh
        userRepository.save(user);
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }
}