import pandas as pd
import os
import subprocess
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.docstore.document import Document
from langchain.embeddings import HuggingFaceEmbeddings
from langchain.vectorstores import FAISS
from langchain_community.llms import Ollama
from langchain.chains import RetrievalQA



def c(text, color): 
    colors = {
        "blue": "\033[94m",
        "green": "\033[92m",
        "yellow": "\033[93m",
        "red": "\033[91m",
        "end": "\033[0m",
        "bold": "\033[1m"
    }
    return f"{colors[color]}{text}{colors['end']}"


CSV_PATH = r"C:\Users\user\Desktop\bonusinfret\CNN_Articles_clean.csv"

MODEL_NAME = "all-MiniLM-L6-v2"
LLM_NAME = "llama3.2:3b"
CHUNK_SIZE = 500
CHUNK_OVERLAP = 50
TOP_K = 4
FAISS_DIR = "faiss_index"


print(c("Loading articles...", "blue"))
df = pd.read_csv(CSV_PATH)
texts = df['Article text'].dropna().tolist()
documents = [Document(page_content=t) for t in texts]



print(c("Splitting documents into chunks...", "blue"))
splitter = RecursiveCharacterTextSplitter(chunk_size=CHUNK_SIZE, chunk_overlap=CHUNK_OVERLAP)
chunked_docs = splitter.split_documents(documents)


print(c("Generating embeddings...", "blue"))
embedding = HuggingFaceEmbeddings(model_name=MODEL_NAME)


print(c("Building FAISS vector store...", "blue"))
if os.path.exists("faiss_index"):
    print(c("Loading existing FAISS index...", "yellow"))
    db = FAISS.load_local("faiss_index", embedding, allow_dangerous_deserialization=True)
else:
    print(c("Creating new FAISS index...", "green"))
    db = FAISS.from_documents(chunked_docs, embedding)
    db.save_local("faiss_index")


llm = Ollama(model="llama3.2:3b")
qa_chain = RetrievalQA.from_chain_type(llm=llm, retriever=db.as_retriever(), return_source_documents=True)

print(c("\nRAG system is ready. Type 'exit' to quit.\n", "green"))

while True:
    question = input(c("Question: ", "bold"))
    if question.lower() in ["exit", "quit"]:
        break

    print(c("Waiting for LLM response...\n", "yellow"))
    result = qa_chain.invoke({"query": question})

    print(c("Answer:", "green"))
    print(result['result'])

    print(c("\nRetrieved sources:", "blue"))
    for doc in result['source_documents']:
        print("--", doc.page_content[:200], "...\n")


