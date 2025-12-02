說明 (繁體中文)

這個資料夾包含一個簡化自前端 TypeScript 的 Emoji 編碼/解碼實作（純邏輯，無 UI）：
- EmojiConstants.java：內含 emoji 與 alphabet 常數
- VariationSelectorMapper.java：負責 byte <-> variation selector code point 的映射
- EmojiEncoder.java：介面，定義 encode/decode
- EmojiEncoderImpl.java：實作 encode/decode（使用 UTF-8 與 variation selectors）
- EmojiEncoderTest.java：簡單的 main() 測試，對多個 emoji 與測資做 round-trip 驗證

快速開始（在 Windows PowerShell 中）：

1) 檢查是否已安裝 JDK（需要 javac 與 java）：

    java -version
    javac -version

如果這兩個指令任何一個找不到，請安裝 JDK（建議 Adoptium Temurin 或 OpenJDK）。安裝後請確保 JDK 的 bin 路徑在系統 PATH 中，或設定 JAVA_HOME 並把 %JAVA_HOME%\bin 加入 PATH。

2) 編譯並執行測試：

    cd C:\Users\rex.liu\IdeaProjects\emoji-encoder
    javac -d out java\*.java
    java -cp out EmojiEncoderTest

預期輸出：

    All tests passed

如果看到 "javac/ java 無法辨識" 的錯誤，代表環境沒有 JDK 或 PATH 未設定。

實作重點與注意事項：
- 編碼流程：把要隱藏的 plaintext 轉為 UTF-8 bytes，然後把每個 byte 映射為 Unicode variation selector code point（U+FE00..U+FE0F 與 U+E0100..U+E01EF），最後把這些 code points 附加在前導 emoji 之後。
- 解碼流程：掃描輸入字串的 code points，跳過直到遇到 variation selector，收集連續的 selector 並還原為 bytes，再用 UTF-8 解碼為原始字串。
- Java 注意事項：Java 的 byte 是 signed（-128..127），實作中使用 (b & 0xFF) 轉為 unsigned 0..255；另外要以 code point 為單位遍歷字串（使用 codePointAt 與 Character.charCount），以支援 supplementary code points (> U+FFFF)。

測試與擴充建議：
- 可改用 JUnit 5 撰寫單元測試並加到 CI。 
- 若要提供 CLI 或 library 發佈，可加入 build.gradle 或 pom.xml。

如需我幫你把測試改寫為 JUnit 測試或建立一個簡單的 Gradle 專案，我可以繼續在 repo 中新增檔案。
