1. 安裝
  - winget install Microsoft.devtunnel
2. 登入github帳號
  - devtunnel user login -g
3. 建立隧道(單一)，要多個可以多開terminal視窗
  -  devtunnel host -p 8080 --allow-anonymous
  -  devtunnel host -p 3000 --allow-anonymous


### 補充
- 更新 devtunnel
```
winget upgrade Microsoft.devtunnel
```
