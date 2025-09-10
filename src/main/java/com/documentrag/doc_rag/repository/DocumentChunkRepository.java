package com.documentrag.doc_rag.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.documentrag.doc_rag.model.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

	@Query(value = """
			SELECT
			dc.id AS id,
			dc.document_id AS documentId,
			dc.chunk_index AS chunkIndex,
			dc.content AS content
			FROM document_chunks dc
			ORDER BY dc.embedding <-> CAST(:embedding AS vector)
			LIMIT :k
			""", nativeQuery = true)
	List<DocumentChunkProjection> findNearestNeighbors(@Param("embedding") String embeddingLiteral, @Param("k") int k);
}