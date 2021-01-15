package com.yhc.distributedlock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		// 我们为了自己开发方便，一般直接使用 <String, Object>
		RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(factory);

		RedisSerializer<?> rs = new StringRedisSerializer();
		template.setKeySerializer(rs);
		template.setHashKeySerializer(rs);
		template.setValueSerializer(rs);
		template.setHashValueSerializer(rs);
		template.afterPropertiesSet();
		return template;
	}
}
