package com.example.log_flow.auth.mapper;

import com.example.log_flow.auth.dto.UserResponse;
import com.example.log_flow.auth.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
