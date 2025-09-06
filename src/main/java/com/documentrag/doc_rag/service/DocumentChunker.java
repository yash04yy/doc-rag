package com.documentrag.doc_rag.service;

import com.documentrag.doc_rag.model.Chunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentChunker {
	private static final int CHUNK_SIZE = 500;
	private static final int CHUNK_OVERLAP = 50;

	public List<Chunk> chunkText(String docId, String text) {
		if (text == null)
			text = "";
		
		List<Chunk> chunks = new ArrayList<>();
		final int step = CHUNK_SIZE - CHUNK_OVERLAP;

		int index = 0;
		for (int start = 0; start < text.length(); start += step, index++) {
			int end = Math.min(start + CHUNK_SIZE, text.length());
			String piece = text.substring(start, end);
			chunks.add(new Chunk(docId, index, piece, start, end));
			if (end == text.length())
				break; // last chunk
		}
		return chunks;
	}
}