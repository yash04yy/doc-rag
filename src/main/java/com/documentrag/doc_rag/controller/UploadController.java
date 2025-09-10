package com.documentrag.doc_rag.controller;

import com.documentrag.doc_rag.model.Chunk;
import com.documentrag.doc_rag.model.Document;
import com.documentrag.doc_rag.model.DocumentChunk;
import com.documentrag.doc_rag.repository.DocumentChunkRepository;
import com.documentrag.doc_rag.repository.DocumentRepository;
import com.documentrag.doc_rag.service.DocumentChunker;
import com.documentrag.doc_rag.service.EmbeddingService;
import com.documentrag.doc_rag.service.PdfTextExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UploadController {

	private final PdfTextExtractor pdfTextExtractor;
	private final DocumentChunker documentChunker;
	private final DocumentRepository documentRepository;
	private final DocumentChunkRepository documentChunkRepository;
	private final EmbeddingService embeddingService;

	public UploadController(PdfTextExtractor pdfTextExtractor, DocumentChunker documentChunker,
			DocumentRepository documentRepository, DocumentChunkRepository documentChunkRepository,
			EmbeddingService embeddingService) {
		this.pdfTextExtractor = pdfTextExtractor;
		this.documentChunker = documentChunker;
		this.documentRepository = documentRepository;
		this.documentChunkRepository = documentChunkRepository;
		this.embeddingService = embeddingService;
	}

	@Value("${app.storage.root:uploads}")
	private String storageRoot;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Map<String, Object> upload(@RequestPart("file") MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Empty file");
		}

		String original = StringUtils.cleanPath(file.getOriginalFilename());
		String id = UUID.randomUUID().toString();

		// Resolve storage dir and ensure it exists
		Path root = Path.of(storageRoot).toAbsolutePath().normalize();
		Path dir = root.resolve(id);
		Files.createDirectories(dir);

		// Save PDF using Files.copy
		Path pdfPath = dir.resolve(original);
		try (InputStream in = file.getInputStream()) {
			Files.copy(in, pdfPath, StandardCopyOption.REPLACE_EXISTING);
		}

		// Extract text
		byte[] pdfBytes = Files.readAllBytes(pdfPath);
		String text = pdfTextExtractor.extractText(pdfBytes);

		if (text == null || text.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
					"No extractable text found. This PDF appears to be scanned/image-only.");
		}

		// Save extracted text
		Path txtPath = dir.resolve(original.replaceAll("\\.pdf$", "") + ".txt");
		Files.writeString(txtPath, text);

		// Chunk text
		List<Chunk> chunks = documentChunker.chunkText(id, text);

		// Persist chunks to JSON
		Path chunksPath = dir.resolve("chunks.json");
		new ObjectMapper().writeValue(chunksPath.toFile(), chunks);

		// Create Document entity
		Document doc = new Document();
		doc.setName(original);
		doc.setUploadDate(Instant.now());
		doc.setNumChunks(chunks.size());
		Document saved = documentRepository.save(doc);

		// Generate & persist embedding in DB
		List<Map<String, Object>> embeddingsOut = new ArrayList<>(chunks.size());
		int embeddedCount = 0;
		for (Chunk c : chunks) {
			String content = c.getContent();
			if (content == null || content.isBlank()) {
				continue;
			}
			List<Double> vector;
			try {
				vector = embeddingService.getEmbedding(content);
			} catch (Exception ex) {
				vector = Collections.emptyList(); // continue on error
			}
			
			if (!vector.isEmpty()) {
				float[] embeddingArray = new float[vector.size()];
				for (int i = 0; i < vector.size(); i++) {
					 embeddingArray[i] = vector.get(i).floatValue();
				}
				
                DocumentChunk entity = new DocumentChunk();
                entity.setDocument(saved);
                entity.setChunkIndex(c.getIndex());
                entity.setContent(content);
                entity.setEmbedding(embeddingArray); // assuming you mapped vector column
                documentChunkRepository.save(entity);
                embeddedCount++;
            }
			
			Map<String, Object> rec = new LinkedHashMap<>();
			rec.put("index", c.getIndex());
			rec.put("start", c.getStartOffset());
			rec.put("end", c.getEndOffset());
			rec.put("embedding", vector);
			embeddingsOut.add(rec);
		}

		Path embeddingsPath = dir.resolve("embeddings.json");
		new ObjectMapper().writeValue(embeddingsPath.toFile(), embeddingsOut);

		return Map.of("dbId", saved.getId(), "file", original, "storedPdf", pdfPath.toString(), "storedTxt",
				txtPath.toString(), "storedChunks", chunksPath.toString(), "storedEmbeddings",
				embeddingsPath.toString(), "chunks", chunks.size(), "chunksEmbedded", embeddedCount, "charsExtracted",
				text.length(), "uploadedAt", saved.getUploadDate().toString());
	}
}