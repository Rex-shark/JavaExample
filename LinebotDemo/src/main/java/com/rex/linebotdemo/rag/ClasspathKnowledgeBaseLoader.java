package com.rex.linebotdemo.rag;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 從 classpath 讀取知識庫檔案。
 *
 * 支援：txt / md / json
 *
 */
public class ClasspathKnowledgeBaseLoader {

    public List<KnowledgeChunk> loadAndChunk(String classpathDir, int chunkSize, int chunkOverlap) {
        if (classpathDir == null || classpathDir.isBlank()) {
            throw new IllegalArgumentException("classpathDir is blank");
        }

        String normalized = classpathDir;
        while (normalized.startsWith("/")) normalized = normalized.substring(1);
        while (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);

        String pattern = "classpath*:%s/**/*.{txt,md,json}".formatted(normalized);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources(pattern);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan knowledge base resources: " + pattern, e);
        }

        TextChunker chunker = new TextChunker();
        List<KnowledgeChunk> out = new ArrayList<>();

        for (Resource r : resources) {
            if (!r.exists()) continue;

            String text = readUtf8(r);
            List<String> chunks = chunker.chunk(text, chunkSize, chunkOverlap);
            String source = safeSourceName(r);
            for (int i = 0; i < chunks.size(); i++) {
                out.add(new KnowledgeChunk(source, i, chunks.get(i)));
            }
        }
        return out;
    }

    private String readUtf8(Resource r) {
        try (InputStream is = r.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + r, e);
        }
    }

    private String safeSourceName(Resource r) {
        try {
            String uri = r.getURI().toString();
            // 盡量把路徑做成相對可讀
            int idx = uri.indexOf("!/" );
            if (idx >= 0) return uri.substring(idx + 2);
            return r.getFilename() != null ? r.getFilename() : uri;
        } catch (IOException e) {
            return r.getFilename() != null ? r.getFilename() : r.toString();
        }
    }
}

