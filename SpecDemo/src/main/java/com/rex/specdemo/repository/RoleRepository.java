package com.rex.specdemo.repository;

import com.rex.specdemo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色資料存取介面
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 依角色代碼查詢
     *
     * @param code 角色代碼
     * @return 角色（Optional）
     */
    Optional<Role> findByCode(String code);

    /**
     * 檢查角色代碼是否存在
     *
     * @param code 角色代碼
     * @return 是否存在
     */
    boolean existsByCode(String code);
}
