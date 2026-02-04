package mqtt.zhu.demo.config;

/**
 * @className: MqttConfig
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/17 11:05
 */

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import mqtt.zhu.demo.handel.MqttCallbackHandler;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wp
 * @Description: MQTT配置类，负责创建和配置MQTT客户端
 * @date 2025-09-05 9:35
 */
@Slf4j
@Configuration
public class MqttConfig {

    @Autowired
    private MqttProperties mqttProperties;
    @Autowired
    private MqttReconnectManager reconnectManager;
    @Autowired
    private MqttGetFIle mqttGetFIle;

    /**
     * 创建MQTT连接选项
     */
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttProperties.getBrokerUrl()});
        // 设置超时时间 单位为秒
        options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        options.setKeepAliveInterval(mqttProperties.getKeepAliveInterval());
        // 禁用Paho内置重连，使用自定义重连
        options.setAutomaticReconnect(false);
        // // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
        options.setCleanSession(mqttProperties.getCleanSession());
        options.setMaxInflight(mqttProperties.getMaxInflight());

        if (mqttProperties.getBrokerUrl().startsWith("ssl")) {
            // 加载证书及私钥
            this.configureTls(options);
        } else if (mqttProperties.getBrokerUrl().startsWith("tcp")) {
            // tcp 开头连接为本地开发测试
            options.setUserName(mqttProperties.getUsername());
            options.setPassword(mqttProperties.getPassword().toCharArray());
        }

        return options;
    }


    /**
     * 创建MQTT异步客户端
     */
    @Bean
    public IMqttAsyncClient mqttAsyncClient(MqttConnectOptions options, MqttCallbackHandler callbackHandler) {
        try {
            // 使用内存持久化
            MqttClientPersistence persistence = new MemoryPersistence();

            // 创建客户端
            IMqttAsyncClient client = null;
            try {
                client = new MqttAsyncClient(mqttProperties.getBrokerUrl(), this.dealClientId(), persistence);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                throw new RuntimeException(e);
            }

            // 设置回调处理器
            client.setCallback(callbackHandler);

            reconnectManager.initialize(client, options);

            // 初始连接尝试
            initialConnectWithRetry(client, options);

            return client;
        } catch (Exception e) {
            log.error("创建MQTT客户端失败", e);
            throw new RuntimeException("无法创建MQTT客户端", e);
        }
    }


    /**
     * 初始连接带重试机制
     */
    private void initialConnectWithRetry(IMqttAsyncClient client, MqttConnectOptions options) {
        new Thread(() -> {
            int attempt = 0;
            int delay = mqttProperties.getInitialReconnectDelay();

            while (!client.isConnected() && !Thread.currentThread().isInterrupted()) {
                try {
                    attempt++;
                    log.info("尝试第{}次连接MQTT代理: {}", attempt, mqttProperties.getBrokerUrl());

                    client.connect(options).waitForCompletion();
                    log.info("成功连接到MQTT代理");

                    // 连接成功后订阅指定主题
                    this.subscribe(client);

                    break;
                } catch (Exception e) {
                    log.warn("连接MQTT代理失败，{}秒后重试...", delay / 1000);

                    if (mqttProperties.getMaxReconnectAttempts() > 0 && attempt >= mqttProperties.getMaxReconnectAttempts()) {
                        log.error("已达到最大连接尝试次数({})，停止尝试", mqttProperties.getMaxReconnectAttempts());
                        break;
                    }

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("连接线程被中断", ie);
                        break;
                    }

                    // 计算下一次延迟
                    delay = (int) Math.min(delay * mqttProperties.getReconnectBackoffMultiplier(), mqttProperties.getMaxReconnectDelay());
                }
            }
        }, "MQTT-Initial-Connect").start();
    }


    // {deviceId}-channel-{channelId}
    // deviceId为设备在天枢云上注册的设备ID
    // channelId表示该设备的通道标识, 为整型数字, 从数字0开始, 依次递增(这里采用16为随机数字)
    public String dealClientId() {
        String channelId = RandomUtil.randomString("0123456789", 16);
        return mqttProperties.getInterfaceDeviceId() + "-channel-" + channelId;
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
                client.subscribe(topic, mqttProperties.getDefaultQos());
                log.info("MQTT主题订阅成功：topic --> {}", topic);
            }
        } catch (MqttException e) {
            log.error("MQTT主题订阅失败：{}", e.getMessage(), e);
        }
    }


    private void configureTls(MqttConnectOptions connOpts) {
        try {
            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null);

            // 加载CA证书
            X509Certificate ca = getCertificate(mqttGetFIle.getSslCaFile());
            caKs.setCertificateEntry("ca", ca);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);

            // 加载客户端证书
            X509Certificate cert = getCertificate(mqttGetFIle.getSslCertFile());

            // 加载私钥
            PrivateKey privateKey = getPrivateKey(mqttGetFIle.getSslKeyFile());

            ks.setCertificateEntry("certificate", cert);
            ks.setKeyEntry("private-key", privateKey, new char[]{}, new Certificate[]{cert});

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(caKs);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, "".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            connOpts.setSocketFactory(sslContext.getSocketFactory());
        } catch (Exception ex) {
            log.error("证书加载异常：{}", ex.getMessage(), ex);
        }
    }

    private X509Certificate getCertificate(InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            // 使用Bouncy Castle的PEMParser读取PEM文件
            PEMParser pemParser = new PEMParser(reader);
            Object object = pemParser.readObject();

            if (object instanceof X509CertificateHolder) {
                X509CertificateHolder certificateHolder = (X509CertificateHolder) object;
                Security.addProvider(new BouncyCastleProvider());
                JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
                return converter.getCertificate(certificateHolder);
            } else {
                throw new IllegalArgumentException("不支持的证书格式");
            }
        } catch (Exception ex) {
            log.error("读取证书时出现错误：{}", ex.getMessage(), ex);
        }
        return null;
    }

    private PrivateKey getPrivateKey(InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            // 使用 Bouncy Castle 的 PEMParser 读取 PEM 文件
            PEMParser pemParser = new PEMParser(reader);
            Object object = pemParser.readObject();

            // 确认读取的是 PEMKeyPair 对象
            if (object instanceof PEMKeyPair) {
                PEMKeyPair pemKeyPair = (PEMKeyPair) object;
                PrivateKeyInfo privateKeyInfo =pemKeyPair.getPrivateKeyInfo();

                // 将 PrivateKeyInfo 转换为 PrivateKey
                Security.addProvider(new BouncyCastleProvider());
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
                return converter.getPrivateKey(privateKeyInfo);
            } else {
                throw new IllegalArgumentException("不支持的私钥格式");
            }
        } catch (Exception ex) {
            log.error("读取私钥时出现错误：{}", ex.getMessage(), ex);
        }
        return null;
    }
}

