package com.bms.branch.controller;

import com.bms.branch.dto.request.BranchRequestDto;
import com.bms.branch.dto.response.BranchResponseDto;
import com.bms.branch.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/api/v1/branches")
public class BranchController {

    final private BranchService branchService;

    @PostMapping
    public ResponseEntity<BranchResponseDto> createBranch(@RequestBody @Valid BranchRequestDto requestDto) {
        BranchResponseDto response = branchService.createBranch(requestDto);
        return ResponseEntity.ok(response);
    }

}
