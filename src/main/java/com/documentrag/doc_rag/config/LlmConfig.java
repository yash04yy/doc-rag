package com.documentrag.doc_rag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LlmConfig {

	@Bean
	public WebClient lmStudioClient(@Value("${lmstudio.base-url}") String baseUrl,
			@Value("${lmstudio.api-key}") String apiKey) {
		return WebClient.builder().baseUrl(baseUrl) // LM Studio API server
				.defaultHeader("Authorization",  "Bearer " + apiKey) // placeholder OK
				.build();
	}
}
