# LinebotDemo 知識庫（示例）

## RAG 是什麼
RAG（Retrieval-Augmented Generation）是在呼叫大型語言模型前，先從知識庫檢索出與問題相關的內容，
再把這些內容當作 context 一起提供給模型，讓回答更貼近你的資料，並減少胡亂編造。

## WebClient 結構（Gemini generateContent）
請求 body 結構：
- contents: array
  - parts: array
    - text: 文字 prompt

如果把 JSON 先序列化成字串再送出，會被包一層引號，導致 API 端解析異常。

## JWT
JWT 是一種用來在系統間傳遞 claims 的 token。JWT 內容是 base64url 編碼，不是加密；
需要保密請另外加密或只放不敏感資訊。

