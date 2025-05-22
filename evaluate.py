import os
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import FAISS
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
    return f"{colors.get(color, '')}{text}{colors['end']}"

MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"
FAISS_DIR = "faiss_index"
OUTPUT_FILE = "evaluation_results.txt"


queries = [
    "What is TuSimple's plan for autonomous trucks?",
    "What are the benefits of the Ironhand glove?",
    "What is the impact of EU sanctions on Russian energy?",
    "Who was the protester on Russian state television and what did she do?",
    "How have oil prices changed due to the war in Ukraine?"
]


embedding = HuggingFaceEmbeddings(model_name=MODEL_NAME)
db = FAISS.load_local(FAISS_DIR, embedding, allow_dangerous_deserialization=True)
retriever = db.as_retriever()

llm = Ollama(model="llama3.2")
qa_chain = RetrievalQA.from_chain_type(llm=llm, retriever=retriever, return_source_documents=True)

print(c("\nStarting RAG evaluation with and without context...\n", "green"))
with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    for i, q in enumerate(queries, 1):
        header = f"{'=' * 40}\nQuestion {i}: {q}\n{'=' * 40}\n"
        f.write(header)
        print(c(header, "bold"))

        # με χρήση context (RAG)
        result_rag = qa_chain.invoke({"query": q})
        rag_answer = result_rag['result']
        f.write("\nAnswer with context:\n" + rag_answer + "\n")
        print(c("Answer with context:", "green"))
        print(rag_answer)

        f.write("\nRelevant Chunks Used:\n")
        print(c("\nRelevant Chunks Used:", "blue"))
        for doc in result_rag['source_documents']:
            chunk = doc.page_content[:250].replace("\n", " ")
            f.write("-- " + chunk + "...\n")
            print("--", chunk, "...\n")

        # χωρίς χρήση context (απευθείας ερώτηση στο LLM)
        no_context = llm.invoke(q)
        f.write("\nAnswer without context:\n" + no_context + "\n")
        print(c("Answer without context:", "yellow"))
        print(no_context)

print(c("\nEvaluation complete. Results saved to 'evaluation_results.txt'\n", "green"))
