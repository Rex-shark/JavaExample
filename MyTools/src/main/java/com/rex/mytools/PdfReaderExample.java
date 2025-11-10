package com.rex.mytools;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfReaderExample {
    private static final Logger log = LoggerFactory.getLogger(PdfReaderExample.class);

    public static void main(String[] args) {
        // classpath 資源路徑（通常放在 src/main/resources/pdf/test.pdf）
        String resourcePath = "pdf/test.pdf";
        int dpi = 300; // 輸出解析度，可依需求調整

        // 輸出資料夾，改為 MyTools 底下
        File outputDir = Paths.get(System.getProperty("user.dir"), "MyTools", "output", "pdf").toFile();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            log.error("無法建立輸出資料夾: {}", outputDir.getAbsolutePath());
            return;
        }

        // 嘗試先從 classpath 讀取，若找不到則回退到 MyTools/src/main/resources/pdf/test.pdf
        try (InputStream is = openPdfInputStream(resourcePath)) {
            if (is == null) {
                log.error("找不到資源: {}（請確認檔案存在於 `MyTools/src/main/resources/pdf/` 或已被打包到 classpath）", resourcePath);
                return;
            }

            byte[] pdfBytes = is.readAllBytes();
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFRenderer renderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();
                log.info("開始轉圖，頁數: {} , 輸出資料夾: {}", pageCount, outputDir.getAbsolutePath());

                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    BufferedImage bimForPng = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.ARGB);

                    String baseName = String.format("pdf_page-%03d", pageIndex + 1);

                    File pngFile = new File(outputDir, baseName + ".png");
                    ImageIO.write(bimForPng, "png", pngFile);

                    BufferedImage bimForJpg = new BufferedImage(
                            bimForPng.getWidth(), bimForPng.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = bimForJpg.createGraphics();
                    try {
                        g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, bimForJpg.getWidth(), bimForJpg.getHeight());
                        g2.drawImage(bimForPng, 0, 0, null);
                    } finally {
                        g2.dispose();
                    }
                    File jpgFile = new File(outputDir, baseName + ".jpg");
                    ImageIO.write(bimForJpg, "jpg", jpgFile);

                    log.info("已輸出 -> {}", pngFile.getAbsolutePath());
                    log.info("已輸出 -> {}", jpgFile.getAbsolutePath());
                }

                log.info("全部完成");
            }
        } catch (IOException e) {
            log.error("處理 PDF 轉圖時發生例外", e);
        }
    }

    // 先嘗試 classpath，找不到時再從 MyTools/src/main/resources/pdf/ 下讀取
    private static InputStream openPdfInputStream(String resourcePath) throws IOException {
        InputStream is = PdfReaderExample.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) return is;

        Path fallback = Paths.get(System.getProperty("user.dir"), "MyTools", "src", "main", "resources", "pdf", "test.pdf");
        if (Files.exists(fallback) && Files.isRegularFile(fallback)) {
            return new FileInputStream(fallback.toFile());
        }
        return null;
    }
}
