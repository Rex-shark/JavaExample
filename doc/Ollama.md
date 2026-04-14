###   本地語言模型

1. 官網下載鏡像
2. 如果要使用GPU，先確認顯示卡，CMD 執行 nvidia-smi
3. 啟動容器 
    ``` PowerShell
    # CPU
    docker run -d -v D:\docker\AI_Models:/root/.ollama -p 11434:11434 --name ollama-c ollama/ollama
    # GPU (NVIDIA)
    docker run -d --gpus all -v D:\docker\AI_Models:/root/.ollama -p 11434:11434 --name ollama-c ollama/ollama
    ```
4. 下載並執行模型
    ```PowerShell
    # 指定的模型 -> mgema3:12b (這個要docker記憶體9G以上)
    docker exec -it ollama-c ollama run gemma3:12b
    # 指定的模型 -> gemma3:
    docker exec -it ollama-c ollama run gemma3
    # 指定的模型 -> gemma3:27b: (4070跑不了)
    docker exec -it ollama-c ollama run gemma3:27b
    docker exec -it ollama-c ollama pull hf.co/mradermacher/gemma-4-E2B-it-heretic-GGUF
    docker exec -it ollama-c ollama pull hf.comradermacher/gemma-4-E4B-it-ultra-uncensored-heretic-i1-GGUF
    docker exec -it ollama-c ollama run  embeddinggemma:300m
    ```
