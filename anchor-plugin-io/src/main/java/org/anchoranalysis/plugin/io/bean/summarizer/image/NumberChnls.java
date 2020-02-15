package org.anchoranalysis.plugin.io.bean.summarizer.image;

import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInputAsStack;

public class NumberChnls extends SummarizerNamedChnlsSimple<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected Integer extractKey(NamedChnlsInputAsStack element) throws RasterIOException {
		return element.numChnl();
	}

	@Override
	protected String describeNoun() {
		return "channel";
	}

	

}
