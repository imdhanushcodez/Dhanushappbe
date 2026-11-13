package com.developersfab.Dhanushappbe.auth;

import com.developersfab.Dhanushappbe.common.AppException;
import com.developersfab.Dhanushappbe.common.ErrorCode;
import com.developersfab.Dhanushappbe.common.FieldErrorItem;
import com.developersfab.Dhanushappbe.common.MessageResponse;
import com.developersfab.Dhanushappbe.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RateLimiterService rateLimiterService;

    @Value("${app.jwt.cookie-name:access_token}")
    private String cookieName;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    private static final String DUMMY_BCRYPT = "$2a$11$e8k6m7v2a4m8c3x5b9n1o.e8k6m7v2a4m8c3x5b9n1o.e8k6m7v2a4m8c";

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String mobile = request.getMobile().trim();

        if (userRepository.existsByEmail(email)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    ErrorCode.EMAIL_ALREADY_REGISTERED,
                    "This email is already registered.",
                    List.of(new FieldErrorItem("email", "ALREADY_EXISTS", "This email is already registered."))
            );
        }

        if (userRepository.existsByMobile(mobile)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    ErrorCode.MOBILE_ALREADY_REGISTERED,
                    "This mobile number is already registered.",
                    List.of(new FieldErrorItem("mobile", "ALREADY_EXISTS", "This mobile number is already registered."))
            );
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .mobile(mobile)
                .passwordChangedAt(Instant.now())
                .build();

        User saved = userRepository.save(user);

        return SignUpResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .mobile(saved.getMobile())
                .build();
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String clientIp = getClientIp(httpRequest);
        String email = request.getEmail().toLowerCase().trim();
        String rateLimitKey = "login:" + clientIp + ":" + email;

        if (!rateLimiterService.isAllowed(rateLimitKey)) {
            throw new AppException(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_REQUESTS, "Too many login attempts. Please try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            passwordEncoder.matches("dummyPassword", DUMMY_BCRYPT);
            throw new AppException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "Email or password is incorrect.");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "Email or password is incorrect.");
        }

        rateLimiterService.reset(rateLimitKey);

        String jwt = tokenProvider.generateToken(user.getId());

        // Set HttpOnly access_token cookie
        Cookie cookie = new Cookie(cookieName, jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) tokenProvider.getExpirySeconds());
        cookie.setAttribute("SameSite", cookieSameSite);
        httpResponse.addCookie(cookie);

        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .name(user.getName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .build();
    }

    public MessageResponse logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
        return new MessageResponse("Logged out successfully.");
    }

    public LoginResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "User not found."));

        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .name(user.getName())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .build();
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String email = request.getEmail().toLowerCase().trim();
        String rateLimitKey = "forgot:" + clientIp + ":" + email;

        if (!rateLimiterService.isAllowed(rateLimitKey)) {
            throw new AppException(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.TOO_MANY_REQUESTS, "Too many password reset requests. Please try again later.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            resetTokenRepository.deleteAllByUserId(user.getId());

            byte[] randomBytes = new byte[32];
            new SecureRandom().nextBytes(randomBytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            String tokenHash = hashToken(rawToken);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .expiresAt(Instant.now().plusSeconds(15 * 60))
                    .build();

            resetTokenRepository.save(resetToken);

            String resetLink = frontendBaseUrl + "/reset-password?token=" + rawToken;
            log.info("Password reset link generated for user {}: {}", user.getId(), resetLink);
        }

        return new MessageResponse("If this email is registered, a reset link has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken());

        PasswordResetToken token = resetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_OR_EXPIRED_TOKEN, "This reset link is invalid or has expired."));

        if (token.getUsedAt() != null || Instant.now().isAfter(token.getExpiresAt())) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_OR_EXPIRED_TOKEN, "This reset link is invalid or has expired.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        resetTokenRepository.save(token);

        return new MessageResponse("Password has been successfully reset.");
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

