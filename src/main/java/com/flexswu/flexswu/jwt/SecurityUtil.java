package com.flexswu.flexswu.jwt;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class SecurityUtil {
    private SecurityUtil() {}

    /** 로그인한 사용자의 PK(userId) 반환 */
    public static Long getLoginUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUserId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패");
    }

    /** (옵션) 토큰의 로그인 아이디(identify) 필요할 때 */
    public static String getLoginIdentifyOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getIdentify();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패");
    }
}