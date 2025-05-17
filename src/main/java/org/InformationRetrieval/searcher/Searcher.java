
package org.InformationRetrieval.searcher;



import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnVectorQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.InformationRetrieval.utils.EmbeddingHelper;

import ai.djl.ModelException;
import ai.djl.translate.TranslateException;
import java.io.*;


public class Searcher {
	
	 private String indexDirectoryPath;

	    public Searcher(String indexDirectoryPath) {
	        this.indexDirectoryPath = indexDirectoryPath;
	    }

	    public List<Document> searchByKeyword(String keyword) {
	        try {
	            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectoryPath)));
	            IndexSearcher searcher = new IndexSearcher(reader);

	            String[] fields = {
	                "author", "headline", "category", "section",
	                "description", "second_headline", "article_text",
	                "url", "date_published"
	            };
	            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());
	            Query query = parser.parse(keyword);

	            TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
	            List<Document> results = new ArrayList<>();
	            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	                results.add(searcher.storedFields().document(scoreDoc.doc));
	            }

	            System.out.println(" Executing keyword search for: " + query.toString());
	            System.out.println(" Total matches found: " + topDocs.totalHits.value);

	            reader.close();
	            return results;

	        } catch (Exception e) {
	            e.printStackTrace();
	            return new ArrayList<>();
	        }
	    }

	    public List<Document> searchByField(String field, String value) {
	        try {
	            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectoryPath)));
	            IndexSearcher searcher = new IndexSearcher(reader);

	            QueryParser parser = new QueryParser(field, new StandardAnalyzer());
	            Query query = parser.parse(value);

	            TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
	            List<Document> results = new ArrayList<>();
	            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	                results.add(searcher.storedFields().document(scoreDoc.doc));
	            }

	            System.out.println("Executing field search for: " + query.toString());
	            System.out.println("Total matches found: " + topDocs.totalHits.value);

	            reader.close();
	            return results;

	        } catch (Exception e) {
	            e.printStackTrace();
	            return new ArrayList<>();
	        }
	    }

	    
	    public List<Document> searchByVector(String queryText, int topK) throws IOException, ModelException, TranslateException {
	        float[] queryVector = EmbeddingHelper.embedText(queryText);

	        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDirectoryPath)))) {
	            IndexSearcher searcher = new IndexSearcher(reader);
	            Query vectorQuery = new KnnVectorQuery("vector", queryVector, topK);

	            TopDocs topDocs = searcher.search(vectorQuery, topK);
	            List<Document> results = new ArrayList<>();

	            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	                if (scoreDoc.score >= 0.75f) {
	                    Document doc = searcher.storedFields().document(scoreDoc.doc);
	                    results.add(doc);
	                    System.out.println("    SCORE: " + scoreDoc.score);
	                }
	            }

	            System.out.println("Executing vector search for: " + queryText);
	            System.out.println("Total vector results retrieved: " + topDocs.totalHits.value);
	            System.out.println("Filtered results (score ≥ 0.75): " + results.size());

	            return results;
	        }
	    }
    
    
}

