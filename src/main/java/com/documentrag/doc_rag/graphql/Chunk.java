package com.documentrag.doc_rag.graphql;

public class Chunk {
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String content;

    public Chunk() {}

    public Chunk(Long id, Long documentId, Integer chunkIndex, String content) {
        this.id = id;
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
