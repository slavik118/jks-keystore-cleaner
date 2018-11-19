package com.cert.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cert.model.JKSFile;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

// We can use java interface static methods to remove utility classes such as Collections and move all of it’s static methods to the corresponding interface, that would be easy to find and use.
// https://www.callicoder.com/java-read-write-csv-file-opencsv/
public interface OpenCSVReader {
	
	 /**
     * Reads entries from the cvs file.
     * 
     * @throws IOException if there was an I/O problem with data.
     */
	// http://zetcode.com/articles/opencsv/
	public static Set<JKSFile> loadCSVFile(final InputStream inputStream) throws IOException{
		  
		final Set<JKSFile> jksFiles = new HashSet<>();
		
		 final CSVParser parser = new CSVParserBuilder().withSeparator(',').build();

		 try (final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
	                CSVReader reader = new CSVReaderBuilder(br).withCSVParser(parser)
	                        .build()) {

	            final List<String[]> rows = reader.readAll();

	            for (final String[] row : rows) {
	                final JKSFile file = new JKSFile(row[0].toString(),
	                		row[1].toString().toCharArray());
	                jksFiles.add(file);                
	            }
	        }
		 return jksFiles;
	}

}
