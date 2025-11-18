package mqtt.zhu.demo.handel;

/**
 * @className: MqttCallbackHandler
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/18 9:22
 */

import lombok.extern.slf4j.Slf4j;
import mqtt.zhu.demo.config.MqttReceiveMsgHandler;
import mqtt.zhu.demo.config.MqttReconnectManager;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author wp
 * @Description: MQTT回调处理器 处理连接丢失、消息到达和交付完成事件
 * @date 2025-09-05 9:51
 */
@Slf4j
@Component
public class MqttCallbackHandler implements MqttCallback {

    @Autowired
    private MqttReconnectManager reconnectManager;

    @Autowired
    private MqttReceiveMsgHandler receiveMsgHandler;

    /**
     * 当连接丢失时调用
     */
    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT连接丢失", cause);
        // 触发自定义重连机制
        reconnectManager.triggerReconnect();
    }

    /**
     * 当消息到达时调用
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.info("收到MQTT消息 - 主题: {}, QoS: {}, 内容: {}", topic, message.getQos(), payload);
        if (StringUtils.isEmpty(payload)) {
            return;
        }
        receiveMsgHandler.handler(payload);
    }

    /**
     * 当消息交付完成时调用
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            log.debug("消息交付完成 - 主题: {}", token.getTopics() != null ? token.getTopics()[0] : "未知");
        } catch (Exception e) {
            log.error("获取交付完成消息主题失败", e);
        }
    }
}

