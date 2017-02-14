//package de.tu_darmstadt.sse.visualization.inputformat;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import de.tu_darmstadt.sse.sharedclasses.SharedClassesSettings;
//import de.tu_darmstadt.sse.visualization.events.AbstractPathExecutionEvent;
//import de.tu_darmstadt.sse.visualization.events.MethodCalleeEvent;
//import de.tu_darmstadt.sse.visualization.events.MethodCallerEvent;
//import de.tu_darmstadt.sse.visualization.events.ReturnEvent;
//
//
//public class LogcatInput extends AbstractConverter{
//	
//	public LogcatInput(File logcatInputFile, File dotOutputFile) {
//		super(logcatInputFile, dotOutputFile);
//	}
//
//	@Override
//	public List<AbstractPathExecutionEvent> readEventsFromInput() {
//		List<String> logcatLines = readLines(getInputFile());
//		List<AbstractPathExecutionEvent> allEvents = collectEvents(logcatLines);
//		return allEvents;		
//	}
//	
//	
//	private List<String> readLines(File logcatFile)
//	{
//		List<String> lines = null;
//		try {
//			lines = Files.readAllLines(Paths.get(logcatFile.getAbsolutePath()), Charset.defaultCharset());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return lines;
//	}
//	
//	
//	private List<AbstractPathExecutionEvent> collectEvents(List<String> logcatLines) {
//		List<AbstractPathExecutionEvent> allEvents = new ArrayList<AbstractPathExecutionEvent>();
//		
//		for(String line : logcatLines)
//		{
//			if(line.contains(SharedClassesSettings.METHOD_CALLER_LABEL))
//			{
//				AbstractPathExecutionEvent event = handleMethodCall(line);
//				allEvents.add(event);
//			}
//			else if(line.contains(SharedClassesSettings.METHOD_CALLEE_LABEL))
//			{
//				AbstractPathExecutionEvent event = handleMethodCalleeEvent(line);
//				allEvents.add(event);
//			}
//			else if(line.contains(SharedClassesSettings.RETURN_LABEL))
//			{
//				AbstractPathExecutionEvent event = handleReturnStmt(line);
//				allEvents.add(event);
//			}
//		}
//		
//		return allEvents;
//	}
//	
//	
//	private MethodCallerEvent handleMethodCall(String line)
//	{			
//		String regex = ".*\\((.*)\\).*" + SharedClassesSettings.METHOD_CALLER_LABEL + "\\s\\|\\|"
//				+ "\\sCodePos:\\s(.*)\\s\\|\\|"
//				+ "\\sMethod-Sign:\\s(\\<.*\\>)\\s\\|\\|"
//				+ "\\sInvokeExpr:\\s(.*)$";
//		Pattern pattern = Pattern.compile(regex);
//		Matcher matcher = pattern.matcher(line);
//			
//		if(matcher.find())
//		{
//			int processId = Integer.parseInt(matcher.group(1).trim());
//			String codePositionAsString = matcher.group(2);
//			long codePosition = Long.parseLong(codePositionAsString);			
//			String methodSignature = matcher.group(3);
//			String invokeExpr = matcher.group(4);
//			
//			return new MethodCallerEvent(processId, codePosition, methodSignature, invokeExpr);
//		}
//		else{
//			return null;
//		}
//	}
//	
//	
//	private ReturnEvent handleReturnStmt(String line)
//	{				
//		String regex = ".*\\((.*)\\).*" + SharedClassesSettings.RETURN_LABEL + ":\\s(<.*>)" + "\\s\\|\\|"
//				+ "\\sCodePos:\\s(.*)\\s\\|\\|"
//				+ "\\sMethod-Sign:\\s(.*)\\s\\|\\|"
//				+ "\\sreturn\\s(.*)$";
//		Pattern pattern = Pattern.compile(regex);
//		Matcher matcher = pattern.matcher(line);
//		
//		if(matcher.find())
//		{
//			int processId = Integer.parseInt(matcher.group(1).trim());
//			String codePositionAsString = matcher.group(2);
//			long codePosition = Long.parseLong(codePositionAsString);
//			String methodSignature = matcher.group(3);
//			String returnValue = matcher.group(4).trim();
//			return new ReturnEvent(processId, codePosition, methodSignature, returnValue);
//		}
//		else{
//			return null;
//		}
//	}
//	
//	
//	private MethodCalleeEvent handleMethodCalleeEvent(String line)
//	{
//		String regex = ".*\\((.*)\\).*" + SharedClassesSettings.METHOD_CALLEE_LABEL + ":\\s(<.*>)$";
//		Pattern pattern = Pattern.compile(regex);
//		Matcher matcher = pattern.matcher(line);
//		
//		if(matcher.find())
//		{
//			int processId = Integer.parseInt(matcher.group(1).trim());
//			String methodSignature = matcher.group(2);
//			return new MethodCalleeEvent(processId, methodSignature);
//		}
//		else{
//			return null;
//		}
//	}
//	
//}
