package com.shop.security;

import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全上下文工具类
 */
public final class SecurityUtils {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MERCHANT = "ROLE_MERCHANT";
    public static final String ROLE_OPERATOR = "ROLE_OPERATOR";
    public static final String ROLE_CUSTOMER_SERVICE = "ROLE_CUSTOMER_SERVICE";
    public static final String ROLE_USER = "ROLE_USER";

    private SecurityUtils() {
    }

    public static SecurityUser getCurrentSecurityUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof SecurityUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return (SecurityUser) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentSecurityUser().getUser().getId();
    }

    public static String getCurrentUsername() {
        return getCurrentSecurityUser().getUser().getUsername();
    }

    public static List<String> getCurrentRoles() {
        return getCurrentSecurityUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public static boolean hasRole(String role) {
        return getCurrentRoles().stream().anyMatch(role::equalsIgnoreCase);
    }

    public static boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }

    public static boolean isMerchant() {
        return hasRole(ROLE_MERCHANT);
    }

    public static boolean isOperator() {
        return hasRole(ROLE_OPERATOR);
    }

    public static boolean isCustomerService() {
        return hasRole(ROLE_CUSTOMER_SERVICE);
    }

    public static boolean isUser() {
        return hasRole(ROLE_USER);
    }

    public static boolean canViewAdminOrderScope() {
        return isAdmin() || isOperator() || isCustomerService();
    }
}
