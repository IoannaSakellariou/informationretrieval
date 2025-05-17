# RAG QA System on CNN Articles

## Overview

This project implements a Retrieval-Augmented Generation (RAG) system for Question Answering (QA) using a dataset of CNN news articles. The system combines retrieval from a vector database with generation from a large language model (LLM) to answer user queries accurately based on context.

## Features

- Loads and chunks article text using `RecursiveCharacterTextSplitter`
- Creates dense vector embeddings using `sentence-transformers/all-MiniLM-L6-v2`
- Stores vectors in a FAISS vector database
- Uses LangChain with `Ollama` and `llama3.2` model for answering questions
- CLI-based QA interaction
- Evaluation script with/without context using 5 test queries

## Installation

Make sure you have Python 3.10+ and the following packages installed:

```bash
pip install langchain langchain-community sentence-transformers faiss-cpu
pip install --upgrade huggingface-hub
```

Also ensure you have [`ollama`](https://ollama.com/) installed and running locally with the `llama3.2` model pulled:

```bash
ollama pull llama3.2
```

## Usage

### Build the FAISS Vector DB:

```bash
python rag.py
```

This will:
- Load and preprocess the dataset
- Chunk the text and embed it
- Store it in a FAISS database

### Run CLI QA system:

```bash
python rag.py
```

Interactively ask questions. Type `exit` to quit.

### Evaluate RAG system:

```bash
python evaluate.py
```

This script runs 5 predefined queries with and without retrieval context and stores the results in `evaluation_results.txt`.

## File Structure

- `rag.py`: Main script to build vector DB and launch CLI QA
- `evaluate.py`: Evaluation of RAG vs vanilla LLM answers
- `CNN_Articles_clean.csv`: Dataset of news articles
- `evaluation_results.txt`: Output from the evaluation script
- `report.txt`: Project report

## Authors
Amalia Georgia Mouselimi
Ioanna Sakellariou