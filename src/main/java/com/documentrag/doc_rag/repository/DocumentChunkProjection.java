package com.documentrag.doc_rag.repository;

public interface DocumentChunkProjection {
	Long getId();

	Long getDocumentId();

	Integer getChunkIndex();

	String getContent();
}
