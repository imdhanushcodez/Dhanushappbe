package com.developersfab.Dhanushappbe.auth;

import com.developersfab.Dhanushappbe.common.MessageResponse;
import com.developersfab.Dhanushappbe.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletResponse response) {
        MessageResponse msg = authService.logout(response);
        return ResponseEntity.ok(msg);
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getMe() {
        Long userId = SecurityUtils.getCurrentUserId();
        LoginResponse me = authService.getMe(userId);
        return ResponseEntity.ok(me);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        MessageResponse response = authService.forgotPassword(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}

