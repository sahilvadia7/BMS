package com.bms.auth.repository;

import com.bms.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User , Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User findByPhoneNo(String phoneNo);
}
