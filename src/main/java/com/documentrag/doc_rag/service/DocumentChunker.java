package com.documentrag.doc_rag.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.documentrag.doc_rag.model.Chunk;

@Service
public class DocumentChunker {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    public List<Chunk> chunkText(String docId, String text) {
        List<Chunk> chunks = new ArrayList<>();
        int start = 0;
        int index = 0;

        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            String piece = text.substring(start, end);

            chunks.add(new Chunk(docId, index, piece, start, end));
            index++;
            start = end - CHUNK_OVERLAP; 
            if (start < 0) start = 0;
        }

        return chunks;
    }
}
