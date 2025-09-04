package com.documentrag.doc_rag.controller;

import com.documentrag.doc_rag.model.Chunk;
import com.documentrag.doc_rag.service.DocumentChunker;
import com.documentrag.doc_rag.service.PdfTextExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {
    private final PdfTextExtractor pdfTextExtractor;
    private final DocumentChunker documentChunker;
    
    public UploadController(PdfTextExtractor pdfTextExtractor, DocumentChunker documentChunker) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.documentChunker = documentChunker;
    }
    @Value("${app.storage.root:uploads}")
    private String storageRoot;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String id = UUID.randomUUID().toString();
        Path dir = Path.of(storageRoot, id);
        Files.createDirectories(dir);

        // Save raw PDF
        Path pdfPath = dir.resolve(original);
        Files.write(pdfPath, file.getBytes());

        // Extract text
        String text = pdfTextExtractor.extractText(file.getBytes());

        // Save raw text (temporary; DB comes later in Week 1 Day 5)
        Path txtPath = dir.resolve(original.replaceAll("\\.pdf$", "") + ".txt");
        Files.writeString(txtPath, text);
        
        List<Chunk> chunks = documentChunker.chunkText(id, text);

        // Save chunks as JSON
        Path chunksPath = dir.resolve("chunks.json");
        new ObjectMapper().writeValue(chunksPath.toFile(), chunks);


        return Map.of(
                "id", id,
                "file", original,
                "storedPdf", pdfPath.toString(),
                "storedTxt", txtPath.toString(),
                "storedChunks", chunksPath.toString(),
                "chunks", chunks.size(),
                "charsExtracted", text.length(),
                "uploadedAt", Instant.now().toString()
            );
    }
}
