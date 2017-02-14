package de.tu_darmstadt.sse.decisionmaker.analysis.stringtotypeextractor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import de.tu_darmstadt.sse.apkspecific.CodeModel.CodePositionManager;

public class StringToPrimitiveTypeExtractorDataflowHandler implements ResultsAvailableHandler{
	CodePositionManager codePositionManager = CodePositionManager.getCodePositionManagerInstance();
	private final Map<Integer, Set<Object>> valuesToFuzz;
	
	public StringToPrimitiveTypeExtractorDataflowHandler(Map<Integer, Set<Object>> valuesToFuzz) {
		this.valuesToFuzz = valuesToFuzz;
	}
	
	@Override
	public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) {
		for(ResultSinkInfo sinkInfo : results.getResults().keySet()) {
			Stmt sink = sinkInfo.getSink();
			InvokeExpr sinkExpr = sink.getInvokeExpr();
			SootMethod sinkMethod = sinkExpr.getMethod();
			
			Set<Object> values = new HashSet<Object>();			
			
			switch(sinkMethod.getSignature()) {
				case "<java.lang.Boolean: boolean parseBoolean(java.lang.String)>":
					values.add("true");
					values.add("false");
					break;
				
				//we add two random values
				case "<java.lang.Byte: byte parseByte(java.lang.String)>":
					values.add("0");
					values.add("42");
					break;
					
					//we add two random values
				case "<java.lang.Byte: byte parseByte(java.lang.String, int)>":
					values.add("0");
					values.add("42");
					break;
				
				//we add two random values
				case "<java.lang.Short: short parseShort(java.lang.String)>":
					values.add("0");
					values.add("42");
					break;
					
					//we add two random values
				case "<java.lang.Short: short parseShort(java.lang.String, int)>":
					values.add("0");
					values.add("42");
					break;
					
				//we add two random values
				case "<java.lang.Integer: int parseInteger(java.lang.String)>":
					values.add("0");
					values.add("42");
					break;
					
					//we add two random values
				case "<java.lang.Integer: int parseInteger(java.lang.String, int)>":
					values.add("0");
					values.add("42");
					break;
					
					//we add two random values
				case "<java.lang.Long: long parseLong(java.lang.String)>":
					values.add("0");
					values.add("42");
					break;
					
					//we add two random values
				case "<java.lang.Long: long parseLong(java.lang.String, int)>":
					values.add("0");
					values.add("42");
					break;
					
				//we add two random values
				case "<java.lang.Double: double parseDouble(java.lang.String)>":
					values.add("0");
					values.add("42.0");
					break;
					
				//we add two random values
				case "<java.lang.Float: float parseFloat(java.lang.String)>":
					values.add("0");
					values.add("20.75f");
					break;					
			}						
			
			//all sources
			Set<ResultSourceInfo> sourceInfos = results.getResults().get(sinkInfo);
			for(ResultSourceInfo sourceInfo : sourceInfos) {
				Stmt source = sourceInfo.getSource();
				int sourceID = codePositionManager.getCodePositionForUnit(source).getID();
				valuesToFuzz.put(sourceID, values);
			}
		}
		
	}

}
