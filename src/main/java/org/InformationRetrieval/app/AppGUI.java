package org.InformationRetrieval.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.InformationRetrieval.history.HistoryManager;
import org.InformationRetrieval.indexer.Indexer;
import org.InformationRetrieval.searcher.Searcher;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.*;

public class AppGUI extends Application {

    private final String INDEX_DIR = "src/main/resources/Index";
    private final HistoryManager historyManager = new HistoryManager();

    private VBox resultBox;
    private TextArea historyArea;
    private int currentPage = 0;
    private final int RESULTS_PER_PAGE = 10;
    private List<Document> lastSearchResults = new ArrayList<>();
    private String lastQuery = "";
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Search Engine - Lucene");
        
       


        TextField queryField = new TextField();
        queryField.setPromptText("Search...");

        ToggleGroup searchModeGroup = new ToggleGroup();
        RadioButton generalSearch = new RadioButton("Search By Keyword");
        generalSearch.setToggleGroup(searchModeGroup);
        generalSearch.setSelected(true);

        RadioButton fieldSearch = new RadioButton("Search By Field:");
        fieldSearch.setToggleGroup(searchModeGroup);

        RadioButton vectorSearch = new RadioButton("Search By Vector");
        vectorSearch.setToggleGroup(searchModeGroup);

        ComboBox<String> fieldSelector = new ComboBox<>();
        fieldSelector.getItems().addAll("author", "headline", "category", "section", "description", "article_text");
        fieldSelector.setValue("headline");

        ComboBox<String> sortSelector = new ComboBox<>();
        sortSelector.getItems().addAll("Relevance", "Alphabetical(Title)", "By Date");
        sortSelector.setValue("Relevance");

        HBox searchOptions = new HBox(10, generalSearch, fieldSearch, fieldSelector, vectorSearch);
        searchOptions.setPadding(new Insets(5));

        resultBox = new VBox(10);
        resultBox.setPadding(new Insets(5));
        ScrollPane scrollPane = new ScrollPane(resultBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        scrollPane.setMinHeight(250);

        historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setWrapText(true);
        ScrollPane historyScrollPane = new ScrollPane(historyArea);
        historyScrollPane.setFitToWidth(true);
        historyScrollPane.setPrefHeight(250);
        historyScrollPane.setMinHeight(250);

        Button indexBtn = new Button("\uD83D\uDCE5 Create Index");
        Button searchBtn = new Button("\uD83D\uDD0D Search");
        Button clearBtn = new Button("\uD83E\uDDF9 Clear");
        Button nextPageBtn = new Button("\u27A1 Next");
        Button prevPageBtn = new Button("\u2B05 Previous");

        indexBtn.setOnAction(e -> {
            new Indexer().createIndex("src/main/resources/CNN_Articles_clean.csv", INDEX_DIR);
            showMessage("Indexing was completed successfully!");
        });

        searchBtn.setOnAction(e -> {
            String query = queryField.getText().trim();
            if (query.isEmpty()) {
                showMessage("Type something to search for.");
                return;
            }

            try {
                Searcher searcher = new Searcher(INDEX_DIR);
                List<Document> results;

                if (vectorSearch.isSelected()) {
                    results = searcher.searchByVector(query, 50);
                } else if (generalSearch.isSelected()) {
                    results = searcher.searchByKeyword(query);
                } else {
                    results = searcher.searchByField(fieldSelector.getValue(), query);
                }

                if (sortSelector.getValue().equals("Alphabetical(Title)")) {
                    results.sort(Comparator.comparing(d -> d.get("headline"), String.CASE_INSENSITIVE_ORDER));
                } else if (sortSelector.getValue().equals("By Date")) {
                    results.sort(Comparator.comparing(d -> d.get("date_published"), Comparator.nullsLast(String::compareTo)));
                }

                lastQuery = query;
                lastSearchResults = results;
                currentPage = 0;
                showPageResults();
            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage("Error when searching.");
            }
        });

        clearBtn.setOnAction(e -> {
            resultBox.getChildren().clear();
            queryField.clear();
        });

        nextPageBtn.setOnAction(e -> {
            if ((currentPage + 1) * RESULTS_PER_PAGE < lastSearchResults.size()) {
                currentPage++;
                showPageResults();
            }
        });

        prevPageBtn.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                showPageResults();
            }
        });

        VBox mainBox = new VBox(10,
                new Label("Article Search"),
                queryField,
                searchOptions,
                new HBox(10, sortSelector, indexBtn, searchBtn, clearBtn),
                new Label("Results:"),
                scrollPane,
                new HBox(10, prevPageBtn, nextPageBtn),
                new Label("Search History:"),
                historyArea
        );
        mainBox.setPadding(new Insets(10));

        Scene scene = new Scene(mainBox, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showPageResults() {
        resultBox.getChildren().clear();
        
        if (lastSearchResults.isEmpty()) {
            Text noResults = new Text("\u26A0 No results found for your search.");
            noResults.setStyle("-fx-fill: black; -fx-font-weight: bold;");
            resultBox.getChildren().add(noResults);
            return;
        }
        
        int from = currentPage * RESULTS_PER_PAGE;
        int to = Math.min(from + RESULTS_PER_PAGE, lastSearchResults.size());

        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIR)))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            StandardAnalyzer analyzer = new StandardAnalyzer();
            String[] fields = {"headline", "author", "date_published", "category", "section", "description", "article_text", "url"};
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            Query query = parser.parse(lastQuery);

            Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<b>", "</b>"), new QueryScorer(query));
            highlighter.setTextFragmenter(new SimpleFragmenter(512)); 
;

            for (int i = from; i < to; i++) {
                Document doc = lastSearchResults.get(i);
                TextFlow flow = new TextFlow();
                flow.getChildren().add(new Text("\n" + (i + 1) + ". "));

                for (String field : fields) {
                    String label = field.substring(0, 1).toUpperCase() + field.substring(1).replace("_", " ");
                    String value = doc.get(field);
                    if (value != null) {
                        String highlighted = value;
                        {
    TokenStream ts = analyzer.tokenStream(field, value);
    String frag = highlighter.getBestFragment(ts, value);
    if (frag != null) {
        highlighted = frag;
    } else {
        highlighted = value.length() > 500 ? value.substring(0, 500) + "..." : value;
    }

}
                        flow.getChildren().add(new Text(label + ": "));
                        flow.getChildren().addAll(parseHtmlToTextNodes(highlighted));
                        flow.getChildren().add(new Text("\n"));
                    }
                }
                resultBox.getChildren().add(flow);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultBox.getChildren().add(new Text(" Error displaying highlighted results."));
        }

        List<Document> firstFive = lastSearchResults.subList(0, Math.min(5, lastSearchResults.size()));
        historyManager.addEntry(lastQuery, firstFive);
        historyArea.setText(historyManager.getFormattedHistory());
    }

    private List<Text> parseHtmlToTextNodes(String html) {
        List<Text> parts = new ArrayList<>();
        String[] tokens = html.split("(?i)<b>|</b>");
        boolean bold = false;
        for (String token : tokens) {
            Text t = new Text(token);
            if (bold) t.setStyle("-fx-font-weight: bold");
            parts.add(t);
            bold = !bold;
        }
        return parts;
    }

    private void showMessage(String message) {
        resultBox.getChildren().clear();
        resultBox.getChildren().add(new Text(message));
    }
}
