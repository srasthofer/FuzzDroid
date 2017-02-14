package de.tu_darmstadt.sse.decisionmaker.analysis.randomFuzzer;

import java.util.Random;

import de.tu_darmstadt.sse.decisionmaker.DeterministicRandom;

public class RandomPrimitives {

	private int[] intPredefined = { -100, -3, -2, -1, 0, 1, 2, 3, 100 };
	private char[] charPredefined = { 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                    'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                    '0','1','2','3','4','5','6','7','8','9',
                     '~','`','!','@','#','$','%','^','&','*','(',')','-','_','+','=','{','}','[',']',':',';','\'','<','>',
                     ',','.','?','/','|',' ' }; // all chars found on a US keyboard, except for '\' and '"' which causes trouble in Strings 
	private float[] floatPredefined = { -100f, -3.0f, -2.0f, -1.0f, -0.1f, 0.0f, 0.1f, 1.0f, 2.0f, 3.0f, 100f };
	private double[] doublePredefined = { -100d, -3.0d, -2.0d, -1.0d, -0.1d, 0.0d, 0.1d, 1.0d, 2.0d, 3.0d, 100d };
	
	public Object next(String type) {
		if (type.equals("int") || type.equals("java.lang.Integer"))
			return nextInt();
		else if (type.equals("java.lang.String"))
			return nextString(); // we treat Strings as primitive types
		else if (type.equals("byte") || type.equals("java.lang.Byte"))
			return nextByte();
		else if (type.equals("short") || type.equals("java.lang.Short"))
			return nextShort();
		else if (type.equals("long") || type.equals("java.lang.Long"))
			return nextLong();
		else if (type.equals("char") || type.equals("java.lang.Character"))
			return nextChar();
		else if (type.equals("boolean") || type.equals("java.lang.Boolean"))
			return nextBoolean();
		else if (type.equals("float") || type.equals("java.lang.Float"))
			return nextFloat();
		else if (type.equals("double") || type.equals("java.lang.Double"))
			return nextDouble();
		else
			throw new RuntimeException("unsupported typee: " + type);
	}
	
	public boolean isSupportedType(String type) {
		if (type.equals("int") || type.equals("java.lang.Integer"))
			return true;
		else if (type.equals("java.lang.String"))
			return true; // we treat Strings as primitive typees
		else if (type.equals("byte") || type.equals("java.lang.Byte"))
			return true;
		else if (type.equals("short") || type.equals("java.lang.Short"))
			return true;
		else if (type.equals("long") || type.equals("java.lang.Long"))
			return true;
		else if (type.equals("char") || type.equals("java.lang.Character"))
			return true;
		else if (type.equals("boolean") || type.equals("java.lang.Boolean"))
			return true;
		else if (type.equals("float") || type.equals("java.lang.Float"))
			return true;
		else if (type.equals("double") || type.equals("java.lang.Double"))
			return true;
		else
			return false;
	}
	
	private boolean nextBoolean() {
		return r().nextBoolean();
	}
	
	private byte nextByte() {
		return (byte) intPredefined[r().nextInt(intPredefined.length)];
	}
	
	private char nextChar() {
		return charPredefined[r().nextInt(charPredefined.length)]; // always use predefined chars, r() chars typeically make no sense
	}
	
	private double nextDouble() {
		return doublePredefined[r().nextInt(doublePredefined.length)];
	}
	
	private float nextFloat() {
		return floatPredefined[r().nextInt(floatPredefined.length)];
	}
	
	private int nextInt() {
		return intPredefined[r().nextInt(intPredefined.length)];
	}
	
	private long nextLong() {
		return intPredefined[r().nextInt(intPredefined.length)];
	}
	
	private short nextShort() {
		return (short) intPredefined[r().nextInt(intPredefined.length)];
	}
	
	private String nextString() {
		StringBuilder sb = new StringBuilder();
		while (r().nextBoolean())
			sb.append(nextChar());
		return sb.toString();
	}
	
	private Random r() {
		return DeterministicRandom.theRandom;
	}
	
}
