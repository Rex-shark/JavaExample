package org.example.webmetathreads.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.webmetathreads.common.BusinessException;
import org.example.webmetathreads.common.ResponseCode;
import org.example.webmetathreads.config.ThreadsApiProperties;
import org.example.webmetathreads.dto.ThreadsContainerRequest;
import org.example.webmetathreads.dto.ThreadsContainerResponse;
import org.example.webmetathreads.dto.ThreadsPublishResponse;
import org.example.webmetathreads.entity.ThreadsPost;
import org.example.webmetathreads.entity.enums.PostStatus;
import org.example.webmetathreads.repository.ThreadsPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Threads 發文服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadsService {

    private final ThreadsPostRepository threadsPostRepository;
    private final RestClient threadsRestClient;
    private final ThreadsApiProperties threadsApiProperties;

    /**
     * 建立 Container（步驟一）
     * 呼叫 Threads API 建立 media container
     *
     * @param request 請求
     * @return Container 回應
     */
    @Transactional
    public ThreadsContainerResponse createContainer(ThreadsContainerRequest request) {
        log.info("建立 Container: {}", request.text());

        // 建立發文記錄
        ThreadsPost post = ThreadsPost.builder()
                .aiModel(request.aiModel())
                .generatedTitle(request.title())
                .generatedContent(request.text())
                .status(PostStatus.DRAFT)
                .build();

        // 呼叫 Threads API 建立 container
        String containerId = callCreateContainerApi(request.text());
        post.setContainerId(containerId);

        // 儲存記錄
        ThreadsPost savedPost = threadsPostRepository.save(post);
        log.info("Container 建立成功: id={}, containerId={}", savedPost.getId(), containerId);

        return ThreadsContainerResponse.from(savedPost);
    }

    /**
     * 發布貼文（步驟二）
     * 呼叫 Threads API 發布 media container
     *
     * @param id 資料庫 ID
     * @return 發布回應
     */
    @Transactional
    public ThreadsPublishResponse publish(Long id) {
        log.info("發布貼文: id={}", id);

        // 查詢發文記錄
        ThreadsPost post = threadsPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.THREADS_POST_NOT_FOUND));

        // 檢查狀態
        if (post.getStatus() == PostStatus.PUBLISHED) {
            throw new BusinessException(ResponseCode.THREADS_ALREADY_PUBLISHED);
        }

        // 呼叫 Threads API 發布
        try {
            String mediaId = callPublishApi(post.getContainerId());
            post.setMediaId(mediaId);
            post.setStatus(PostStatus.PUBLISHED);
            post.setPublishedAt(LocalDateTime.now());
            log.info("發布成功: id={}, mediaId={}", id, mediaId);
        } catch (Exception e) {
            post.setStatus(PostStatus.FAILED);
            post.setRetryCount(post.getRetryCount() + 1);
            post.setErrorMessage(e.getMessage());
            log.error("發布失敗: id={}, error={}", id, e.getMessage());
            threadsPostRepository.save(post);
            throw new BusinessException(ResponseCode.THREADS_PUBLISH_FAILED, e.getMessage());
        }

        ThreadsPost savedPost = threadsPostRepository.save(post);
        return ThreadsPublishResponse.from(savedPost);
    }

    /**
     * 查詢所有發文記錄
     *
     * @return 發文記錄列表
     */
    @Transactional(readOnly = true)
    public List<ThreadsContainerResponse> findAll() {
        return threadsPostRepository.findAll().stream()
                .map(ThreadsContainerResponse::from)
                .toList();
    }

    /**
     * 查詢單筆發文記錄
     *
     * @param id 資料庫 ID
     * @return 發文記錄
     */
    @Transactional(readOnly = true)
    public ThreadsContainerResponse findById(Long id) {
        ThreadsPost post = threadsPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.THREADS_POST_NOT_FOUND));
        return ThreadsContainerResponse.from(post);
    }

    /**
     * 呼叫 Threads API 建立 container
     *
     * @param text 發文內容
     * @return container ID
     */
    private String callCreateContainerApi(String text) {
        String userId = threadsApiProperties.getUserId();
        String accessToken = threadsApiProperties.getAccessToken();
        System.out.println("userId = " + userId);
        System.out.println("accessToken = " + accessToken);
        // 檢查設定
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(accessToken)) {
            log.warn("Threads API 設定未完成，使用模擬 container ID");
            return "mock_container_" + System.currentTimeMillis();
        }

        try {
            // 準備請求參數
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("media_type", "TEXT");
            params.add("text", text);
            params.add("access_token", accessToken);

            // 呼叫 API
            @SuppressWarnings("unchecked")
            Map<String, Object> response = threadsRestClient.post()
                    .uri("/{userId}/threads", userId)
                    .body(params)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("id")) {
                return response.get("id").toString();
            }

            throw new BusinessException(ResponseCode.THREADS_API_ERROR, "回應格式錯誤");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("呼叫 Threads API 失敗: {}", e.getMessage());
            throw new BusinessException(ResponseCode.THREADS_API_ERROR, e.getMessage());
        }
    }

    /**
     * 呼叫 Threads API 發布 container
     *
     * @param containerId container ID
     * @return media ID
     */
    private String callPublishApi(String containerId) {
        String userId = threadsApiProperties.getUserId();
        String accessToken = threadsApiProperties.getAccessToken();

        // 檢查設定
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(accessToken)) {
            log.warn("Threads API 設定未完成，使用模擬 media ID");
            return "mock_media_" + System.currentTimeMillis();
        }

        // 檢查是否為模擬 container
        if (containerId != null && containerId.startsWith("mock_container_")) {
            log.warn("使用模擬 container，回傳模擬 media ID");
            return "mock_media_" + System.currentTimeMillis();
        }

        try {
            // 準備請求參數
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("creation_id", containerId);
            params.add("access_token", accessToken);

            // 呼叫 API
            @SuppressWarnings("unchecked")
            Map<String, Object> response = threadsRestClient.post()
                    .uri("/{userId}/threads_publish", userId)
                    .body(params)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("id")) {
                return response.get("id").toString();
            }

            throw new BusinessException(ResponseCode.THREADS_API_ERROR, "回應格式錯誤");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("呼叫 Threads API 發布失敗: {}", e.getMessage());
            throw new BusinessException(ResponseCode.THREADS_API_ERROR, e.getMessage());
        }
    }
}
