package com.developersfab.Dhanushappbe.profile;

import com.developersfab.Dhanushappbe.common.MessageResponse;
import com.developersfab.Dhanushappbe.security.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        ProfileResponse response = profileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse response) {
        Long userId = SecurityUtils.getCurrentUserId();
        MessageResponse msg = profileService.changePassword(userId, request, response);
        return ResponseEntity.ok(msg);
    }
}

