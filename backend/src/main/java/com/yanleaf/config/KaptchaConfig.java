package com.yanleaf.config;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {
   @Bean
   public Producer kaptchaProducer() {
       Properties properties = new Properties();
       properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "150");
       properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "50");
       properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
       Config config = new Config(properties);
       return config.getProducerImpl();
   }
}