package com.bms.branch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "branch")
@Data
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String branchCode;
    private String branchName;
    private String ifscCode;
    private String email;
    private String contactNumber;
    private Boolean status;
    private LocalDate openingDate;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "branch")
    private Set<BranchEmployeeMapping> employees = new HashSet<>();

    private LocalDate createdAt;
    private LocalDate updatedAt;
    private boolean isActive;
}
