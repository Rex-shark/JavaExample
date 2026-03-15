package org.example.webmetathreads.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webmetathreads.common.ApiResponse;
import org.example.webmetathreads.dto.ThreadsContainerRequest;
import org.example.webmetathreads.dto.ThreadsContainerResponse;
import org.example.webmetathreads.dto.ThreadsPublishResponse;
import org.example.webmetathreads.service.ThreadsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Threads 發文 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
public class ThreadsController {

    private final ThreadsService threadsService;

    /**
     * 建立 Container（步驟一）
     *
     * @param request 請求
     * @return Container 回應
     */
    @PostMapping("/container")
    public ApiResponse<ThreadsContainerResponse> createContainer(
            @Valid @RequestBody ThreadsContainerRequest request) {
        log.info("建立 Container 請求: {}", request);
        ThreadsContainerResponse response = threadsService.createContainer(request);
        return ApiResponse.success("Container 建立成功", response);
    }

    /**
     * 發布貼文（步驟二）
     *
     * @param id 資料庫 ID
     * @return 發布回應
     */
    @PostMapping("/publish/{id}")
    public ApiResponse<ThreadsPublishResponse> publish(@PathVariable Long id) {
        log.info("發布貼文請求: id={}", id);
        //return ApiResponse.success("發布成功",null);
        ThreadsPublishResponse response = threadsService.publish(id);
        return ApiResponse.success("發布成功", response);
    }

    /**
     * 查詢所有發文記錄
     *
     * @return 發文記錄列表
     */
    @GetMapping("/posts")
    public ApiResponse<List<ThreadsContainerResponse>> findAll() {
        List<ThreadsContainerResponse> posts = threadsService.findAll();
        return ApiResponse.success(posts);
    }

    /**
     * 查詢單筆發文記錄
     *
     * @param id 資料庫 ID
     * @return 發文記錄
     */
    @GetMapping("/posts/{id}")
    public ApiResponse<ThreadsContainerResponse> findById(@PathVariable Long id) {
        ThreadsContainerResponse post = threadsService.findById(id);
        return ApiResponse.success(post);
    }
}
