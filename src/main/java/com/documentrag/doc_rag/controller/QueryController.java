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

@RestController
@RequestMapping("/api/query")
public class QueryController {

	private final EmbeddingService embeddingService;
	private final DocumentChunkRepository documentChunkRepository;

	public QueryController(EmbeddingService embeddingService, DocumentChunkRepository documentChunkRepository) {
		this.embeddingService = embeddingService;
		this.documentChunkRepository = documentChunkRepository;
	}

	@GetMapping
	public List<Map<String, Object>> query(@RequestParam String q, @RequestParam(defaultValue = "5") int k) {
		// Get embedding as List<? extends Number>
		List<? extends Number> queryVector = embeddingService.getEmbedding(q);

		// Build pgvector literal like "[0.1,0.2,0.3]"
		String embeddingLiteral = toPgVectorLiteral(queryVector);

		// Run KNN search (projection, not entity)
		List<DocumentChunkProjection> results = documentChunkRepository.findNearestNeighbors(embeddingLiteral, k);

		//Build response
		List<Map<String, Object>> response = new ArrayList<>();
		for (DocumentChunkProjection chunk : results) {
			Map<String, Object> row = new HashMap<>();
			row.put("docId", chunk.getDocumentId());
			row.put("chunkIndex", chunk.getChunkIndex());
			row.put("content", chunk.getContent());
			response.add(row);
		}
		return response;
	}

	private static String toPgVectorLiteral(List<? extends Number> vector) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < vector.size(); i++) {
			if (i > 0)
				sb.append(',');
			Number n = vector.get(i);
			sb.append(n.toString());
		}
		sb.append(']');
		return sb.toString();
	}
}