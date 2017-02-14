package de.tu_darmstadt.sse.decisionmaker.analysis.integritycheck;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import soot.Unit;
import de.tu_darmstadt.sse.FrameworkOptions;
import de.tu_darmstadt.sse.commandlinelogger.LoggerHelper;
import de.tu_darmstadt.sse.commandlinelogger.MyLevel;
import de.tu_darmstadt.sse.decisionmaker.analysis.AnalysisDecision;
import de.tu_darmstadt.sse.decisionmaker.analysis.FuzzyAnalysis;
import de.tu_darmstadt.sse.decisionmaker.server.ThreadTraceManager;
import de.tu_darmstadt.sse.decisionmaker.server.TraceManager;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.DecisionRequest;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.ServerResponse;
import de.tu_darmstadt.sse.sharedclasses.networkconnection.serializables.SignatureSerializableObject;


public class AntiIntegrityCheckFuzzyAnalysis extends FuzzyAnalysis{

	private final Set<SignatureSerializableObject> certificates = new HashSet<SignatureSerializableObject>();
	
	@Override
	public void doPreAnalysis(Set<Unit> targetUnits, TraceManager traceManager) {
		Set<Certificate> certs = extractCertificates();
		createSignatureSerializableObjects(certs);
	}

	@Override
	public List<AnalysisDecision> resolveRequest(DecisionRequest clientRequest,
			ThreadTraceManager completeHistory) {	
		List<AnalysisDecision> decisions = new ArrayList<AnalysisDecision>();
		if(clientRequest.getLoggingPointSignature().equals("<de.tu_darmstadt.sse.additionalappclasses.wrapper.DummyWrapper: android.content.pm.PackageInfo dummyWrapper_getPackageInfo(android.content.pm.PackageManager,java.lang.String,int)>")) {
			for(SignatureSerializableObject encodedCertificate : certificates) {
				ServerResponse response = new ServerResponse();
				response.setAnalysisName(getAnalysisName());
		        response.setResponseExist(true);
		        response.setReturnValue(encodedCertificate);
				AnalysisDecision finalDecision = new AnalysisDecision();
				finalDecision.setAnalysisName(getAnalysisName());
				finalDecision.setDecisionWeight(8);
			    finalDecision.setServerResponse(response);
			        
			    decisions.add(finalDecision);
			}
		}
		return decisions;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getAnalysisName() {
		return "IntegrityCheck";
	}
	
	
	private Set<Certificate> extractCertificates() {
		Set<Certificate> certificates = new HashSet<Certificate>(); 
		try{
			String apkPath = null;
			if(FrameworkOptions.apkPathOriginalAPK != null)
				apkPath = FrameworkOptions.apkPathOriginalAPK;
			else
				apkPath = FrameworkOptions.apkPath;
			JarFile jf = new JarFile(apkPath, true);
			 Vector<JarEntry> entriesVec = new Vector<>();
	           byte[] buffer = new byte[8192];

	           Enumeration<JarEntry> entries = jf.entries();
	           while (entries.hasMoreElements()) {
	               JarEntry je = entries.nextElement();
	               entriesVec.addElement(je);
	               InputStream is = null;
	               try {
	                   is = jf.getInputStream(je);
	                   int n;
	                   while ((n = is.read(buffer, 0, buffer.length)) != -1) {
	                       // we just read. this will throw a SecurityException
	                       // if  a signature/digest check fails.
	                   }
	               } finally {
	                   if (is != null) {
	                       is.close();
	                   }
	               }
	           }

	           Manifest man = jf.getManifest();
	           if (man != null) {
	               Enumeration<JarEntry> e = entriesVec.elements();
	               if(e.hasMoreElements()) {
	            	   JarEntry je = e.nextElement();
	            	   certificates.addAll(Arrays.asList(je.getCertificates()));
	               }	              
	           }
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		return certificates;
	}
	
	
	private void createSignatureSerializableObjects(Set<Certificate> certs) {
		for(Certificate cert : certs) {
			try {
				SignatureSerializableObject sso = new SignatureSerializableObject(cert.getEncoded());
				certificates.add(sso);
			}catch(Exception ex) {
				LoggerHelper.logEvent(MyLevel.EXCEPTION_ANALYSIS, ex.getMessage());
				ex.printStackTrace();
				System.exit(0);
			}
		}
	}

}
