package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@EnableJpaAuditing
@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}
	@Bean
	public AuditorAware<String> auditorProvider() {
		// 실제로는 세션에서 유저 정보등을 꺼내쓴다. 혹은 시스템 정보
		return () -> Optional.of(UUID.randomUUID().toString());
	}
}
