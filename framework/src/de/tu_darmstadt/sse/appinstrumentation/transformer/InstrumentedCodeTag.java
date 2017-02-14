package de.tu_darmstadt.sse.appinstrumentation.transformer;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;


public class InstrumentedCodeTag implements Tag{

	public static final String name = "InstrumentedCodeTag";
	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		return null;
	}

}
