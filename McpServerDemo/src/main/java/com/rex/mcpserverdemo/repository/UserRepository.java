package com.rex.mcpserverdemo.repository;

import com.rex.mcpserverdemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 使用者 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 依用戶名查詢
     */
    Optional<User> findByUsername(String username);

    /**
     * 依 Email 查詢
     */
    Optional<User> findByEmail(String email);

    /**
     * 依用戶名模糊查詢
     */
    List<User> findByUsernameContaining(String keyword);

    /**
     * 檢查用戶名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 檢查 Email 是否存在
     */
    boolean existsByEmail(String email);
}
