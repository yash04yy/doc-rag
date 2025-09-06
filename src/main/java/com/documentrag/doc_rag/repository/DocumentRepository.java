package com.documentrag.doc_rag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.documentrag.doc_rag.model.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

}
