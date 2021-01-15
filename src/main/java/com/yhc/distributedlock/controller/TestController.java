package com.yhc.distributedlock.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yhc.distributedlock.annotation.RedisLock;
import com.yhc.distributedlock.plugin.RedisService;

@RestController
public class TestController {

	private static final Logger log = LoggerFactory.getLogger(TestController.class);

	private RedisService redisService;

	@Autowired
	public void setRedisService(RedisService redisService) {
		this.redisService = redisService;
	}

	@RedisLock(key = "test", expire = 10)
	@RequestMapping("/test")
	public boolean test(@RequestParam String key, @RequestParam long threadSleepTime) {
		log.info("# request begin");
		try {
			Thread.sleep(threadSleepTime);
			return redisService.set(key, UUID.randomUUID().toString());
		} catch (Exception e) {
			log.error("# request fail , ", e);
		}
		log.info("# request end");
		return false;
	}
}
