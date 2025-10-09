package com.bms.auth.dto.response;

import com.bms.auth.enums.Roles;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String email;
    private Roles role;
    private boolean status;
}
