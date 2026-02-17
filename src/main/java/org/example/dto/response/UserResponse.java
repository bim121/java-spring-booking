package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.model.UserRole;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
}
