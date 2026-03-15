package org.example.webmetathreads.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 建立 Container 請求 DTO
 *
 * @param text    發文內容
 * @param aiModel AI 模型名稱（選填）
 * @param title   標題（選填）
 */
public record ThreadsContainerRequest(
        @NotBlank(message = "發文內容不能為空") @Size(max = 500, message = "發文內容不能超過 500 字元") String text,

        String aiModel,

        String title) {
}
