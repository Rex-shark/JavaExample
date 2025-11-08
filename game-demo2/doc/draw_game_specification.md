* 禁止修改後端模組LinebotGame1
* 只可以修改前端模組game-demo2
### UI需求
1. 在game-demo2首頁新增一個按鈕draw_game，按下後會導入到draw_game頁面
2. draw_game頁面版面可參考fight_move_game.html與devour_game.html
3. draw_game頁面中央是一個畫布，右邊是聊天室區域，中央上方有一個資訊區域
4. 資訊區域
   * 開始遊戲按鈕、題目編號INPUT欄位、延長時間按鈕
   * 顯示難易度等級(1~5顆星⭐️)
   * 顯示目前題目提示詞prompts中的提示詞
   * 有一個秒數倒數的區域
5. 畫布區域
   * 使用者可以在畫布上進行繪圖
   * 畫布上方有清除按鈕，可以清除畫布內容
   * 畫布基本功能至少要有橡皮擦、筆刷顏色選擇、筆刷粗細選擇
   * 要有下載功能按下後產出png圖片下載到使用者電腦
6. 聊天室區域，可比照fight_move_game.html與devour_game.html

### 邏輯需求
1. 在draw_game頁面中，使用webSocket連線到後端LinebotGame1模組
2. 在draw_game頁面中，有一個開始按鈕與一個INPUTM用來輸入題目編號
   1. 按下開始按鈕後，呼叫LinebotGame1模組的GameController.getDrawGameTopic api，並傳入題目編號
   2. api回傳格式參考ApiGameResponse、與DrawGameModel
   3. api範例與說明
      ```json
      {
      "success": true,
      "message": "成功",
      "data": {
      "answer": "貓",
      "prompts": [
      "一個字",
      "動物"
      ],
      "level": 2,
      "id": "1"
      }
      ```
      answer為答案，prompts為提示詞陣列，level為難度等級，id為題目編號
   4. prompts至少有一個提示詞，一開始就第一個prompts顯示在draw_game頁面上方
   5. level用來顯示難易度1 顯示一顆星 ⭐️依此類推，最高5顆星

3. 在draw_game頁面中，當收到socketMessageResponse時，根據socketMessageResponse.game.type進行不同的處理
   * 當type為GUESS時，表示是使用者猜答案的訊息，比對socketMessageResponse.game.text
     * 如果玩家答錯，顯示「{抓玩家名稱}猜{text}！❌答錯啦~(將錯誤答案顯示在聊天室)」在聊天室上
     * 如果玩家答對，顯示「{抓玩家名稱}答對了！💯(不要顯示正確答案)！」在聊天室上
     * 如果玩家答對，要記錄答題的時間秒數，用來排名。
     * 如果玩家答錯，要記錄答錯次，當遊戲結束後，每答錯一次秒數延長5秒
   * 當type為MESSAGE時，顯示使用者發送的訊息在聊天室上
4. 延長時間按鈕，按下後倒數時間+15秒。
5. 時間每經過20秒，就在提示區依序多顯示一個prompts提示詞，直到提示詞顯示完為止
