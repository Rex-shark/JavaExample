package com.rex.linebotgame1.service;

import com.rex.linebotgame1.entity.TistUser;
import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import com.rex.linebotgame1.repository.TistUserRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class TistUserService {

    @Resource
    private TistUserRepository tistUserRepository;

    @Resource
    LineBotApiService lineBotApiService;

    public Optional<LineBotUserModel> findUserByMessageContext(MessageContext ctx) {

        if (ctx == null){
            return Optional.empty();
        }
        String lineId = ctx.getUserId();
        if (lineId == null) {
            return Optional.empty();
        }
        return tistUserRepository.findByLineId(lineId)
                .map(LineBotUserModel::new);

    }
    public String registerTistUser(MessageContext ctx){
        String t = ctx.getText().trim();
        //驗證T的格式 必須是-註冊 然後一個空格 中間一個字串 再空格 在一個字串
        //例如: -註冊 T400 2400550，驗證通過並回傳TistUserModel物件，否則回傳null
        String[] parts = t.split(" ");
        if (parts.length != 3) {
            return "註冊格式錯誤!必須是-註冊 TIST員工編號 精誠員工編號 ";
        }

        String tistId = parts[1];
        String systexId = parts[2];
        //檢查是否在建檔庫中有此人
        Optional<TistUser> user = tistUserRepository.findByTistIdAndSystexId(tistId.toUpperCase(), systexId.toUpperCase());

        if (user.isEmpty()) {
            System.out.println("非資料庫內人員，無法註冊！");
            return "查無此資料！";
        }
        Optional<LineBotUserModel> lineBotUserModel =  lineBotApiService.getLintBotUser(ctx); // 呼叫 API 取得最新使用者資料

        if (lineBotUserModel.isEmpty()) {
            System.out.println("line API 查無資料！");
            return "查無此資料！";
        }
        user.get().setLineId(lineBotUserModel.get().getLineUserId());
        user.get().setNickname(lineBotUserModel.get().getNickname());
        user.get().setImageUrl(lineBotUserModel.get().getImageUrl());
        tistUserRepository.save(user.get());
        return  user.get().getName()  +"，註冊成功！感謝您的參與！" ;

    }
}
