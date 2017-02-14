package de.tu_darmstadt.sse.dynamiccfg.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class FileUtils {

	
	public static Set<String> textFileToLineSet(String fileName) {
		Set<String> analysesNames = new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		    try {
		        String line;
		        while ((line = br.readLine()) != null) {
		        	analysesNames.add(line);
		        }
		    } finally {
		        br.close();
		    }
		}catch(Exception ex) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}
		return analysesNames;		
	}
	
	
}
