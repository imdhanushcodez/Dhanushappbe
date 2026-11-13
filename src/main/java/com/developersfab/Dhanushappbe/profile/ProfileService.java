package com.developersfab.Dhanushappbe.profile;

import com.developersfab.Dhanushappbe.common.MessageResponse;
import com.developersfab.Dhanushappbe.auth.User;
import com.developersfab.Dhanushappbe.auth.UserRepository;
import com.developersfab.Dhanushappbe.common.AppException;
import com.developersfab.Dhanushappbe.common.ErrorCode;
import com.developersfab.Dhanushappbe.common.FieldErrorItem;
import com.developersfab.Dhanushappbe.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.jwt.cookie-name:access_token}")
    private String cookieName;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    public ProfileResponse getProfile(Long userId) {
        User user = getUser(userId);
        return toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        String mobile = request.getMobile().trim();

        if (userRepository.existsByMobileAndIdNot(mobile, userId)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    ErrorCode.MOBILE_ALREADY_REGISTERED,
                    "This mobile number is already registered by another account.",
                    List.of(new FieldErrorItem("mobile", "ALREADY_EXISTS", "This mobile number is already registered by another account."))
            );
        }

        user.setMobile(mobile);
        user.setName(request.getName() != null ? request.getName().trim() : null);
        user.setSex(request.getSex() != null && !request.getSex().isBlank() ? request.getSex() : null);
        user.setDateOfBirth(request.getDateOfBirth());
        user.setProfilePhotoUrl(request.getProfilePhotoUrl() != null && !request.getProfilePhotoUrl().isBlank()
                ? request.getProfilePhotoUrl().trim() : null);

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    @Transactional
    public MessageResponse changePassword(Long userId, ChangePasswordRequest request, HttpServletResponse response) {
        User user = getUser(userId);

        // US-18: Wrong current password returns 403 INVALID_PASSWORD (not 401)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    ErrorCode.INVALID_PASSWORD,
                    "Current password is incorrect.",
                    List.of(new FieldErrorItem("currentPassword", "INVALID_PASSWORD", "Current password is incorrect."))
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        // Issue fresh cookie so current session continues
        String jwt = tokenProvider.generateToken(user.getId());
        Cookie cookie = new Cookie(cookieName, jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) tokenProvider.getExpirySeconds());
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);

        return new MessageResponse("Password changed successfully.");
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "User not found."));
    }

    private ProfileResponse toProfileResponse(User user) {
        return ProfileResponse.builder()
                .email(user.getEmail())
                .mobile(user.getMobile())
                .name(user.getName())
                .sex(user.getSex())
                .dateOfBirth(user.getDateOfBirth())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .build();
    }
}

