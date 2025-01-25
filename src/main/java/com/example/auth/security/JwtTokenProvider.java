package com.example.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import com.example.auth.dto.UserDTO;
import com.example.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private String secretKey = "mysk"; // Thay đổi secret key thành giá trị mạnh hơn trong thực tế

    public String createToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
