package com.rex.linebotgame1.service;

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

    private Optional<LineBotUserModel> findUserByMessageContext(MessageContext ctx) {

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

}
