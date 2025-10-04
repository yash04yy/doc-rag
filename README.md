# Intelligent Document Analyzer Bot (RAG-based)

This project is a Spring Boot + PostgreSQL application that supports document upload, chunking, embedding generation, and retrieval-augmented generation (RAG) for intelligent document Q&A.

## Features
- Upload PDF documents.
- Extract and chunk text content.
- Generate embeddings using Hugging Face models.
- Store document metadata and embeddings in PostgreSQL.
- Perform semantic search for question answering.

## Tech Stack
- **Spring Boot (Java)**: Handles the web backend, business logic, and REST/GraphQL APIs.
- **PostgreSQL**: Stores document metadata, text chunks, and embeddings. Uses `pgvector` for efficient vector similarity search.
- **Text Embedding Model**: The project uses `text-embedding-nomic-embed-text-v2-moe` (or any compatible model via LM Studio) to convert text into embeddings.
- **LM Studio**: Provides a local REST API for generating embeddings from text.
- **GraphQL & REST**: Both API styles supported—GraphQL for flexible queries, REST for simple integrations.
- **Maven**: The build automation tool used to manage dependencies.

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (local or Docker)
- **LM Studio** (local embedding API)

<img width="1512" height="856" alt="Screenshot 2025-09-15 at 6 10 34 PM" src="https://github.com/user-attachments/assets/df083713-5528-4a5c-a74a-8ab4b9e12c66" />
## Directory Layout (Key Files)

- `src/main/java/com/documentrag/doc_rag/controller/` — Contains all controllers for REST and GraphQL (see `RagGraphQLController.java`).
- `src/main/java/com/documentrag/doc_rag/service/` — Services for embeddings, RAG answer generation, and more.
- `src/main/java/com/documentrag/doc_rag/repository/` — JPA repositories/interfaces for chunk/document storage.
- `src/main/java/com/documentrag/doc_rag/graphql/` — GraphQL DTOs/types for API responses.
- `src/main/resources/graphql/` — GraphQL schema definitions.
- `src/main/resources/application.properties` — Configuration (database, ports, API keys, etc.)

---

## Example User Flow

1. Start the Spring Boot app (and ensure PostgreSQL + LM Studio are running).
2. Upload your PDF using the UI or API.
3. The backend chunks and indexes your document in the DB.
4. Run a search via REST or GraphQL.
5. Receive an answer, plus direct links to the most relevant source passages.

---

## Extending or Modifying the Project

- **Add fields**: Update the schema and DTOs (Chunk, QueryChunksResult), adapt `DocumentChunkProjection` and add DB fields if needed.
- **Switch embedding models**: Change LM Studio's backend or update config in `application.properties`.
- **Tune retrieval**: Adjust k (top-k) chunk retrieval in API/UI or query logic.
- **Integrate new LLMs**: Swap out or enhance the `RagService` logic.
- **Add more endpoints**: Implement more REST endpoints or GraphQL queries as needed.

---



## API Endpoints & Usage

### REST Endpoint

- **Semantic Search (RAG Retrieval)**
    - URL: `http://localhost:8080/api/query`
    - Method: `GET`
    - Params:
        - `q`: your query string
        - `k`: (optional) number of relevant chunks to return (default: 5)
    - Example:
      ```
      curl "http://localhost:8080/api/query?q=What is angioplasty?&k=3"
      ```
    - Returns:
        ```json
        {
          "answer": "Generated answer text...",
          "chunks": [
            { "docId": 1, "chunkIndex": 0, "content": "..." },
            ...
          ]
        }
        ```

### GraphQL Endpoint

- **Querying via GraphQL**
    - URL: `http://localhost:8080/graphql`
    - Method: `POST`
    - Example Query:
      ```graphql
      {
        queryChunks(q: "What is angioplasty?", k: 3) {
          answer
          chunks {
            id
            documentId
            chunkIndex
            content
          }
        }
      }
      ```
    - Curl Example:
      ```
      curl -X POST http://localhost:8080/graphql \
        -H "Content-Type: application/json" \
        -d '{"query": "{ queryChunks(q: \"What is angioplasty?\", k: 3) { answer chunks { id documentId chunkIndex content } } }"}'
      ```

- **GraphiQL UI (Web IDE)**
    - URL: `http://localhost:8080/graphiql`
    - Use this in your browser for interactive query/testing.

### Troubleshooting & Notes

- Ensure your backend is running and all required services are up (DB and embedding model).
- For authentication or CORS issues, adjust your Spring Boot configuration or security settings as needed.
- Both endpoints use the same retrieval and RAG backend for consistent answers.

---
