package com.bms.loan.feign;

import com.bms.loan.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "http://localhost:8081/api/v1/auth")
public interface UserClient {

    @GetMapping("/{userId}")
    UserResponseDto getUserById(@PathVariable("userId") Long userId);
}
