package com.rex.specdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 會員實體類別
 * 儲存系統會員的基本資訊
 */
@Entity
@Table(name = "member", indexes = {
        @Index(name = "idx_member_uuid", columnList = "uuid"),
        @Index(name = "idx_member_username", columnList = "username")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    /**
     * 主鍵，自動遞增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * UUID，給前端使用的唯一識別碼
     */
    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    /**
     * 帳號，唯一值
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密碼（BCrypt 加密）
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * 電子郵件
     */
    @Column(length = 100)
    private String email;

    /**
     * 手機號碼
     */
    @Column(length = 20)
    private String phone;

    /**
     * 狀態：0-停用，1-啟用
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 會員擁有的角色（多對多）
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "member_role", joinColumns = @JoinColumn(name = "member_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * 建立時間
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 在持久化前自動產生 UUID
     */
    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
    }
}
