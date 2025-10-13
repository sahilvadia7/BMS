package com.bms.branch.repository;

import com.bms.branch.model.Branch;
import com.bms.branch.model.BranchEmployeeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface BranchEmployeeMappingRepository extends JpaRepository<BranchEmployeeMapping,Long> {
    void deleteByBranchIdAndEmployeeId(Long branchId, Long employeeId);
    List<BranchEmployeeMapping> findByBranchId(Long branchId);

    @Query("SELECT bem.employeeId FROM BranchEmployeeMapping bem WHERE bem.branch.id = :branchId")
    List<Long> findEmployeeIdsByBranchId(Long branchId);
}
