package com.rex.linebotdemo.rag;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 不靠外部向量庫的簡易 TF-IDF 檢索器：
 * - 啟動時把 chunks 建索引
 * - 查詢時算 cosine similarity
 */
public class TfIdfRetriever {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}]+", Pattern.UNICODE_CHARACTER_CLASS);

    private final List<KnowledgeChunk> chunks;

    // vocab -> index
    private final Map<String, Integer> vocabIndex = new HashMap<>();
    // idf per vocab index
    private double[] idf;
    // doc vectors (sparse)
    private List<Map<Integer, Double>> docVectors;
    // doc vector norms
    private double[] docNorms;

    public TfIdfRetriever(List<KnowledgeChunk> chunks) {
        this.chunks = chunks == null ? List.of() : List.copyOf(chunks);
        buildIndex();
    }

    public List<ScoredChunk> retrieve(String query, int topK, double minScore) {
        if (query == null || query.isBlank()) return List.of();
        if (chunks.isEmpty()) return List.of();

        Map<Integer, Double> qVec = toTfIdfVector(query);
        double qNorm = norm(qVec);
        if (qNorm == 0) return List.of();

        PriorityQueue<ScoredChunk> heap = new PriorityQueue<>(Comparator.comparingDouble(ScoredChunk::score));

        for (int i = 0; i < chunks.size(); i++) {
            double score = cosine(qVec, qNorm, docVectors.get(i), docNorms[i]);
            if (score < minScore) continue;
            ScoredChunk sc = new ScoredChunk(chunks.get(i), score);
            heap.offer(sc);
            if (heap.size() > topK) heap.poll();
        }

        List<ScoredChunk> out = new ArrayList<>(heap);
        out.sort((a, b) -> Double.compare(b.score(), a.score()));
        return out;
    }

    private void buildIndex() {
        // 1) build vocab & document frequency
        Map<String, Integer> df = new HashMap<>();

        List<List<String>> tokenizedDocs = new ArrayList<>(chunks.size());
        for (KnowledgeChunk c : chunks) {
            List<String> tokens = tokenize(c.text());
            tokenizedDocs.add(tokens);
            Set<String> uniq = new HashSet<>(tokens);
            for (String t : uniq) {
                df.merge(t, 1, Integer::sum);
            }
        }

        // vocab index
        int idx = 0;
        for (String term : df.keySet()) {
            vocabIndex.put(term, idx++);
        }

        int vocabSize = vocabIndex.size();
        idf = new double[vocabSize];

        // smooth idf
        int N = Math.max(1, chunks.size());
        for (Map.Entry<String, Integer> e : df.entrySet()) {
            int termIndex = vocabIndex.get(e.getKey());
            int dfi = e.getValue();
            idf[termIndex] = Math.log((N + 1.0) / (dfi + 1.0)) + 1.0;
        }

        // 2) create doc vectors
        docVectors = new ArrayList<>(chunks.size());
        docNorms = new double[chunks.size()];

        for (int iDoc = 0; iDoc < chunks.size(); iDoc++) {
            Map<Integer, Double> vec = toTfIdfVector(tokenizedDocs.get(iDoc));
            docVectors.add(vec);
            docNorms[iDoc] = norm(vec);
        }
    }

    private Map<Integer, Double> toTfIdfVector(String text) {
        return toTfIdfVector(tokenize(text));
    }

    private Map<Integer, Double> toTfIdfVector(List<String> tokens) {
        Map<Integer, Integer> tf = new HashMap<>();
        for (String t : tokens) {
            Integer vi = vocabIndex.get(t);
            if (vi == null) continue;
            tf.merge(vi, 1, Integer::sum);
        }

        Map<Integer, Double> vec = new HashMap<>();
        for (Map.Entry<Integer, Integer> e : tf.entrySet()) {
            int termIndex = e.getKey();
            int freq = e.getValue();
            // log tf
            double tfWeight = 1.0 + Math.log(freq);
            vec.put(termIndex, tfWeight * idf[termIndex]);
        }
        return vec;
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();

        String normalized = text.toLowerCase(Locale.ROOT).replace("\r\n", "\n");
        List<String> tokens = new ArrayList<>();

        // 1) 英數 token（原本邏輯）
        String[] parts = TOKEN_SPLIT.split(normalized);
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (t.length() < 2) continue;
            tokens.add(t);
        }

        // 2) 中文/日文/韓文 token（CJK 字元 n-gram）
        //    目的：讓「尾牙日期」這種查詢可以匹配到文件中的「尾牙日期是...」
        String cjkOnly = normalized.replaceAll("[^\\p{IsHan}\\p{InHiragana}\\p{InKatakana}\\p{IsHangul}]", "");
        if (cjkOnly.length() >= 2) {
            addCharNgrams(tokens, cjkOnly, 2);
        }
        if (cjkOnly.length() >= 3) {
            addCharNgrams(tokens, cjkOnly, 3);
        }

        return tokens;
    }

    private void addCharNgrams(List<String> tokens, String s, int n) {
        for (int i = 0; i + n <= s.length(); i++) {
            String g = s.substring(i, i + n);
            tokens.add(g);
        }
    }

    private double cosine(Map<Integer, Double> a, double aNorm, Map<Integer, Double> b, double bNorm) {
        if (aNorm == 0 || bNorm == 0) return 0;

        // iterate smaller map
        Map<Integer, Double> small = a.size() <= b.size() ? a : b;
        Map<Integer, Double> large = a.size() <= b.size() ? b : a;

        double dot = 0;
        for (Map.Entry<Integer, Double> e : small.entrySet()) {
            Double bv = large.get(e.getKey());
            if (bv != null) dot += e.getValue() * bv;
        }
        return dot / (aNorm * bNorm);
    }

    private double norm(Map<Integer, Double> v) {
        double sum = 0;
        for (double x : v.values()) sum += x * x;
        return Math.sqrt(sum);
    }
}
