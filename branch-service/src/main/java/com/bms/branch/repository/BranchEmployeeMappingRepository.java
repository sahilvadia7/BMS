package com.bms.branch.repository;

import com.bms.branch.model.BranchEmployeeMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchEmployeeMappingRepository extends JpaRepository<BranchEmployeeMapping,Long> {
}
