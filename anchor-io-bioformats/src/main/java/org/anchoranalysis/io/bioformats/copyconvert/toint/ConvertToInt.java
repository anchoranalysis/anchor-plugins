package org.anchoranalysis.io.bioformats.copyconvert.toint;

import java.nio.IntBuffer;

import org.anchoranalysis.io.bioformats.copyconvert.ConvertTo;

public abstract class ConvertToInt extends ConvertTo<IntBuffer> {

	public ConvertToInt() {
		super( wrapper-> wrapper.asInt() );
	}
		
}
