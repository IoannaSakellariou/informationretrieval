package org.InformationRetrieval.history;

import org.apache.lucene.document.Document;

import java.util.*;

public class HistoryManager {

    private static int MAX_HISTORY = 20;

    // LinkedHashMap to preserve insertion order — query -> list of article titles
    private LinkedHashMap<String, List<String>> historyMap = new LinkedHashMap<String, List<String>>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<String>> eldest) {
            return size() > MAX_HISTORY;
        }
    };

    // Adds a new query and up to 5 result headlines
    public void addEntry(String query, List<Document> results) {
        List<String> top5Titles = new ArrayList<>();
        for (int i = 0; i < Math.min(results.size(), 5); i++) {
            String title = results.get(i).get("headline");
            top5Titles.add(title != null ? title : "(no title)");
        }
        historyMap.put(query, top5Titles);
    }

    // Returns the formatted search history string
    public String getFormattedHistory() {
        if (historyMap.isEmpty()) return "\uD83D\uDCEC No search history available."; 

        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (Map.Entry<String, List<String>> entry : historyMap.entrySet()) {
            sb.append("\uD83D\uDD0E ").append(count++).append(". Query: ").append(entry.getKey()).append("\n"); 
            List<String> articles = entry.getValue();
            for (String title : articles) {
                sb.append("   • ").append(title).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
