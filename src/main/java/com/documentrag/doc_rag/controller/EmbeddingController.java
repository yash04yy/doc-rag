package com.documentrag.doc_rag.controller;
import com.documentrag.doc_rag.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/embed")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @GetMapping
    public List<Double> embed(@RequestParam String text) {
        return embeddingService.getEmbedding(text);
    }
}