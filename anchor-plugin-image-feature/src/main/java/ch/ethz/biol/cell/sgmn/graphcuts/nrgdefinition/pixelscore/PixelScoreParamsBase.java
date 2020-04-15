package ch.ethz.biol.cell.sgmn.graphcuts.nrgdefinition.pixelscore;

import java.util.List;
import java.util.Optional;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.histogram.Histogram;

public abstract class PixelScoreParamsBase extends PixelScoreSingleChnl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init(List<Histogram> histograms, Optional<KeyValueParams> keyValueParams) throws InitException {
		
		if (keyValueParams.isPresent()) {
			throw new InitException("This pixel-score required key-value-params to be present, but they are not");
		}
		
		setupParams(
			keyValueParams.get()
		);
	}
	
	protected abstract void setupParams(KeyValueParams keyValueParams) throws InitException;
	
	protected static double extractParamsAsDouble( KeyValueParams kpv, String key) throws InitException {
		if (!kpv.containsKey(key)) {
			throw new InitException( String.format("Key '%s' does not exist",key));
		}
		
		return Double.valueOf( kpv.getProperty(key) );
	}
}
