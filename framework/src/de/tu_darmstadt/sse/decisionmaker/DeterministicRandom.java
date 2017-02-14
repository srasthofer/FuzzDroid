package de.tu_darmstadt.sse.decisionmaker;

import java.util.Random;


public class DeterministicRandom {

	public static Random theRandom = new Random(23);
	
	public static void reinitialize(int seed) {
		theRandom = new Random(seed);
	}
	
}
