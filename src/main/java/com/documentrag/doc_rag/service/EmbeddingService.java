package com.documentrag.doc_rag.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmbeddingService {

    private static final String LMSTUDIO_URL = "http://127.0.0.1:1234/v1/embeddings";

    private final RestTemplate restTemplate;

    public EmbeddingService() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings("unchecked")
    public List<Double> getEmbedding(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "text-embedding-nomic-embed-text-v2-moe");
        requestBody.put("input", text);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> response = restTemplate.postForObject(
                LMSTUDIO_URL,
                entity,
                Map.class
        );

        if (response == null) {
            return Collections.emptyList();
        }

        // Parse response: { "data": [ { "embedding": [ ... ] } ] }
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data != null && !data.isEmpty()) {
            Object embeddingObj = data.get(0).get("embedding");
            if (embeddingObj instanceof List) {
                return (List<Double>) embeddingObj;
            }
        }

        return Collections.emptyList();
    }
}