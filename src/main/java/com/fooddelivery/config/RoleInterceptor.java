package com.fooddelivery.config;

import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RoleInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequiresRole requiresRole = resolveRequiresRole(handlerMethod);
        if (requiresRole == null) {
            return true;
        }

        String userIdHeader = request.getHeader(AuthConstants.HEADER_USER_ID);
        String roleHeader = request.getHeader(AuthConstants.HEADER_ROLE);
        if (userIdHeader == null || userIdHeader.isBlank() || roleHeader == null || roleHeader.isBlank()) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Missing authentication headers");
        }

        Integer userId;
        try {
            userId = Integer.parseInt(userIdHeader.trim());
        } catch (NumberFormatException ex) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Invalid user id header");
        }

        Role role;
        try {
            role = Role.valueOf(roleHeader.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Invalid role header");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED, "User not found"));
        if (user.getRole() != role) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Role header does not match user");
        }

        Set<Role> allowed = Arrays.stream(requiresRole.value()).collect(Collectors.toSet());
        if (!allowed.contains(role)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Role not permitted for this endpoint");
        }

        request.setAttribute(AuthConstants.ATTR_USER_ID, userId);
        request.setAttribute(AuthConstants.ATTR_USER_ROLE, role);
        return true;
    }

    private RequiresRole resolveRequiresRole(HandlerMethod handlerMethod) {
        RequiresRole methodAnnotation = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
    }
}
