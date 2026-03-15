package org.example.webmetathreads.repository;

import org.example.webmetathreads.entity.ThreadsPost;
import org.example.webmetathreads.entity.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Threads 發文記錄 Repository
 */
@Repository
public interface ThreadsPostRepository extends JpaRepository<ThreadsPost, Long> {

    /**
     * 根據 container ID 查詢
     *
     * @param containerId container ID
     * @return 發文記錄
     */
    Optional<ThreadsPost> findByContainerId(String containerId);

    /**
     * 根據狀態查詢
     *
     * @param status 狀態
     * @return 發文記錄列表
     */
    List<ThreadsPost> findByStatus(PostStatus status);

    /**
     * 查詢所有待發布的記錄
     *
     * @return 待發布記錄列表
     */
    default List<ThreadsPost> findAllDraft() {
        return findByStatus(PostStatus.DRAFT);
    }
}
