package com.rex.specdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色實體類別
 * 用於 RBAC 權限控制
 */
@Entity
@Table(name = "role", indexes = {
        @Index(name = "idx_role_code", columnList = "code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /**
     * 主鍵，自動遞增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色代碼，唯一值（如：ROLE_ADMIN, ROLE_USER）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 角色名稱
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 角色描述
     */
    @Column(length = 200)
    private String description;

    /**
     * 狀態：0-停用，1-啟用
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 擁有此角色的會員（多對多反向）
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Member> members = new HashSet<>();

    /**
     * 角色擁有的權限（多對多）
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 角色可存取的菜單（多對多）
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_menu", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "menu_id"))
    @Builder.Default
    private Set<Menu> menus = new HashSet<>();

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
