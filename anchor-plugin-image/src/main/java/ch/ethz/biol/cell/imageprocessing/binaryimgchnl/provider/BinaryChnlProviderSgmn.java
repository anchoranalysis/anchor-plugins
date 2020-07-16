package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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

import java.nio.ByteBuffer;
import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderSgmn extends BinaryChnlProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField
	private BinarySegmentation sgmn;
	
	@BeanField @OptionalBean 
	private HistogramProvider histogramProvider;
	
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	// END BEAN PROPERTIES
	
	@Override
	protected Mask createFromSource(Channel chnlSource) throws CreateException {
		return new Mask(
				sgmnResult(chnlSource),
				chnlSource.getDimensions().getRes(),
				ChannelFactory.instance().get(VoxelDataTypeUnsignedByte.INSTANCE)
			);
	}
	
	private BinaryVoxelBox<ByteBuffer> sgmnResult(Channel chnl) throws CreateException {
		Optional<ObjectMask> omMask = mask(chnl.getDimensions());
		
		BinarySegmentationParameters params = createParams(chnl.getDimensions()); 

		try {
			return sgmn.sgmn(chnl.getVoxelBox(), params, omMask);
		
		} catch (SegmentationFailedException e) {
			throw new CreateException(e);
		}
	}

	private BinarySegmentationParameters createParams(ImageDimensions dim) throws CreateException {
		return new BinarySegmentationParameters(
			dim.getRes(),
			OptionalFactory.create(histogramProvider)
		);
	}
	
	private Optional<ObjectMask> mask(ImageDimensions dim) throws CreateException {
		Optional<Mask> maskChnl = ChnlProviderNullableCreator.createOptionalCheckSize(mask, "mask", dim);
		return maskChnl.map( chnl->
			new ObjectMask(chnl.binaryVoxelBox())
		);
	}

	public BinarySegmentation getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySegmentation sgmn) {
		this.sgmn = sgmn;
	}

	public HistogramProvider getHistogramProvider() {
		return histogramProvider;
	}

	public void setHistogramProvider(HistogramProvider histogramProvider) {
		this.histogramProvider = histogramProvider;
	}

	public BinaryChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}
}
