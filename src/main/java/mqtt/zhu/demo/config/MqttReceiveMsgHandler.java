package mqtt.zhu.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

/**
 * @className: ReceiveMsgHandler
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/18 9:27
 */
@Slf4j
@Component
public class MqttReceiveMsgHandler {

    public void handler(String payload) {
       log.info("开始业务处理payload>>>>>>>>>>>>>>>>>>>");
    }
}

