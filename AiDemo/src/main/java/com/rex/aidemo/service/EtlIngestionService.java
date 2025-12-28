package com.rex.aidemo.service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EtlIngestionService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(EtlIngestionService.class);

    private final VectorStore vectorStore;

    // ★ 修改點 1: 使用陣列 Resource[] 接收多個檔案
    // ★ 修改點 2: 路徑改成 "classpath:knowledge/*.txt" (讀取該資料夾下所有 txt)
    @Value("classpath:knowledge/*.txt")
    private Resource[] resources;

    public EtlIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("開始執行 ETL 流程 (多檔案模式)...");

        // 1. 檢查是否有檔案
        if (resources == null || resources.length == 0) {
            logger.warn("警告：在 knowledge 資料夾下找不到任何 .txt 檔案！");
            return;
        }

        logger.info("偵測到 {} 個檔案，準備開始讀取...", resources.length);

        // 用來收集所有檔案的原始 Document
        List<Document> allRawDocuments = new ArrayList<>();

        // ---------------------------------------------------------
        // 1. Extract (批次讀取): 迴圈遍歷每個檔案
        // ---------------------------------------------------------
        for (Resource resource : resources) {
            try {
                logger.info("正在讀取檔案: {}", resource.getFilename());

                TextReader textReader = new TextReader(resource);
                // 加入檔名作為 Metadata，這樣以後搜尋時知道資料來自哪個檔案
                textReader.getCustomMetadata().put("filename", resource.getFilename());

                List<Document> docs = textReader.get();
                allRawDocuments.addAll(docs);

            } catch (Exception e) {
                logger.error("讀取檔案失敗: {}", resource.getFilename(), e);
                // 這裡選擇繼續讀下一個檔案，不中斷程式
            }
        }

        logger.info("所有檔案讀取完畢，共收集 {} 份原始文件內容。", allRawDocuments.size());

        // ---------------------------------------------------------
        // 2. Transform (轉換/切割)
        // ---------------------------------------------------------
        TokenTextSplitter textSplitter = new TokenTextSplitter();

        // 一次性切割所有文件
        List<Document> splitDocuments = textSplitter.apply(allRawDocuments);

        logger.info("文件切割完成，共產生 {} 個區塊 (Chunks)。", splitDocuments.size());

        // ---------------------------------------------------------
        // 3. Load (載入): 存入 VectorStore
        // ---------------------------------------------------------
        // 這會批次將所有區塊轉成向量並存入
        vectorStore.add(splitDocuments);

        logger.info("ETL 流程結束：所有資料已成功存入 VectorStore！");
    }
}