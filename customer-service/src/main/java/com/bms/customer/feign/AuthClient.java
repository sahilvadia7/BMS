package com.bms.customer.feign;

import com.bms.customer.dtos.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "http://localhost:8081/api/v1/auth")
public interface AuthClient {

    @GetMapping("/{userId}")
    UserResponse getUser(@PathVariable("userId") Long userId);
}
