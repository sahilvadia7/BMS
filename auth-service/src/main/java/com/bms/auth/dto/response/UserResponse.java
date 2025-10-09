package com.bms.auth.dto.response;

import com.bms.auth.enums.Roles;
import lombok.Data;

@Data
public class UserResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNo;
    private Boolean isActive;
    private Roles role;
}
