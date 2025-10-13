package com.babymate.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig
 * 
 * <p>
 * 此設定類別用來建立 Redis 連線工廠與 RedisTemplate。
 * </p>
 * 
 * <ul>
 * <li>使用 {@link LettuceConnectionFactory} 作為 Redis 連線驅動。</li>
 * <li>設定 key 與 value 的序列化方式，以確保資料可正確儲存與讀取。</li>
 * <li>value 採用 {@link GenericJackson2JsonRedisSerializer} 將物件序列化為 JSON。</li>
 * </ul>
 */
@Configuration
public class RedisConfig {

	private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
	
	/**
	 * 建立 Redis 連線工廠 (使用 Lettuce)
	 * 
	 * @return Redis 連線工廠實例
	 */
	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		// 使用預設設定 (可於 application.properties 設定 host、port、password)
		LettuceConnectionFactory factory = new LettuceConnectionFactory();
		logger.info("RedisConnectionFactory 初始化完成");
		
		return factory;
	}

	/**
	 * 建立 RedisTemplate，提供 Spring 操作 Redis 的高階封裝。
	 * 
	 * @param connectionFactory Redis 連線工廠
	 * @return 配置完成的 RedisTemplate 實例
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// 設定 key 為 String 序列化器
		template.setKeySerializer(new StringRedisSerializer());
		// 設定 value 為 JSON 序列化器
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		// 設定 Hash key/value 的序列化方式
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

		// 初始化設定
		template.afterPropertiesSet();
		logger.info("Redis 已連線至主機：{}:{}", connectionFactory.getHostName(), connectionFactory.getPort());
		logger.info("RedisTemplate 已建立完成，Key 使用 String，Value 使用 JSON");
		return template;
	}
}
