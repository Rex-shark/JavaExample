package com.rex.mytools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageToBase64 {
    private static final Logger log = LoggerFactory.getLogger(ImageToBase64.class);
    private static final List<String> EXTS = List.of("png", "jpg", "jpeg", "gif");

    public static void main(String[] args) {
        // 改為從 MyTools/image 讀取
        Path imageDir = Paths.get(System.getProperty("user.dir"), "MyTools", "image");
        System.out.println("imageDir = " + imageDir);

        if (!Files.exists(imageDir) || !Files.isDirectory(imageDir)) {
            log.error("找不到資料夾: `{}`，請確認 `MyTools/image` 資料夾存在", imageDir.toAbsolutePath());
            return;
        }

        Path outputDir = Paths.get(System.getProperty("user.dir"), "MyTools", "output");
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            log.error("無法建立輸出資料夾: {}", outputDir, e);
            return;
        }

        try (Stream<Path> files = Files.list(imageDir)) {
            var images = files
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return EXTS.stream().anyMatch(name::endsWith);
                    })
                    .toList();

            if (images.isEmpty()) {
                log.info("在 `{}` 找不到圖檔", imageDir.toAbsolutePath());
                return;
            }

            for (Path imgPath : images) {
                try {
                    byte[] bytes = Files.readAllBytes(imgPath);
                    String ext = getExtension(imgPath.getFileName().toString()).orElse("png");
                    String mime = mimeForExt(ext);
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String dataUri = "data:" + mime + ";base64," + base64;

                    // 建立單一檔案輸出：原檔名（去副檔名） + _base64.txt
                    String fileName = imgPath.getFileName().toString();
                    String baseName = fileName;
                    int dot = fileName.lastIndexOf('.');
                    if (dot > 0) baseName = fileName.substring(0, dot);

                    Path outFile = outputDir.resolve(baseName + "_base64.txt");
                    Files.writeString(outFile, dataUri, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    log.info("已轉換 -> {} -> {}", imgPath.getFileName().toString(), outFile.toAbsolutePath());
                } catch (IOException e) {
                    log.error("讀取或轉換檔案失敗: {}", imgPath, e);
                }
            }

            log.info("全部完成，輸出目錄: {}", outputDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("處理圖檔時發生例外", e);
        }
    }

    private static java.util.Optional<String> getExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i >= 0 && i < name.length() - 1) {
            return java.util.Optional.of(name.substring(i + 1).toLowerCase());
        }
        return java.util.Optional.empty();
    }

    private static String mimeForExt(String ext) {
        return switch (ext) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}
