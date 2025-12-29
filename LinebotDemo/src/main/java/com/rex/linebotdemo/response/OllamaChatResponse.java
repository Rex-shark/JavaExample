package com.rex.linebotdemo.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for Ollama Chat API (/api/chat) non-stream response.
 * Example:
 * {
 *   "model": "gemma3:latest",
 *   "created_at": "...",
 *   "message": {"role":"assistant","content":"..."},
 *   "done": true,
 *   ...
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaChatResponse {

    public Message message;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        public String role;
        public String content;
    }
}
