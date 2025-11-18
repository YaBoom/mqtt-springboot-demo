package mqtt.zhu.demo.config;

/**
 * @className: MqttGetFIle
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/17 11:06
 */
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author wp
 * @Description: 获取ca认证文件
 * @date 2025-09-04 16:07
 */
@Slf4j
@Component
public class MqttGetFIle {

    @Autowired
    private MqttProperties mqttProperties;

    public InputStream getSslCaFile() {
        try {
            String path = getFilePath("ca.crt");
            return getInputStream(path);
        } catch (Exception e) {
            log.error("读取ca证书出现异常：{}", e.getMessage(), e);
        }
        return null;
    }

    public InputStream getSslCertFile() {
        try {
            String path = getFilePath(mqttProperties.getClientId() + ".crt");
            return getInputStream(path);
        } catch (Exception e) {
            log.error("读取客户端证书出现异常：{}", e.getMessage(), e);
        }
        return null;
    }

    public InputStream getSslKeyFile() {
        try {
            String path = getFilePath(mqttProperties.getClientId() + ".key");
            return getInputStream(path);
        } catch (Exception e) {
            log.error("读取私钥出现异常：{}", e.getMessage(), e);
        }
        return null;
    }

    private String getFilePath(String fileName) {
        return String.format("ssl/%s/" + fileName, mqttProperties.getClientId());
    }

    private InputStream getInputStream(String path) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(path);
        if (classPathResource.exists()) {
            return classPathResource.getInputStream();
        }
        return new FileInputStream(new File(applicationHome.getDir(), path));
    }

    private ApplicationHome applicationHome = new ApplicationHome(getClass());
}

