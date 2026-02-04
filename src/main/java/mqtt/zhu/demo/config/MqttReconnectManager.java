package mqtt.zhu.demo.config;

/**
 * @className: MqttReconnectManager
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/17 11:06
 */

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wp
 * @Description: MQTT重连管理器  负责处理连接丢失后的自动重连
 * @date 2025-09-05 13:20
 */
@Slf4j
@Component
public class MqttReconnectManager {

    @Autowired
    private MqttProperties mqttProperties;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private final AtomicInteger currentDelay = new AtomicInteger(0);
    private final AtomicReference<IMqttAsyncClient> clientRef = new AtomicReference<>();
    private final AtomicReference<MqttConnectOptions> optionsRef = new AtomicReference<>();

    private volatile boolean isReconnecting = false;
    private volatile boolean isShutdown = false;


    public MqttReconnectManager() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "MQTT-Reconnect-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 初始化重连管理器
     */
    public void initialize(IMqttAsyncClient client, MqttConnectOptions options) {
        clientRef.set(client);
        optionsRef.set(options);
        currentDelay.set(mqttProperties.getInitialReconnectDelay());
    }

    /**
     * 触发重连
     */
    public void triggerReconnect() {
        if (isShutdown || isReconnecting || !mqttProperties.isEnableCustomReconnect()) {
            return;
        }

        synchronized (this) {
            if (isReconnecting) {
                return;
            }
            isReconnecting = true;
        }

        scheduler.execute(this::doReconnect);
    }

    /**
     * 执行重连操作
     */
    private void doReconnect() {
        IMqttAsyncClient client = clientRef.get();
        MqttConnectOptions options = optionsRef.get();

        if (client == null || options == null || isShutdown) {
            isReconnecting = false;
            return;
        }

        try {
            // 检查最大重试次数
            if (mqttProperties.getMaxReconnectAttempts() > 0 &&
                    reconnectAttempts.get() >= mqttProperties.getMaxReconnectAttempts()) {
                log.warn("已达到最大重连次数({})，停止重连", mqttProperties.getMaxReconnectAttempts());
                isReconnecting = false;
                return;
            }

            log.info("尝试第{}次重连...", reconnectAttempts.incrementAndGet() + 1);

            if (client.isConnected()) {
                log.info("客户端已连接，无需重连");
                resetReconnectState();
                return;
            }

            // 尝试连接
            client.connect(options).waitForCompletion();
            log.info("重连成功！");
            // 重连成功后重新订阅主题
            subscribe(client);

            resetReconnectState();

        } catch (Exception e) {
            log.warn("重连失败，{}秒后再次尝试", currentDelay.get() / 1000);

            // 计算下一次重连延迟（使用退避算法）
            int nextDelay = (int) (currentDelay.get() * mqttProperties.getReconnectBackoffMultiplier());
            nextDelay = Math.min(nextDelay, mqttProperties.getMaxReconnectDelay());
            currentDelay.set(nextDelay);

            // 调度下一次重连
            if (!isShutdown) {
                scheduler.schedule(this::doReconnect, currentDelay.get(), TimeUnit.MILLISECONDS);
            }
        } finally {
            isReconnecting = false;
        }
    }

    public void subscribe(IMqttAsyncClient client) {
        try {
            List<String> topics = new ArrayList<>();
            if (mqttProperties.getTopic().getReceiver().getDoShare()) {
                // $share/25352d632800/$iot/v1/device/25352d632800/connection_agent/functions/call
                topics.add(mqttProperties.getTopic().getReceiver().getShare() + mqttProperties.getInterfaceDeviceId() + "/" + mqttProperties.getTopic().getPrefix() + mqttProperties.getInterfaceDeviceId() + mqttProperties.getTopic().getReceiver().getInstructrceive());
            } else {
                // $iot/v1/device/{网关设备 id}/connection_agent/functions/call
                topics.add(mqttProperties.getTopic().getPrefix() + mqttProperties.getInterfaceDeviceId() + mqttProperties.getTopic().getReceiver().getInstructrceive());
            }
            // 对mqtt下行主题进行订阅
            for (String topic : topics) {
                client.subscribe(topic, 1);
                log.info("MQTT主题订阅成功：topic --> {}", topic);
            }
        } catch (MqttException e) {
            log.error("MQTT主题订阅失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 重置重连状态
     */
    private void resetReconnectState() {
        reconnectAttempts.set(0);
        currentDelay.set(mqttProperties.getInitialReconnectDelay());
        isReconnecting = false;
    }

    /**
     * 停止重连管理器
     */
    @PreDestroy
    public void shutdown() {
        isShutdown = true;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("MQTT重连管理器已关闭");
    }

    /**
     * 获取当前重连状态
     */
    public boolean isReconnecting() {
        return isReconnecting;
    }

    /**
     * 获取重连尝试次数
     */
    public int getReconnectAttempts() {
        return reconnectAttempts.get();
    }
}

