package com.documentrag.doc_rag.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.documentrag.doc_rag.repository.DocumentChunkProjection;
import com.documentrag.doc_rag.repository.DocumentChunkRepository;
import com.documentrag.doc_rag.service.EmbeddingService;
import com.documentrag.doc_rag.service.RagService;

@RestController
@RequestMapping("/api/query")
public class QueryController {

	private final EmbeddingService embeddingService;
	private final DocumentChunkRepository documentChunkRepository;
	private final RagService ragService;

	public QueryController(EmbeddingService embeddingService, DocumentChunkRepository documentChunkRepository,
			RagService ragService) {
		this.embeddingService = embeddingService;
		this.documentChunkRepository = documentChunkRepository;
		this.ragService = ragService;
	}

	@GetMapping
	public Map<String, Object> query(@RequestParam String q, @RequestParam(defaultValue = "5") int k) {
		List<? extends Number> queryVector = embeddingService.getEmbedding(q);
		String embeddingLiteral = toPgVectorLiteral(queryVector);

		List<DocumentChunkProjection> results = documentChunkRepository.findNearestNeighbors(embeddingLiteral, k);

		String answer = ragService.answerWithRag(q, results);

		Map<String, Object> response = new HashMap<>();
		response.put("answer", answer);

		List<Map<String, Object>> sources = new ArrayList<>();
		for (DocumentChunkProjection chunk : results) {
			Map<String, Object> row = new HashMap<>();
			row.put("docId", chunk.getDocumentId());
			row.put("chunkIndex", chunk.getChunkIndex());
			row.put("content", chunk.getContent());
			sources.add(row);
		}
		response.put("chunks", sources);
		return response;
	}

	private static String toPgVectorLiteral(List<? extends Number> vector) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < vector.size(); i++) {
			if (i > 0)
				sb.append(',');
			sb.append(vector.get(i).toString());
		}
		sb.append(']');
		return sb.toString();
	}
}
