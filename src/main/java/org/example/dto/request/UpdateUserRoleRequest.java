package org.example.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.model.UserRole;

@Data
public class UpdateUserRoleRequest {
    @NotNull(message = "Role is required")
    private UserRole role;
}
