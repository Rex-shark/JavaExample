package com.rex.linebotdemo.rag;

/**
 * 檢索結果：chunk + score。
 * score 越大代表越相關。
 */
public record ScoredChunk(KnowledgeChunk chunk, double score) {
}

