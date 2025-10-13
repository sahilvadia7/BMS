package com.bms.transaction.feing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "branch-service", path = "/api/v1/branches")
public interface BranchClient {
    @GetMapping("/{branchId}/exists")
    boolean existsById(@PathVariable("branchId") Long branchId);
}