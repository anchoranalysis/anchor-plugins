package org.anchoranalysis.plugin.io.bean.output;

import org.anchoranalysis.io.output.bean.OutputManagerWithPrefixer;
import org.anchoranalysis.io.output.bean.allowed.OutputAllowed;
import org.anchoranalysis.plugin.io.bean.output.allowed.AllOutputAllowed;

public class OutputManagerPermissive extends OutputManagerWithPrefixer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean isOutputAllowed(String outputName) {
		return true;
	}

	@Override
	public OutputAllowed outputAllowedSecondLevel(String key) {
		return new AllOutputAllowed();
	}

}
