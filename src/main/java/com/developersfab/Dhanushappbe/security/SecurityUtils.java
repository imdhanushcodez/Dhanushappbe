package com.developersfab.Dhanushappbe.security;

import com.developersfab.Dhanushappbe.common.AppException;
import com.developersfab.Dhanushappbe.common.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        throw new AppException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "User is not authenticated.");
    }

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new AppException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "User is not authenticated.");
    }
}

