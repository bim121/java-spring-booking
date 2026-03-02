package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.dto.request.UpdateUserRequest;
import org.example.dto.request.UpdateUserRoleRequest;
import org.example.dto.response.UserResponse;
import org.example.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "User profile and role management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Get current user",
            description = "Returns profile information of authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile returned",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponse response = userService.getCurrentUser(email);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update current user",
            description = "Updates profile information of authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request) {
        String email = authentication.getName();
        UserResponse response = userService.updateCurrentUser(email, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update user role (ADMIN only)",
            description = "Allows ADMIN to change role of a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role updated",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @Parameter(description = "User ID", example = "5")
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        UserResponse response = userService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok(response);
    }
}
