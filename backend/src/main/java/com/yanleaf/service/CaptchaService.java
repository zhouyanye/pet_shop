package com.yanleaf.service;

import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    @Autowired
    private Producer kaptchaProducer;

    // 内存存储：uuid -> 验证码文本
    private final Map<String, String> store = new ConcurrentHashMap<>();
    // 记录过期时间
    private final Map<String, Long> expire = new ConcurrentHashMap<>();
    private static final long TTL = 3 * 60 * 1000L; // 3分钟

    /** 生成验证码，返回 { uuid, imageBase64 } */
    public Map<String, String> generate() {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        String uuid = UUID.randomUUID().toString();
        store.put(uuid, text);
        expire.put(uuid, System.currentTimeMillis() + TTL);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            String base64 = "data:image/png;base64," +
                    Base64Utils.encodeToString(os.toByteArray());
            return Map.of("uuid", uuid, "image", base64);
        } catch (Exception e) {
            throw new RuntimeException("验证码生成失败", e);
        }
    }

    /** 校验后删除 */
    public boolean verify(String uuid, String input) {
        if (uuid == null || input == null) return false;
        Long exp = expire.get(uuid);
        String code = store.get(uuid);
        // 清理
        store.remove(uuid);
        expire.remove(uuid);
        if (code == null || exp == null) return false;
        if (System.currentTimeMillis() > exp) return false;
        return code.equalsIgnoreCase(input);
    }
}