package com.bms.account.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NomineeDTO {

    @NotBlank(message = "Nominee name is required")
    private String nomineeName;

    @NotBlank(message = "Relationship with nominee is required")
    private String relationship;

    @NotNull(message = "Nominee age is required")
    private Integer age;

    private String contactNumber;
}
