package com.github.listen_to_me.common.config;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class AlipayConfig {
    @Value("${alipay.appId}")
    private String appId;
    @Value("${alipay.privateKey}")
    private String privateKey;
    @Value("${alipay.publicKey}")
    private String publicKey;
    @Value("${alipay.notifyUrl}")
    private String notifyUrl;
    @Value("${alipay.gatewayUrl}")
    private String gatewayUrl;

    // 项目启动时初始化支付宝配置
    @PostConstruct
    public void init() {
        Config config = new Config();
        config.protocol = "https";
        config.gatewayHost = gatewayUrl.replace("https://", "").replace("/gateway.do", "");
        config.signType = "RSA2";
        config.appId = appId;
        config.merchantPrivateKey = privateKey;
        config.alipayPublicKey = publicKey;
        config.notifyUrl = notifyUrl;
        Factory.setOptions(config);
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }
}