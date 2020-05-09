package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
