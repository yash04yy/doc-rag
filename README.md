# Intelligent Document Analyzer Bot (RAG-based)

This project is a Spring Boot + PostgreSQL application that supports document upload, chunking, embedding generation, and retrieval-augmented generation (RAG) for intelligent document Q&A.

## Features
- Upload PDF documents.
- Extract and chunk text content.
- Generate embeddings using Hugging Face models.
- Store document metadata and embeddings in PostgreSQL.
- Perform semantic search for question answering.

## Tech Stack
- **Backend:** Spring Boot (Java)
- **Database:** PostgreSQL
- **Embedding Model:** text-embedding-nomic-embed-text-v2-moe
- **Build Tool:** Maven

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL (local or Docker)
- **LM Studio** (local embedding API)
