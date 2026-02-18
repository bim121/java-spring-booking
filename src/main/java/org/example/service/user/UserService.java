package org.example.service.user;

import org.example.dto.request.LoginRequest;
import org.example.dto.request.RegisterRequest;
import org.example.dto.request.UpdateUserRequest;
import org.example.dto.response.JwtResponse;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.example.model.UserRole;

public interface UserService {
    UserResponse register(RegisterRequest request);

    JwtResponse login(LoginRequest request);

    UserResponse getCurrentUser(String email);

    UserResponse updateCurrentUser(String email, UpdateUserRequest request);

    UserResponse updateUserRole(Long userId, UserRole role);

    User getUserEntityByEmail(String email);

    User getUserEntityById(Long id);
}
