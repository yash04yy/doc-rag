package com.documentrag.doc_rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmbeddingService {
	private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

	private final String embeddingsUrl;
	private final RestTemplate restTemplate = new RestTemplate();
	private final String model;

	public EmbeddingService(@Value("${embedding.api.url}") String embeddingsUrl,
			@Value("${embedding.api.model}") String model, @Value("${lmstudio.api-key:}") String apiKey) {
		this.embeddingsUrl = embeddingsUrl;
		this.model = model;
	}

	@SuppressWarnings("unchecked")
	public List<Double> getEmbedding(String text) {
		log.info("Getting embedding for text ({} chars) using model '{}' at URL '{}'", text.length(), model, embeddingsUrl);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", model);
		requestBody.put("input", text);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

		Map<String, Object> response = null;
		try {
			response = restTemplate.postForObject(embeddingsUrl, entity, Map.class);
			log.debug("Received embedding API response: {}", response);
		} catch (RestClientException e) {
			log.error("Error posting to embedding API: {}", e.getMessage(), e);
			return Collections.emptyList();
		}

		if (response == null) {
			log.warn("Embedding API returned null response");
			return Collections.emptyList();
		}

		List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
		if (data != null && !data.isEmpty()) {
			Object embeddingObj = data.get(0).get("embedding");
			if (embeddingObj instanceof List) {
				log.info("Successfully retrieved embedding ({} dimensions)", ((List<?>) embeddingObj).size());
				return (List<Double>) embeddingObj;
			} else {
				log.warn("Embedding object is missing or not a list");
			}
		} else {
			log.warn("Embedding API response has empty or missing 'data'");
		}

		return Collections.emptyList();
	}
}
