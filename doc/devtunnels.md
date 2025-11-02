1. 安裝
  - winget install Microsoft.devtunnel
2. 登入github帳號
  - devtunnel user login -g
3. 建立隧道(單一)
  -  devtunnel host -p 8080 --allow-anonymous
4. 建立隧道(多組) 沒測試過!
    - devtunnel create mytunnel --ports 8080,5173 --allow-anonymous
    - devtunnel host mytunnel

    
### 補充
- 更新 devtunnel
```
winget upgrade Microsoft.devtunnel
```
