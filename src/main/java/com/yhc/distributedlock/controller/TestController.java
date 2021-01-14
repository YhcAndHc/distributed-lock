package com.yhc.distributedlock.controller;

import com.yhc.distributedlock.annotation.RedisLock;
import com.yhc.distributedlock.plugin.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;

@RestController
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    private RedisService redisService;

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    //    @RedisLock(key = "test", expire = 10)
    @RequestMapping("/test")
    public boolean test(@RequestParam String key, @RequestParam String value, @RequestParam long expireTime) {
        log.info("# request begin");
        try {
//            Thread.sleep(60000);
            boolean result1 = redisService.setnx(key, value, expireTime);
            boolean result2 = redisService.delete(key);
            long result3 = redisService.getExpire(key);
            log.info("# {},{},{}", result1, result2, result3);
            return redisService.setnx(key, value, expireTime);
        } catch (Exception e) {
            log.error("# request fail , ", e);
        }
        log.info("# request end");
        return false;
    }
}
