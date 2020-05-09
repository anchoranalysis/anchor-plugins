package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.bean.BeanInstanceMap;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class StackProviderRGBChnlProvider extends StackProvider {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private ChnlProvider chnlProviderRed;
	
	@BeanField @OptionalBean
	private ChnlProvider chnlProviderGreen;
	
	@BeanField @OptionalBean
	private ChnlProvider chnlProviderBlue;
	// END BEAN PROPERTIES
	
	@Override
	public void checkMisconfigured( BeanInstanceMap defaultInstances ) throws BeanMisconfiguredException {
		super.checkMisconfigured( defaultInstances );
			
		if (chnlProviderRed==null && chnlProviderGreen==null && chnlProviderBlue==null) {
			throw new BeanMisconfiguredException("At least one of the chnlProviderRed, chnlProviderGreen or chnlProviderBlue must be set");
		}
	}	

	private static ImageDim combineWithExisting( ImageDim existing, Chnl toCombine ) throws IncorrectImageSizeException {
		
		if (toCombine==null) {
			return existing;
		}
		
		if (existing==null) {
			return toCombine.getDimensions();
		}
		
		if (!existing.equals(toCombine.getDimensions())) {
			throw new IncorrectImageSizeException("dims are not equal");
		}
		
		return existing;
	}
	
	private static ImageDim createDimensions( Chnl chnlRed, Chnl chnlGreen, Chnl chnlBlue) throws IncorrectImageSizeException {
		
		if (chnlRed==null && chnlGreen==null && chnlBlue==null) {
			throw new IllegalArgumentException("All chnls are null");
		}
		
		ImageDim sd = null;
		sd = combineWithExisting( sd, chnlRed );
		sd = combineWithExisting( sd, chnlGreen );
		sd = combineWithExisting( sd, chnlBlue );
		return sd;
	}
	
	private static void addToStack( Stack stack, Chnl chnl, ImageDim dim, VoxelDataType outputChnlType ) throws IncorrectImageSizeException, CreateException {
		
		if (chnl==null) {
			chnl = ChnlFactory.instance().createEmptyInitialised(dim, outputChnlType);
		}
		
		if(!outputChnlType.equals(chnl.getVoxelDataType())) {
			throw new CreateException(
				String.format("Channel has a different type (%s) that the expected output-type (%s)", chnl.getVoxelDataType(), outputChnlType)
			);
		}
		
		stack.addChnl(chnl);
	}
	
	private static String voxelDataTypeString( Chnl chnl ) {
		if (chnl!=null) {
			return chnl.getVoxelDataType().toString();
		} else {
			return("empty");
		}
	}
	
	// Chooses the output type of the data
	private static VoxelDataType chooseOutputDataType( Chnl chnlRed, Chnl chnlGreen, Chnl chnlBlue) throws CreateException {
		
		VoxelDataType dataType = null;
		
		Chnl[] all = new Chnl[] { chnlRed, chnlGreen, chnlBlue };
		
		for( Chnl c : all ) {
			if (c==null) {
				continue;
			}
			
			if (dataType==null) {
				dataType = c.getVoxelDataType();
			} else {
				if (!c.getVoxelDataType().equals(dataType)) {
					String s = String.format(
						"Input channels have different voxel data types. Red=%s; Green=%s; Blue=%s",
						voxelDataTypeString(chnlRed),
						voxelDataTypeString(chnlGreen),
						voxelDataTypeString(chnlBlue)
					);
					throw new CreateException( s );
				}
			}
		}
		
		// If we have no channels, then default to unsigned 8-bit
		if (dataType==null) {
			dataType = VoxelDataTypeUnsignedByte.instance;
		}
		
		return dataType;
	}
	
	
	@Override
	public Stack create() throws CreateException {

		Chnl chnlRed = chnlProviderRed!=null ? chnlProviderRed.create() : null;
		Chnl chnlGreen = chnlProviderGreen!=null ? chnlProviderGreen.create() : null;
		Chnl chnlBlue = chnlProviderBlue!=null ? chnlProviderBlue.create() : null;
		
		VoxelDataType outputType = chooseOutputDataType(chnlRed,chnlGreen,chnlBlue);
		
		return createRGBStack( chnlRed, chnlGreen, chnlBlue, outputType );
	}
	
	public static Stack createRGBStack( Chnl chnlRed, Chnl chnlGreen, Chnl chnlBlue, VoxelDataType outputType ) throws CreateException  {
		try {
			ImageDim sd = createDimensions(chnlRed, chnlGreen, chnlBlue);
			
			Stack out = new Stack();
			addToStack( out, chnlRed, sd, outputType );
			addToStack( out, chnlGreen, sd, outputType );
			addToStack( out, chnlBlue, sd, outputType );
			return out;
		} catch (IncorrectImageSizeException e) {
			throw new CreateException(e);
		}			
	}

	public ChnlProvider getChnlProviderRed() {
		return chnlProviderRed;
	}

	public void setChnlProviderRed(ChnlProvider chnlProviderRed) {
		this.chnlProviderRed = chnlProviderRed;
	}

	public ChnlProvider getChnlProviderGreen() {
		return chnlProviderGreen;
	}

	public void setChnlProviderGreen(ChnlProvider chnlProviderGreen) {
		this.chnlProviderGreen = chnlProviderGreen;
	}

	public ChnlProvider getChnlProviderBlue() {
		return chnlProviderBlue;
	}

	public void setChnlProviderBlue(ChnlProvider chnlProviderBlue) {
		this.chnlProviderBlue = chnlProviderBlue;
	}


}
