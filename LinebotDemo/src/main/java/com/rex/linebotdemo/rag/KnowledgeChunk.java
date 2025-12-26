package com.rex.linebotdemo.rag;

/**
 * 知識庫切塊後的最小單位。
 * @param source 來源檔名（classpath 相對路徑）
 * @param index  chunk 序號
 * @param text   chunk 的文字
 */
public record KnowledgeChunk(String source, int index, String text) {
}

