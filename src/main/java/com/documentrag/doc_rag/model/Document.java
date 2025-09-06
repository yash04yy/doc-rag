package com.documentrag.doc_rag.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="documents")
public class Document {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;
    private String name;
    private Instant uploadDate;
    private int numChunks;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Instant getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(Instant uploadDate) {
		this.uploadDate = uploadDate;
	}
	public int getNumChunks() {
		return numChunks;
	}
	public void setNumChunks(int numChunks) {
		this.numChunks = numChunks;
	}
    
}
