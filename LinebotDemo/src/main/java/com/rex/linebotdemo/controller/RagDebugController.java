package com.rex.linebotdemo.controller;

import com.rex.linebotdemo.rag.RagProperties;
import com.rex.linebotdemo.rag.RagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用來快速確認知識庫是否被載入、以及某個 query 會檢索到什麼 context。
 *
 * 注意：這是 debug 用，正式環境建議加權限或移除。
 */
@RestController
public class RagDebugController {

    private final RagService ragService;
    private final RagProperties ragProperties;

    public RagDebugController(RagService ragService, RagProperties ragProperties) {
        this.ragService = ragService;
        this.ragProperties = ragProperties;
    }

    @GetMapping("/debug/rag")
    public Map<String, Object> debug(@RequestParam("q") String query) {
        return Map.of(
                "enabled", ragProperties.isEnabled(),
                "knowledgeClasspathDir", ragProperties.getKnowledgeClasspathDir(),
                "chunkCount", ragService.chunkCount(),
                "query", query,
                "context", ragService.buildContext(query)
        );
    }
}

