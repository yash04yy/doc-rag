package com.documentrag.doc_rag.controller;

import com.documentrag.doc_rag.repository.DocumentChunkProjection;
import com.documentrag.doc_rag.repository.DocumentChunkRepository;
import com.documentrag.doc_rag.service.EmbeddingService;
import com.documentrag.doc_rag.service.RagService;
import com.documentrag.doc_rag.graphql.QueryChunksResult;
import com.documentrag.doc_rag.graphql.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RagGraphQLController {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private final RagService ragService;

    @Autowired
    public RagGraphQLController(
            EmbeddingService embeddingService,
            DocumentChunkRepository documentChunkRepository,
            RagService ragService
    ) {
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
        this.ragService = ragService;
    }

    @QueryMapping
    public QueryChunksResult queryChunks(
        @Argument String q,
        @Argument Integer k) {
        if (q == null || q.isEmpty()) {
            throw new IllegalArgumentException("Query string (q) must not be empty");
        }
        int limit = (k != null) ? k : 5;
        List<Double> queryVector = embeddingService.getEmbedding(q);

        String embeddingLiteral = toPgVectorLiteral(queryVector);

        List<DocumentChunkProjection> results = documentChunkRepository.findNearestNeighbors(embeddingLiteral, limit);
        String answer = ragService.answerWithRag(q, results);

        List<Chunk> chunks = results.stream().map(chunk -> new Chunk(
                chunk.getId(),
                chunk.getDocumentId(),
                chunk.getChunkIndex(),
                chunk.getContent()
        )).collect(Collectors.toList());

        return new QueryChunksResult(answer, chunks);
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
