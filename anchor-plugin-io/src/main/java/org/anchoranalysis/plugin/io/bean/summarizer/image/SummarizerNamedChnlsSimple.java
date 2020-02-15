package org.anchoranalysis.plugin.io.bean.summarizer.image;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInputAsStack;

/**
 * A simple summerizer where there's one summary-item per image (series are ignored!)
 */
public abstract class SummarizerNamedChnlsSimple<T> extends SummarizerNamedChnls<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void add(NamedChnlsInputAsStack element) throws OperationFailedException {
		try {
			incrCount( extractKey(element) );
		} catch (RasterIOException e) {
			throw new OperationFailedException(e);
		}
	}
	
	protected abstract T extractKey(NamedChnlsInputAsStack element) throws RasterIOException;
}
