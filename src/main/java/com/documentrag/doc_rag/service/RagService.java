package com.documentrag.doc_rag.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.documentrag.doc_rag.repository.DocumentChunkProjection;

@Service
public class RagService {

	private final WebClient lmStudioClient;
	private final String model;
	private final double temperature;
	private final int maxTokens;

	public RagService(WebClient lmStudioClient, @Value("${lmstudio.model:}") String model,
			@Value("${lmstudio.temperature:0.2}") double temperature,
			@Value("${lmstudio.max-tokens:512}") int maxTokens) {
		this.lmStudioClient = lmStudioClient;
		this.model = model;
		this.temperature = temperature;
		this.maxTokens = maxTokens;
	}

	public String answerWithRag(String question, List<DocumentChunkProjection> chunks) {
		String context = buildContext(chunks, 6000);
		String prompt = """
				You are a helpful assistant. Use ONLY the provided context to answer.

				Question:
				%s

				Context:
				%s

				Instructions:

				Be concise and factual.
				If relevant, cite docId and chunkIndex from the context.
				""".formatted(question, context);
		Map<String, Object> payload = buildPayload(prompt);

		Map<String, Object> resp = lmStudioClient.post().uri("/v1/chat/completions") // baseUrl should be
																						// http://localhost:1234
				.contentType(MediaType.APPLICATION_JSON).bodyValue(payload).retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
				}).block();

		String answer = extractAnswer(resp);
		return (answer == null || answer.isBlank()) ? "No answer from model." : answer;
	}

	private Map<String, Object> buildPayload(String prompt) {
		Map<String, Object> payload = new HashMap<>();
		if (model != null && !model.isBlank()) {
			payload.put("model", model);
		}
		payload.put("temperature", temperature);
		payload.put("max_tokens", maxTokens);
		payload.put("messages", List.of(Map.of("role", "system", "content", "You are a helpful assistant."),
				Map.of("role", "user", "content", prompt)));
		return payload;
	}

	private String buildContext(List<DocumentChunkProjection> chunks, int maxChars) {
		String joined = chunks.stream()
				.map(c -> "docId=" + c.getDocumentId() + " chunkIndex=" + c.getChunkIndex() + "\n" + c.getContent())
				.collect(Collectors.joining("\n---\n"));
		return joined.length() <= maxChars ? joined : joined.substring(0, maxChars);
	}

	@SuppressWarnings("unchecked")
	private String extractAnswer(Map<String, Object> resp) {
		if (resp == null)
			return null;

		Object choicesObj = resp.get("choices");
		if (!(choicesObj instanceof List))
			return null;
		List<?> choicesList = (List<?>) choicesObj;
		if (choicesList.isEmpty())
			return null;

		Object firstChoice = choicesList.get(0);
		if (!(firstChoice instanceof Map))
			return null;
		Map<String, Object> firstChoiceMap = (Map<String, Object>) firstChoice;

		Object messageObj = firstChoiceMap.get("message");
		if (!(messageObj instanceof Map))
			return null;
		Map<String, Object> messageMap = (Map<String, Object>) messageObj;

		Object contentObj = messageMap.get("content");
		return (contentObj instanceof String) ? (String) contentObj : null;
	}
}