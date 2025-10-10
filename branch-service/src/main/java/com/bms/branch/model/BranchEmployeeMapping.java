package com.bms.branch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "branch_employee_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = "employee_id"))
public class BranchEmployeeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "employee_id", nullable = false, unique = true)
    private Long employeeId;

    private LocalDate assignedDate;
}
