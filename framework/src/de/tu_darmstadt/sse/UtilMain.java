package de.tu_darmstadt.sse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import de.tu_darmstadt.sse.bootstrap.InstanceIndependentCodePosition;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;


public class UtilMain {
	
	private final static String BLACKLISTED_APKS = "." + File.separator + "files" + File.separator + "blacklistedAPKs.txt";
	
	
	public static Set<InstanceIndependentCodePosition> convertUnitsToIndependentCodePosition(Set<Unit> units, InfoflowCFG cfg) {
		Set<InstanceIndependentCodePosition> positions = new HashSet<InstanceIndependentCodePosition>();
		
		for(Unit unit : units) {
			//now we have access to the CFG
			//check if target is reachable:
			if(!cfg.isReachable(unit) || cfg.getMethodOf(unit) == null) {
				LoggerHelper.logEvent(MyLevel.LOGGING_POINT, "target is not statically reachable!");
				continue;
			}
			
			
			String methodSig = cfg.getMethodOf(unit).getSignature();
			int lineNum = generateLineNumberOfUnit(unit, cfg);
			String statement = unit.toString();
			InstanceIndependentCodePosition position = new InstanceIndependentCodePosition(methodSig, lineNum, statement);
			positions.add(position);
		}
		
		return positions;
	}
	
	
	public static Unit convertIndependentCodePositionToUnits(InstanceIndependentCodePosition codePos) {
		Unit unit = null;
		
		SootMethod sm = Scene.v().getMethod(codePos.getMethodSignature());
		if(sm != null) {
			int currentLineNum = 0;
			for (Iterator<Unit> unitIt = sm.getActiveBody().getUnits().iterator(); unitIt.hasNext(); ) {
				Unit currentUnit = unitIt.next();
				if(currentLineNum == codePos.getLineNumber() && currentUnit.toString().equals(codePos.getStatement()))
					unit = currentUnit;
				currentLineNum++;
			}
		}
				
		
		return unit;
	}
	
	
	private static int generateLineNumberOfUnit(Unit unit, InfoflowCFG cfg) {
		SootMethod sm = cfg.getMethodOf(unit);
		
		if(sm == null)
			return -1;
		
		int lineNum = 0;
		for (Iterator<Unit> unitIt = sm.getActiveBody().getUnits().iterator(); unitIt.hasNext(); ) {
			Unit currentUnit = unitIt.next();
			// Is this the statement
			if(unit == currentUnit)
				return lineNum;			
			lineNum++;
		}
		
		return -1;
	}
	
	
	public static Set<String> getBlacklistedAPKs() {
		Set<String> blacklisted = new HashSet<String>();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(BLACKLISTED_APKS));
		    try {
		        String line;
		        while ((line = br.readLine()) != null) {
		        	if(!line.startsWith("%"))
		        		blacklisted.add(line);
		        }
		    } finally {
		        br.close();
		    }
		}catch(Exception ex) {
			LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}
		
		return blacklisted;
	}
	
	
	public static void writeToFile(String fileName, String content) {
		File outputFile = new File(fileName);
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
			
			fw = new FileWriter(outputFile.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);	
			bw.write(content);
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {		
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
