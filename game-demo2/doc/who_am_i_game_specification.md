* 禁止修改後端模組LinebotGame1
* 只可以修改前端模組game-demo2
### 遊戲說明
* 遊戲名稱：我是誰(who_am_i_game)，
* 遊戲玩法：網頁會顯示一張圖片，讓玩家猜圖片中的角色是誰，玩家可以在聊天室中輸入答案進行猜測，系統會根據玩家的輸入回應是否答對，並在遊戲結束後根據答題時間與答錯次數進行排名。

### UI需求
1. 在game-demo2首頁新增一個按鈕who_am_i_game，按下後會導入到who_am_i_game頁面
2. who_am_i_game頁面版面可參考devour_game.html
3. who_am_i_game頁面中央是一個圖案顯示區塊，右邊是聊天室區域，中央上方有一個資訊區域
4. 資訊區域
   * 功能跟devour_game一致可參考devour_game.html
   * 開始遊戲按鈕、題目編號INPUT欄位、延長時間按鈕
   * 顯示難易度等級(1~5顆星⭐️)
   * 顯示目前題目提示詞prompts中的提示詞
   * 有一個秒數倒數的區域
5. 圖案顯示區塊
   * 將收到的圖片網址，顯示在該區塊中
6. 聊天室區域
   * 功能跟devour_game一致可參考devour_game.html
   * 顯示玩家猜的答案與系統回應訊息
   * 玩家顯示資訊為{大頭像}{單位名稱}-{玩家名稱}
   * 系統回應訊息UI顏色要與玩家顏色不同，以利區分
7. 遊戲結束後跳出的視窗
   * 功能跟devour_game類似可參考devour_game.html
   * 顯示排名資訊，包含玩家名稱、大頭像、答題時間(秒)、答錯次數、總成績(秒)
   * 圖案顯示區塊改為正確答案的圖片與名稱
8. 整體風格可參考devour_game.html

### 邏輯需求
1. 在who_am_i_game頁面中，使用webSocket連線到後端LinebotGame1模組
2. 在who_am_i_game頁面中，有一個開始按鈕與一個INPUTM用來輸入題目編號
   1. 按下開始按鈕後，呼叫LinebotGame1模組的GameController.getWhoAmIGameTopic api，並傳入題目編號
   2. api回傳格式參考ApiGameResponse、與WhoAmIGameModel
   3. api範例
      ```json
      {
      "success": true,
      "message": "成功",
      "data": {
      "id": "1",
      "displayName": "皮卡丘",
      "answers": [
      "皮卡丘",
      "皮神"
      ],
      "prompts": [
      "黃色的",
      "電屬性",
      "老鼠"
      ],
      "level": 1,
      "questionImageUrl": "https://...略",
      "promptImageUrl": "https://...略",
      "answerImageUrl": "https://...略"
      }
      ```
      4. 說明
      * displayName為角色名稱，answers為答案陣列，prompts為提示詞陣列，level為難度等級，id為題目編號，
    questionImageUrl為題目圖片網址，promptImageUrl為提示詞圖片網址，answerImageUrl為答案圖片網址
   4. prompts至少有一個提示詞，一開始就第一個prompts顯示在資訊區域中
   5. answers為正確答案比對詞陣列，玩家只要猜中其中一個詞即為答對
   6. level用來顯示難易度1 顯示一顆星 ⭐️依此類推，最高5顆星
   7. questionImageUrl為題目圖片一開始就顯示這張
   8. promptImageUrl為提示圖片，當時間經過20秒後，切換為這張
   9. answerImageUrl為答案圖片，當遊戲結束，切換為這張


3. 在who_am_i_game頁面中，當收到socketMessageResponse時，根據socketMessageResponse.game.type進行不同的處理
   * 當type為GUESS時，表示是使用者猜答案的訊息，比對socketMessageResponse.game.text
     * 如果玩家答錯，顯示「{玩家名稱}猜{text}！❌答錯啦~(將錯誤答案顯示在聊天室)」在聊天室上
     * 如果玩家答對，顯示「{玩家名稱}答對了！💯(不要顯示正確答案)！」在聊天室上
     * 如果玩家答對，要記錄答題的時間秒數，用來排名。
     * 如果玩家答錯，要記錄答錯次，當遊戲結束後，每答錯一次該玩家成績秒數延長5秒
   * 當type為MESSAGE時，顯示使用者發送的訊息在聊天室上
4. 延長時間按鈕，按下後倒數時間+15秒。
5. 開始後倒數時間為30秒，時間到遊戲結束。
6. 當倒數時間到，遊戲結束
   1. 顯示正確答案圖片與名稱在圖案顯示區塊
   2. 計算排名成績，成績計算方式為答題時間(秒)+答錯次數*5秒
   3. 根據成績進行排名，成績較低者排名較前
   4. 顯示排名視窗，內容包含玩家名稱、大頭像、答題時間(秒)、答錯次數、總成績(秒)
7. prompts至少有一個提示詞，一開始就第一個prompts顯示，時間每經過15秒，就在提示區依序多顯示一個prompts提示詞，直到提示詞顯示完為止

