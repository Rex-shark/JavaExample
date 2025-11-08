package com.rex.linebotgame1.service;

import com.rex.linebotgame1.entity.TistUser;
import com.rex.linebotgame1.model.LineBotUserModel;
import com.rex.linebotgame1.model.MessageContext;
import com.rex.linebotgame1.repository.TistUserRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 使用者註冊TistUser
     * @param ctx
     * @return
     */
    @Transactional
    public String registerTistUser(MessageContext ctx){
        String t = ctx.getText().trim();
        //驗證T的格式 必須是-註冊 然後一個空格 中間一個字串 再空格 在一個字串
        //例如: -註冊 T400 2400550，驗證通過並回傳TistUserModel物件，否則回傳null
        String[] parts = t.split(" ");
        if (parts.length != 3) {
            return "註冊格式錯誤❌必須是-註冊 TIST員工編號 精誠員工編號 ";
        }

        String tistId = parts[1];
        String systexId = parts[2];
        //檢查是否在建檔庫中有此人
        Optional<TistUser> userOpt = tistUserRepository.findByTistIdAndSystexId(tistId.toUpperCase(), systexId.toUpperCase());

        if (userOpt.isEmpty()) {
            System.out.println("非資料庫內人員，無法註冊！");
            return null;//不回覆註冊訊息，避免暴力測試
        }

        //符合資料庫內人員，呼叫 line API 取得最新使用者資料
        Optional<LineBotUserModel> lineBotUserModel =  lineBotApiService.getLintBotUser(ctx); // 呼叫 API 取得最新使用者資料

        if (lineBotUserModel.isEmpty()) {
            System.out.println("line API 查無資料！");
            return "請先加入好友，如有任何疑問，歡迎聯絡尾牙小組！" ;
        }
        TistUser user = userOpt.get();
        if (user.isRegister()) {
            //已註冊過，判斷lineId是否相同，相同就更新URL跟暱稱，不同顯示該帳號已註冊
            if (user.getLineId().equals(lineBotUserModel.get().getLineUserId())) {
                user.setNickname(lineBotUserModel.get().getNickname());//TODO Rex 考慮是否要覆蓋暱稱
                user.setImageUrl(lineBotUserModel.get().getImageUrl());
                tistUserRepository.save(user);
                return  user.getName()  +"，資料已更新❗" ;
            }else{
                return "此line帳號已註冊！如有任何疑問，歡迎聯絡尾牙小組❗" ;
            }
        }
        user.setLineId(lineBotUserModel.get().getLineUserId());
        user.setNickname(lineBotUserModel.get().getNickname());//TODO Rex 考慮是否要覆蓋暱稱
        user.setImageUrl(lineBotUserModel.get().getImageUrl());
        user.setRegister(true);
        tistUserRepository.save(user);
        return  user.getUnitName()+"-" +  user.getName()  +"，註冊成功❗感謝您的參與。" ;

    }
}
