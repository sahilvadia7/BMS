package com.bms.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime loginDate;
}