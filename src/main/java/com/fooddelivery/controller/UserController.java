package com.fooddelivery.controller;

import com.fooddelivery.config.AuthConstants;
import com.fooddelivery.config.RequiresRole;
import com.fooddelivery.dto.request.CreateUserRequest;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.AppException;
import com.fooddelivery.exception.ErrorCode;
import com.fooddelivery.model.User;
import com.fooddelivery.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping("/{id}")
    @RequiresRole({Role.ADMIN, Role.RESTAURANT_OWNER, Role.CUSTOMER, Role.DELIVERY_PARTNER})
    public ResponseEntity<User> getUser(@PathVariable Integer id, HttpServletRequest request) {
        Integer callerId = (Integer) request.getAttribute(AuthConstants.ATTR_USER_ID);
        Role callerRole = (Role) request.getAttribute(AuthConstants.ATTR_USER_ROLE);
        User user = userService.getUser(id);
        if (callerRole != Role.ADMIN && !user.getId().equals(callerId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Cannot access another user's profile");
        }
        return ResponseEntity.ok(user);
    }
}
