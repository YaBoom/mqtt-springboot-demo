package mqtt.zhu.demo.service;

/**
 * @className: MqttPublisherService
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/17 9:53
 */
/**
 * @author wp
 * @Description:  MQTT服务接口 提供MQTT消息发布和订阅功能
 * @date 2025-09-05 9:48
 */
public interface MqttService {
    /**
     * 发布消息到指定主题
     * @param topic 主题
     * @param payload 消息内容
     * @param qos 服务质量等级 (0,1,2)
     * @param retained 是否保留消息
     */
    boolean publish(String topic, String payload, int qos, boolean retained);

    /**
     * 发布消息到指定主题（默认配置QoS，不保留）
     * @param topic 主题
     * @param payload 消息内容
     */
    boolean publish(String topic, String payload);

    /**
     * 订阅指定主题
     * @param topic 主题，支持通配符
     * @param qos 服务质量等级
     */
    void subscribe(String topic, int qos);

    /**
     * 取消订阅指定主题
     * @param topic 主题
     */
    void unsubscribe(String topic);

    /**
     * 检查MQTT客户端是否已连接
     * @return 连接状态
     */
    boolean isConnected();

    /**
     * 断开MQTT连接
     */
    void disconnect();

    /**
     * 重新连接MQTT代理
     */
    void reconnect();
}


