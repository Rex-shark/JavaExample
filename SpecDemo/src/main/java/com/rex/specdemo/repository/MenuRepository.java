package com.rex.specdemo.repository;

import com.rex.specdemo.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜單資料存取介面
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * 查詢所有頂層菜單（無父菜單）
     *
     * @return 頂層菜單列表
     */
    List<Menu> findByParentIsNullOrderBySortAsc();

    /**
     * 依父菜單 ID 查詢子菜單
     *
     * @param parentId 父菜單 ID
     * @return 子菜單列表
     */
    List<Menu> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 依狀態查詢所有菜單
     *
     * @param status 狀態
     * @return 菜單列表
     */
    List<Menu> findByStatusOrderBySortAsc(Integer status);
}
