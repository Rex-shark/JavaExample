package com.rex.jpamysql.repository;

import com.rex.jpamysql.entity.UserBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@SuppressWarnings("unused")
@Repository
public interface UserRepository extends JpaRepository<UserBase, Long> {
    Optional<UserBase> findByUuid(String uuid);
    boolean existsByUuid(String uuid);
    boolean existsByAccount(String account);
}
