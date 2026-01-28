package com.rex.specdemo.repository;

import com.rex.specdemo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 權限資料存取介面
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 依權限代碼查詢
     *
     * @param code 權限代碼
     * @return 權限（Optional）
     */
    Optional<Permission> findByCode(String code);

    /**
     * 檢查權限代碼是否存在
     *
     * @param code 權限代碼
     * @return 是否存在
     */
    boolean existsByCode(String code);
}
