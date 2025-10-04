package com.documentrag.doc_rag.graphql;

import java.util.List;

public class QueryChunksResult {
    private String answer;
    private List<Chunk> chunks;

    public QueryChunksResult() {}

    public QueryChunksResult(String answer, List<Chunk> chunks) {
        this.answer = answer;
        this.chunks = chunks;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }
}
