package org.example.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.example.dto.request.RegisterRequest;
import org.example.dto.request.UpdateUserRequest;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.example.model.UserRole;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserMapperTest {
    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toEntity_mapsRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("a@b.com");
        request.setFirstName("A");
        request.setLastName("B");
        request.setPassword("pass");

        User user = mapper.toEntity(request);
        assertEquals("a@b.com", user.getEmail());
        assertEquals("A", user.getFirstName());
        assertEquals("B", user.getLastName());
    }

    @Test
    void toResponse_mapsUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
        user.setRole(UserRole.CUSTOMER);

        UserResponse response = mapper.toResponse(user);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("a@b.com", response.getEmail());
        assertEquals(UserRole.CUSTOMER, response.getRole());
    }

    @Test
    void updateEntityFromRequest_ignoresNulls() {
        User user = new User();
        user.setEmail("old@b.com");
        user.setFirstName("Old");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("New");

        mapper.updateEntityFromRequest(request, user);
        assertEquals("old@b.com", user.getEmail());
        assertEquals("New", user.getFirstName());
    }
}

