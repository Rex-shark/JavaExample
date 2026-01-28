package com.rex.specdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系統菜單實體類別
 * 支援樹狀結構的選單管理
 */
@Entity
@Table(name = "menu", indexes = {
        @Index(name = "idx_menu_parent_id", columnList = "parent_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    /**
     * 主鍵，自動遞增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 菜單名稱
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 路由路徑
     */
    @Column(length = 200)
    private String path;

    /**
     * 圖示
     */
    @Column(length = 50)
    private String icon;

    /**
     * 父菜單（自關聯）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    /**
     * 子菜單列表
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Menu> children = new ArrayList<>();

    /**
     * 排序順序
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer sort = 0;

    /**
     * 狀態：0-停用，1-啟用
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 可存取此菜單的角色（多對多反向）
     */
    @ManyToMany(mappedBy = "menus", fetch = FetchType.LAZY)
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
