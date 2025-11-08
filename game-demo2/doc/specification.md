# 後端開發模組:LinebotGame1

# webSocket
1. 連線方式
   * webSocket 連線參考後端WebSocketConfig
2. 回應response格式
   * 參考後端SocketMessageResponse

## SocketMessageResponse 說明

* socketMessageResponse.success : boolean
  * 是否成功，預設都是true

* socketMessageResponse.type : string
  * 訊息類型，參考後端SocketMessageType
  * 目前有以下幾種:
    * SYSTEM : 來自系統訊息，目前尚未實作，收到時不處理，但是程式碼保留可修改的區塊，並記錄console.log
    * USER :  來自使用者訊息，表示此Response來自line user的操作，依照socketMessageResponse.user內的資料進行邏輯處理
    * UNKNOWN : 未知狀態，收到時不處理，但是程式碼保留可處理的區塊，並記錄console.log

* socketMessageResponse.success : roomId
  * 房間ID ，表示此Response來自哪個房間，目前都是default。

* socketMessageResponse.message : string
  *  系統保留資訊，依據不同遊戲有不同的邏輯

* socketMessageResponse.user : LineBotUserModel
  * 使用者訊息內容，參考後端LineBotUserModel
  * userMessageResponse.lineUserId : string
    * 使用者ID，來自line userId
  * userMessageResponse.name : string
    * 使用者姓名
  * userMessageResponse.nickname : string
    * 使用者暱稱
  * userMessageResponse.unitName : string
     * 使用者單位名稱
  * userMessageResponse.title : string
    * 使用者職稱
  * userMessageResponse.imageUrl : string
    * 使用者頭像URL

* socketMessageResponse.game : GameMessageModel
  *  遊戲訊息內容，參考後端GameMessageModel
  * type : GameMessageType
    * 遊戲訊息類型，參考後端GameMessageType
    * 目前有以下幾種:
      * MOVE : 使用者移動
      * MESSAGE : 使用者發送訊息
      * JOIN : 使用者加入遊戲
      * GUESS : 使用者猜答案
      * UNKNOWN : 未知狀態，收到時不處理
    * status : string
      * 依照各遊戲需求使用
    * text: string
      * 使用者發送的文字，配合GameMessageType使用。
        * 範例:當type為MOVE時，text可能是"上","下","左","右"等移動指令，代表使用者想要移動的方向。
         ```json
          {
            "type": "MOVE",
            "text": "上",
            "status": "1"
          }
        ```


