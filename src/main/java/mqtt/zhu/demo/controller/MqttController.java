package mqtt.zhu.demo.controller;

import mqtt.zhu.demo.service.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @className: MqttController
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/11/17 11:09
 */
@RestController
@RequestMapping("/mqtt")
public class MqttController {

    @Autowired
    private MqttService mqttService;

    @PostMapping("/publish")
    public String publishMessage(
            @RequestParam String topic,
            @RequestParam String message) {

        boolean result = mqttService.publish(topic, message);
        return result ? "消息发布成功" : "消息发布失败";
    }
}

