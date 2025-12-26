package com.rex.linebotdemo.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * 很單純的 chunker：用固定字元數切塊，並保留 overlap。
 */
public class TextChunker {

    public List<String> chunk(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) return List.of();
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize must be > 0");
        if (overlap < 0) throw new IllegalArgumentException("overlap must be >= 0");
        if (overlap >= chunkSize) throw new IllegalArgumentException("overlap must be < chunkSize");

        String normalized = text.replace("\r\n", "\n").trim();

        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < normalized.length()) {
            int end = Math.min(i + chunkSize, normalized.length());
            String chunk = normalized.substring(i, end).trim();
            if (!chunk.isBlank()) out.add(chunk);
            if (end >= normalized.length()) break;
            i = Math.max(0, end - overlap);
        }
        return out;
    }
}

