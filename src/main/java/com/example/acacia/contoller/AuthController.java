package com.example.acacia.contoller;

import com.example.acacia.SecurityConfig.JwtUtils;
import com.example.acacia.dto.LoginRequest;
import com.example.acacia.dto.LoginResponse;
import com.example.acacia.model.Member;
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtils jwtUtil;
    private final MemberRepository memberRepository;

    @Value("${app.security.cookie-secure}")
    private boolean isSecure;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(7 * 24 * 60 * 60)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired refresh token"));
        }

        String email = jwtUtil.extractUsername(refreshToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.createAccessToken(member);
        Long expirationTime = jwtUtil.extractExpiration(newAccessToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "expirationTime", expirationTime
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/tester")
    public ResponseEntity<String> tester(){
        return ResponseEntity.ok("Loan App running....");
    }

}
