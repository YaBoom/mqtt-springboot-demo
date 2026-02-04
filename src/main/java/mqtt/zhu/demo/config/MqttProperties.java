package mqtt.zhu.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @className: MqttProperties
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/17 10:55
 */
@Data
@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    /**
     * MQTT代理服务器地址
     */
    private String brokerUrl;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 网关设备 ID（物联网接入服务 ID）
     */
    private String interfaceDeviceId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 连接超时时间（秒）
     */
    private Integer connectionTimeout;

    /**
     * 保持活动间隔（秒）
     */
    private Integer keepAliveInterval;

    /**
     * 重连延迟时间（毫秒）
     */
    private Integer reconnectDelay;

    /**
     * 是否清除会话
     */
    private Boolean cleanSession;

    /**
     * 最大飞行消息数
     */
    private Integer maxInflight;

    /**
     * 默认主题
     */
    private String defaultTopic;

    /**
     * 默认服务质量等级
     */
    private Integer defaultQos;

    /**
     * 是否启用自定义重连机制
     */
    private boolean enableCustomReconnect;

    /**
     * 最大重试次数（0表示无限重试）
     */
    private int maxReconnectAttempts;

    /**
     * 初始重连延迟（毫秒）
     */
    private int initialReconnectDelay;

    /**
     * 最大重连延迟（毫秒）
     */
    private int maxReconnectDelay;

    /**
     * 重连延迟倍增因子
     */
    private double reconnectBackoffMultiplier;

    /**
     * mqtt上报信息物模型标识前缀 测试环境：trailer_watch_model 正式环境：'0202001'
     */
    private String physicalModel;

    /**
     * 主题配置
     */
    private Topic topic = new Topic();

    /**
     * 主题配置类
     */
    @Data
    public static class Topic {
        /**
         * 发送接收固定topic前缀
         */
        private String prefix;

        /**
         * 发送者主题配置
         */
        private Sender sender = new Sender();

        /**
         * 接收者主题配置
         */
        private Receiver receiver = new Receiver();

        /**
         * 发送者主题配置类
         */
        @Data
        public static class Sender {
            /**
             * 位置上报
             */
            private String tpos;

            /**
             * 巡检上报
             */
            private String tstatus;

            /**
             * 指令响应上报(首次响应)
             */
            private String instructResp;

            /**
             * 指令执行结果事件
             */
            private String instructResult;

            /**
             * 连接情况上报 connection_agent.online 为上线；connection_agent.offline 为下线
             */
            private String connectionAgent;
        }

        /**
         * 接收者主题配置类
         */
        @Data
        public static class Receiver {
            /**
             * 是否开启集群部署：开启后拼接前缀【只在订阅前添加前缀】
             */
            private Boolean doShare;

            /**
             * 做集群部署时，按照规则增加前缀：$share/{deviceId}  deviceId为网关设备 ID（物联网接入服务 ID）
             * 例如：$share/25352d632800/$iot/v1/device/25352d632800/connection_agent/functions/call
             */
            private String share;

            /**
             * 原先单体部署时使用前缀
             */
            private String prefix;

            /**
             * 指令接收主题
             */
            private String instructrceive;
        }
    }
}
