package org.InformationRetrieval.indexer;

import org.InformationRetrieval.loader.CsvLoader;
import org.InformationRetrieval.utils.EmbeddingHelper;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


import java.nio.file.Paths;
import java.util.List;

public class Indexer {

    private final CsvLoader csvLoader = new CsvLoader();

    public void createIndex(String csvFileName, String indexDirectoryPath) {
        System.out.println(" Indexing started..."); 

        try {
            List<String[]> articles = csvLoader.loadCsv(csvFileName);
            System.out.println(" Articles loaded: " + articles.size()); 

            Directory directory = FSDirectory.open(Paths.get(indexDirectoryPath));
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            IndexWriter writer = new IndexWriter(directory, config);

            
            for (String[] article : articles) {

                Document doc = new Document();

                doc.add(new StringField("index", article[0], Field.Store.YES));
                doc.add(new TextField("author", article[1], Field.Store.YES));
                doc.add(new StringField("date_published", article[2], Field.Store.YES));
                doc.add(new TextField("category", article[3], Field.Store.YES));
                doc.add(new TextField("section", article[4], Field.Store.YES));
                doc.add(new StringField("url", article[5], Field.Store.YES));
                doc.add(new TextField("headline", article[6], Field.Store.YES));
                doc.add(new TextField("description", article[7], Field.Store.YES));
                doc.add(new TextField("keywords", article[8], Field.Store.YES));
                doc.add(new TextField("second_headline", article[9], Field.Store.YES));
                doc.add(new TextField("article_text", article[10], Field.Store.YES));

                String textForEmbedding = article[6] + " " + article[7] + " " + article[10];
                float[] vector = EmbeddingHelper.embedText(textForEmbedding);

                doc.add(new KnnVectorField("vector", vector, VectorSimilarityFunction.DOT_PRODUCT));
                writer.addDocument(doc);
            }

            writer.commit();
            writer.close();

            System.out.println("Index creation completed successfully!"); // ✅

        } catch (Exception e) {
            System.err.println("Indexing error: " + e.getMessage()); // ❌
            e.printStackTrace();
        }
    }
}
