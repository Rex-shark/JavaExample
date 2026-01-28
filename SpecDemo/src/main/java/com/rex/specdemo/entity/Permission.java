package com.rex.specdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 權限實體類別
 * 定義系統中可執行的操作權限
 */
@Entity
@Table(name = "permission", indexes = {
        @Index(name = "idx_permission_code", columnList = "code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    /**
     * 主鍵，自動遞增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 權限代碼，唯一值（如：user:create, user:read）
     */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /**
     * 權限名稱
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 權限描述
     */
    @Column(length = 200)
    private String description;

    /**
     * 擁有此權限的角色（多對多反向）
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
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
}
