package de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.analysis.symbolicexecution.datastructure.SMTProgram;


public class SMTExecutor {
	
	private final Set<SMTProgram> smtPrograms;
	private final File Z3script;
	
	public SMTExecutor(Set<SMTProgram> smtPrograms, File Z3script) {
		this.smtPrograms = smtPrograms;
		this.Z3script = Z3script;
	}
	
	
	public Set<File> createSMTFile() {
		Set<File> allSMTProgramFiles = new HashSet<File>();
		for(SMTProgram smtProgram : smtPrograms) {
			File smtFile = null;
			FileOutputStream fos = null;
			try {
				smtFile = File.createTempFile("Z3SMT.txt", null);
				fos = new FileOutputStream(smtFile.getPath());		
				byte[] output = smtProgram.toString().getBytes();
				fos.write(output);
			} catch (IOException e) {
				e.printStackTrace();			
			} finally {
				try {
					fos.close();
				} catch(Exception ex) {
					System.err.println("File-handle not closed properly");
				}
			}
			allSMTProgramFiles.add(smtFile);
		}
		
		return allSMTProgramFiles;
	}
	
	
	public String executeZ3str2ScriptAndExtractLoggingPointValue(File smtFile) {
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		String line = null;
		try { 
			Process p = Runtime.getRuntime().exec(String.format("%s -f %s", Z3script.getAbsolutePath(), smtFile.getAbsolutePath()));         
	        stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        String pattern = "loggingPoint\\_0.*\"(.*)\"";
	        Pattern r = Pattern.compile(pattern);	        
	        
	        while ((line = stdInput.readLine()) != null) {
	        	Matcher m = r.matcher(line);
	        	if (m.find( )) {
	        		return m.group(1);
	        	}
	        }
		}catch(IOException e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		} finally {
			try {
				stdInput.close();
				stdError.close();
			} catch (IOException e) {
				System.err.println("BufferedReader not properly closed!");
			}			
		}
		return null;
	}	
	
	
	public String executeZ3str2ScriptAndExtractValue(File smtFile, String value) {
		BufferedReader stdInput = null;
		BufferedReader stdError = null;
		String line = null;
		try { 
			Process p = Runtime.getRuntime().exec(String.format("%s -f %s", Z3script.getAbsolutePath(), smtFile.getAbsolutePath()));         
			stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String pattern = value.replace("$", "\\$")+"\\_0.*\"(.*)\"";
			Pattern r = Pattern.compile(pattern);	        
			
			while ((line = stdInput.readLine()) != null) {
				Matcher m = r.matcher(line);
				if (m.find( )) {
					return m.group(1);
				}
			}
		}catch(IOException e) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		} finally {
			try {
				stdInput.close();
				stdError.close();
			} catch (IOException e) {
				System.err.println("BufferedReader not properly closed!");
			}			
		}
		return null;
	}	
}
