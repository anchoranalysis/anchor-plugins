package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToUnsignedShort;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeFloat;

public abstract class ChnlProviderGradientBase extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private double scaleFactor = 1.0;
	
	/** Iff true, outputs a short channel, otherwise byte cjannel*/
	@BeanField
	private boolean outputShort=false;
		
	/**
	 * Added to all gradients (so we can store negative gradients) 
	 */
	@BeanField
	private int addSum = 0;
	// END BEAN
	
	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnlIn = chnlProvider.create();
		
		// The gradient is calculated on a float
		Chnl chnlIntermediate = ChnlFactory.instance().createEmptyInitialised(
			chnlIn.getDimensions(),
			VoxelDataTypeFloat.instance
		);
		
		GradientCalculator calculator = new GradientCalculator(
			createDimensionArr(),
			(float) scaleFactor,
			addSum
		);
		calculator.calculateGradient(
			chnlIn.getVoxelBox(),
			chnlIntermediate.getVoxelBox().asFloat()
		);
		
		return convertToOutputType(chnlIntermediate);
	}
	
	protected abstract boolean[] createDimensionArr() throws CreateException;
	
	private Chnl convertToOutputType( Chnl chnlToConvert ) {
		ChnlConverter<?> converter = outputShort ? new ChnlConverterToUnsignedShort() : new ChnlConverterToUnsignedByte();
		return converter.convert(chnlToConvert, ConversionPolicy.CHANGE_EXISTING_CHANNEL );
	}
	
	public double getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public boolean isOutputShort() {
		return outputShort;
	}

	public void setOutputShort(boolean outputShort) {
		this.outputShort = outputShort;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public int getAddSum() {
		return addSum;
	}

	public void setAddSum(int addSum) {
		this.addSum = addSum;
	}
}
