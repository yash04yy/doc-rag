package com.documentrag.doc_rag.model;

import lombok.Data;

@Data
public class Chunk {
	 public Chunk(String docId, int index, String content, int startOffset, int endOffset) {
		super();
		this.docId = docId;
		this.index = index;
		this.content = content;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}
	 public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getStartOffset() {
		return startOffset;
	}
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}
	public int getEndOffset() {
		return endOffset;
	}
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}
	 private String docId;
	 private int index;
	 private String content;
	 private int startOffset;
	 private int endOffset;
}
