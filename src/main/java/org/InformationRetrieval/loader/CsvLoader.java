package org.InformationRetrieval.loader;


import com.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;



public class CsvLoader {

	
	public List<String[]> loadCsv(String absolutePath) {
	    List<String[]> records = new ArrayList<>(); // λίστα για να αποθηκεύσουμε όλες τις εγγραφές του CSV.

	    try (CSVReader csvReader = new CSVReader(new FileReader(absolutePath))) {
	        String[] values; //θα κρατά την κάθε γραμμή
	        boolean firstLine = true;// σημαία για να αγνοήσουμε την πρώτη γραμμή

	        while ((values = csvReader.readNext()) != null) {
	            if (firstLine) {
	                firstLine = false;
	                continue;
	            } // διαβάζουμε γραμμη γραμμη, αγνοούμε την πρωτη
	            records.add(values); // προσθετουμε στην λιστα τις γραμμες
	        }
	    } catch (Exception e) {
	    	System.err.println("\u274C CSV reading error: " + e.getMessage());

	        e.printStackTrace();
	    }

	    return records;
	}


}
