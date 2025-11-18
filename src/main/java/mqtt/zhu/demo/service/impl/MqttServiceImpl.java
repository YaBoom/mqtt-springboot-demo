package mqtt.zhu.demo.service.impl;

/**
 * @className: MqttServiceImpl
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/17 11:08
 */

import lombok.extern.slf4j.Slf4j;
import mqtt.zhu.demo.config.MqttProperties;
import mqtt.zhu.demo.service.MqttService;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author wp
 * @Description: MQTT服务实现类
 * @date 2025-09-05 9:48
 */
@Slf4j
@Service
public class MqttServiceImpl implements MqttService {

    @Autowired
    private MqttProperties mqttProperties;

    private final IMqttAsyncClient mqttClient;

    @Autowired
    @Lazy
    public MqttServiceImpl(IMqttAsyncClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    @Override
    public boolean publish(String topic, String payload, int qos, boolean retained) {
        boolean flag = false;
        try {
            if (isConnected()) {
                mqttClient.publish(topic, payload.getBytes(), qos, retained);
                log.debug("已发布消息到主题 {}: {}", topic, payload);
                flag = true;
            } else {
                log.warn("MQTT客户端未连接，无法发布消息");
            }
        } catch (MqttException e) {
            log.error("发布消息到主题 {} 失败", topic, e);
        }
        return flag;
    }

    @Override
    public boolean publish(String topic, String payload) {
        boolean flag = publish(topic, payload, mqttProperties.getDefaultQos(), false);
        return flag;
    }

    @Override
    public void subscribe(String topic, int qos) {
        try {
            if (isConnected()) {
                mqttClient.subscribe(topic, qos);
                log.info("已订阅主题: {}", topic);
            } else {
                log.warn("MQTT客户端未连接，无法订阅主题");
            }
        } catch (MqttException e) {
            log.error("订阅主题 {} 失败", topic, e);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        try {
            if (isConnected()) {
                mqttClient.unsubscribe(topic);
                log.info("已取消订阅主题: {}", topic);
            } else {
                log.warn("MQTT客户端未连接，无法取消订阅主题");
            }
        } catch (MqttException e) {
            log.error("取消订阅主题 {} 失败", topic, e);
        }
    }

    @Override
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    @Override
    public void disconnect() {
        try {
            if (isConnected()) {
                mqttClient.disconnect();
                log.info("已断开MQTT连接");
            }
        } catch (MqttException e) {
            log.error("断开MQTT连接失败", e);
        }
    }

    @Override
    public void reconnect() {
        try {
            if (!isConnected()) {
                mqttClient.reconnect();
                log.info("正在重新连接MQTT代理...");
            }
        } catch (MqttException e) {
            log.error("重新连接MQTT代理失败", e);
        }
    }
}


