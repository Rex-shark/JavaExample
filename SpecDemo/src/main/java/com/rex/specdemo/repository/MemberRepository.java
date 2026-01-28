package com.rex.specdemo.repository;

import com.rex.specdemo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 會員資料存取介面
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 依帳號查詢會員
     *
     * @param username 帳號
     * @return 會員（Optional）
     */
    Optional<Member> findByUsername(String username);

    /**
     * 依 UUID 查詢會員
     *
     * @param uuid UUID
     * @return 會員（Optional）
     */
    Optional<Member> findByUuid(String uuid);

    /**
     * 檢查帳號是否存在
     *
     * @param username 帳號
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 檢查 Email 是否存在
     *
     * @param email 電子郵件
     * @return 是否存在
     */
    boolean existsByEmail(String email);
}
