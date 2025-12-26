package com.rex.linebotdemo.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 核心服務：
 * 1) 啟動時載入 classpath 知識庫並建索引
 * 2) 查詢時回傳 topK 相關 chunk
 */
@Service
public class RagService {

    private final RagProperties props;

    private volatile TfIdfRetriever retriever;
    private volatile List<KnowledgeChunk> chunks = List.of();

    public RagService(RagProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        // 即使 RAG 關閉也可以先建索引（小知識庫成本低）；如果你想省資源可改成 enabled 才建
        ClasspathKnowledgeBaseLoader loader = new ClasspathKnowledgeBaseLoader();
        this.chunks = List.copyOf(loader.loadAndChunk(
                props.getKnowledgeClasspathDir(),
                props.getChunkSize(),
                props.getChunkOverlap()
        ));
        this.retriever = new TfIdfRetriever(chunks);
    }

    public List<ScoredChunk> retrieve(String query) {
        TfIdfRetriever r = this.retriever;
        if (r == null) return List.of();
        return r.retrieve(query, props.getTopK(), props.getMinScore());
    }

    /**
     * 把 chunks 組裝成要塞進 prompt 的 context 文本（含來源）。
     */
    public String buildContext(String query) {
        List<ScoredChunk> scored = retrieve(query);
        if (scored.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("【檢索到的參考資料】\n");

        int max = props.getMaxContextChars();
        for (ScoredChunk s : scored) {
            KnowledgeChunk c = s.chunk();
            String block = "- source: %s (score=%.3f, chunk=%d)\n%s\n\n"
                    .formatted(c.source(), s.score(), c.index(), c.text());
            if (sb.length() + block.length() > max) {
                // 超過上限就停止
                break;
            }
            sb.append(block);
        }

        return sb.toString().trim();
    }

    /**
     * 方便測試/觀察：回傳索引 chunk 數量。
     */
    public int chunkCount() {
        return chunks.size();
    }
}
