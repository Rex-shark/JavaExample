報告主人！喵~~

Checklist:
- [x] 新增文件 `LinebotGame1/doc/SocketMessageResponse.md`
- [x] 說明 `SocketMessageResponse` 的欄位與型別
- [x] 說明內含 `LineBotUserModel` 與 `GameMessageModel` 的欄位
- [x] 提供 JSON 序列化後的範例（目前預設行為）
- [x] 提供若改為小寫 enum 的備選範例、以及如何從前端傳來的字串轉回 enum 的範例程式碼

說明：
此文件說明 `SocketMessageResponse` 在 Java 物件層級的欄位結構，並給出轉為 JSON 後的範例。檔案路徑：`LinebotGame1/doc/SocketMessageResponse.md`。

1) Java 物件結構（來源檔案）
- `SocketMessageResponse` (檔案: `com.rex.linebotgame1.model.SocketMessageResponse`)
  - `boolean success` — 操作是否成功
  - `SocketMessageType type` — 訊息類型（enum，見下方）
  - `String roomId` — 房間 ID
  - `String message` — 顯示用訊息或錯誤描述
  - `LineBotUserModel user` — 使用者資訊（若有）
  - `GameMessageModel game` — 遊戲相關資料（若有）

- `LineBotUserModel`（`com.rex.linebotgame1.model.LineBotUserModel`）欄位：
  - `String lineUserId`
  - `String name`
  - `String nickname`
  - `String unitName`
  - `String title`
  - `String message`
  - `String imageUrl`

- `GameMessageModel`（`com.rex.linebotgame1.model.GameMessageModel`）欄位：
  - `GameMessageType type` — 遊戲訊息類型（enum）
  - `String text` — 訊息內容
  - `String status` — 保留欄位（尚未定義）

2) Enum 定義（目前程式碼）
- `SocketMessageType`：`SYSTEM`, `USER`, `UNKNOWN`
- `GameMessageType`：`MOVE`, `MESSAGE`, `JOIN`, `GUESS`, `UNKNOWN`

3) 預設 JSON 序列化行為（使用 Jackson 的預設設定）
- Enum 預設會被序列化為字串，使用 `enum.name()` 的值（也就是大寫，例如 `"SYSTEM"`、`"MOVE"`）。

範例（物件）:
{
  "success": true,
  "type": "SYSTEM",
  "roomId": "room-123",
  "message": "遊戲開始",
  "user": {
    "lineUserId": "Uabc123",
    "name": "王小明",
    "nickname": "小明",
    "unitName": "研發部",
    "title": "工程師",
    "message": "",
    "imageUrl": "https://example.com/avatar.png"
  },
  "game": {
    "type": "MOVE",
    "text": "up",
    "status": ""
  }
}

（上述為 JSON 輸出範例；注意 `type` 與 `game.type` 在預設情況下會是大寫字串）

4) 如果想要讓 enum 序列化為小寫字串（例如前端要看到 `"move"`），可以在 enum 加上 Jackson 註解（示意，程式檔案請另行修改）：

- 在 enum 中加入：
  - `@JsonValue`：控制序列化輸出（回傳小寫）
  - `@JsonCreator`：控制從字串反序列化回 enum

示意程式碼（僅供文件參考，請注意：此檔案不會修改程式碼）

```java
// GameMessageType 範例
public enum GameMessageType {
    MOVE, MESSAGE, JOIN, GUESS, UNKNOWN;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static GameMessageType fromJson(String value) {
        if (value == null) return UNKNOWN;
        try {
            return GameMessageType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
```

如果加上以上註解，序列化範例會變成：
{
  "success": true,
  "type": "system",
  "roomId": "room-123",
  "message": "遊戲開始",
  "user": { ... },
  "game": {
    "type": "move",
    "text": "up",
    "status": ""
  }
}

5) 從前端字串轉回 enum（常用程式範例）
- 假設接收到 `Map<String,String> body` 或 `JsonNode`，要把前端傳來的字串（如 `"move"` 或 `"MOVE"`）轉為 `GameMessageType` 或 `SocketMessageType`，可以使用 enum 類別提供的 `from`/`fromJson` 方法（程式庫中已有 `from` 實作）：

```java
String typeStr = body.get("type"); // 例如 "move"
GameMessageType gameType = GameMessageType.from(typeStr); // 會得到 GameMessageType.MOVE 或 UNKNOWN

String socketTypeStr = body.get("type");
SocketMessageType socketType = SocketMessageType.from(socketTypeStr);
```

說明：
- 目前專案中的 `GameMessageType.from(String)` 與 `SocketMessageType.from(String)` 都會處理 null、去除空白並將字串轉為大寫再用 `valueOf`，若不匹配會回傳 `UNKNOWN`。這個行為能接受前端傳來的小寫（例如 `"move"`）並正確對應。

6) 小提醒
- 本文件僅為說明與範例，未變更任何程式碼（遵守要求）。
- 如果你想要讓所有 enum 都以小寫輸出到前端，請考慮在 enum 上加入 `@JsonValue`/`@JsonCreator` 或在全域 Jackson 設定中設定自訂序列化器。

就是這樣，喵!
