package com.lhjz.portal.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.lhjz.portal.component.MailSender;
import com.lhjz.portal.entity.Setting;
import com.lhjz.portal.pojo.Enum.SettingType;
import com.lhjz.portal.repository.SettingRepository;
import com.lhjz.portal.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableJpaAuditing
@EnableScheduling
@Slf4j
public class DataInitConfig {

	@Autowired
	SettingRepository settingRepository;

	@Autowired
	MailSender mailSender;

	@PostConstruct
	public void init() {

		log.info("初始化配置邮箱服务 start...");

		Setting setting = settingRepository.findOneBySettingType(SettingType.Mail);

		if (setting != null) {
			Map<?, ?> mailSettings = JsonUtil.json2Object(setting.getContent(), Map.class);
			JavaMailSenderImpl sender = mailSender.getMailSender();
			sender.setHost(String.valueOf(mailSettings.get("host")));
			sender.setPort((int) Double.parseDouble(String.valueOf(mailSettings.get("port"))));
			sender.setUsername(String.valueOf(mailSettings.get("username")));
			sender.setPassword(String.valueOf(mailSettings.get("password")));
			sender.setDefaultEncoding("UTF-8");

			log.info("初始化配置邮箱服务 end...");
		}

	}

}