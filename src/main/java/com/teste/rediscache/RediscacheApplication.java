package com.teste.rediscache;

import com.teste.rediscache.config.EventStoreProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableConfigurationProperties(EventStoreProperties.class)
public class RediscacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(RediscacheApplication.class, args);
	}

}
