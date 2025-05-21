package org.InformationRetrieval.history;

import org.apache.lucene.document.Document;

import java.util.*;

public class HistoryManager {

    private static final int MAX_HISTORY = 20;

    private final LinkedHashMap<String, List<String>> historyMap = new LinkedHashMap<>();

    public void addEntry(String query, List<Document> results) {
        List<String> top5Titles = new ArrayList<>();
        for (int i = 0; i < Math.min(results.size(), 5); i++) {
            String title = results.get(i).get("headline");
            top5Titles.add(title != null ? title : "(no title)");
        }

        if (historyMap.size() >= MAX_HISTORY) {
            String firstKey = historyMap.keySet().iterator().next();
            historyMap.remove(firstKey);
        }

        historyMap.put(query, top5Titles);
    }

    public String getFormattedHistory() {
        if (historyMap.isEmpty()) return "\uD83D\uDCEC No search history available.";

        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (Map.Entry<String, List<String>> entry : historyMap.entrySet()) {
            sb.append("\uD83D\uDD0E ").append(count++).append(". Query: ").append(entry.getKey()).append("\n");
            for (String title : entry.getValue()) {
            	sb.append("   \u2022 ").append(title).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
