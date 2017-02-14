package de.tu_darmstadt.sse.apkspecific.CodeModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class CodePositionWriter {
	
	private final CodePositionManager codePositionManager;
	
	public CodePositionWriter(CodePositionManager codePositionManager) {
		this.codePositionManager = codePositionManager;
	}
	
	
	public void writeCodePositions(String fileName) throws FileNotFoundException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(fileName));
			for (String signature : codePositionManager.getMethodsWithCodePositions()) {
				writer.write("METHOD " + signature + "\n");
				writer.write("OFFSET " + codePositionManager.getMethodOffset(
						signature) + "\n");
				writer.write("\n");
			}
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}

}
