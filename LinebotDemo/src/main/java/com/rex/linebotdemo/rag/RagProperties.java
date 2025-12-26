package com.rex.linebotdemo.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RAG 設定。
 *
 * 預設用 classpath 下的文字/Markdown 檔作為知識庫，不需要外部向量庫。
 */
@ConfigurationProperties(prefix = "ai.rag")
public class RagProperties {

    /** 是否啟用 RAG（檢索後把 context 注入 prompt） */
    private boolean enabled = false;

    /** classpath 知識庫資料夾，例如：knowledge */
    private String knowledgeClasspathDir = "knowledge";

    /** 每個 chunk 的最大字元數 */
    private int chunkSize = 800;

    /** chunk 重疊字元數，保留上下文 */
    private int chunkOverlap = 120;

    /** 取回 topK chunk */
    private int topK = 4;

    /** 最低相似度門檻（0~1），太低容易塞入雜訊 */
    private double minScore = 0.08;

    /** 最多塞進 prompt 的 context 字元數（避免太長） */
    private int maxContextChars = 4000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKnowledgeClasspathDir() {
        return knowledgeClasspathDir;
    }

    public void setKnowledgeClasspathDir(String knowledgeClasspathDir) {
        this.knowledgeClasspathDir = knowledgeClasspathDir;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getMinScore() {
        return minScore;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public int getMaxContextChars() {
        return maxContextChars;
    }

    public void setMaxContextChars(int maxContextChars) {
        this.maxContextChars = maxContextChars;
    }
}
