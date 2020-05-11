package ch.ethz.biol.cell.imageprocessing.chnl.provider;

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


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactoryByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxList;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

import ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox.CreateVoxelBoxFromPixelwiseFeature;
import ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox.CreateVoxelBoxFromPixelwiseFeatureWithMask;

public class ChnlProviderPixelScore extends ChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider intensityProvider;
	
	@BeanField @OptionalBean
	private ChnlProvider gradientProvider;
	
	// We don't use {@link ChnlProiderMask} as here it's optional.
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	
	@BeanField
	private PixelScore pixelScore;
	
	@BeanField
	private List<ChnlProvider> listChnlProviderExtra = new ArrayList<>();
	
	@BeanField
	private List<HistogramProvider> listHistogramProviderExtra = new ArrayList<>();
	
	@BeanField @OptionalBean
	private KeyValueParamsProvider keyValueParamsProvider;
	// END BEAN PROPERTIES
	
	private VoxelBoxList createVoxelBoxList( Chnl chnlIntensity ) throws CreateException {
		
		VoxelBoxList listOut = new VoxelBoxList();
		
		listOut.add( chnlIntensity.getVoxelBox() );
		
		if (gradientProvider!=null) {
			listOut.add( gradientProvider.create().getVoxelBox() );
		}
		for( ChnlProvider chnlProvider : listChnlProviderExtra ) {
			VoxelBoxWrapper vbExtra = chnlProvider!=null ? chnlProvider.create().getVoxelBox() : null;
			listOut.add(vbExtra);
		}
		return listOut;
	}
	
	
	private ObjMask createMaskOrNull() throws CreateException {
		if (mask==null) {
			return null;
		}
		
		BinaryChnl binaryChnlMask = mask.create();
		Chnl chnlMask = binaryChnlMask.getChnl();
		
		return new ObjMask(
			new BoundingBox( chnlMask.getDimensions().getExtnt()),
			chnlMask.getVoxelBox().asByte(),
			binaryChnlMask.getBinaryValues()
		);
	}
	
	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnlIntensity = intensityProvider.create();
				
		VoxelBoxList listVb = createVoxelBoxList( chnlIntensity);
		List<Histogram> listHistExtra = ProviderBeanUtilities.listFromBeans(listHistogramProviderExtra);
		
		Optional<KeyValueParams> kpv;
		if (keyValueParamsProvider!=null) {
			kpv = Optional.of(keyValueParamsProvider.create());
		} else {
			kpv = Optional.empty();
		}

		ObjMask objMask = createMaskOrNull();
		
		VoxelBox<ByteBuffer> vbPixelScore;
		if (objMask!=null) {
			CreateVoxelBoxFromPixelwiseFeatureWithMask creator = new CreateVoxelBoxFromPixelwiseFeatureWithMask(
				listVb,
				kpv,
				listHistExtra
			);
			
			vbPixelScore = creator.createVoxelBoxFromPixelScore(pixelScore, objMask);
			
		} else {
			CreateVoxelBoxFromPixelwiseFeature creator = new CreateVoxelBoxFromPixelwiseFeature(
				listVb,
				kpv,
				listHistExtra
			);
				
			vbPixelScore = creator.createVoxelBoxFromPixelScore(pixelScore, getLogger() );
		}
		
		return new ChnlFactoryByte().create(vbPixelScore, chnlIntensity.getDimensions().getRes());
	}
	
	public ChnlProvider getIntensityProvider() {
		return intensityProvider;
	}

	public void setIntensityProvider(ChnlProvider intensityProvider) {
		this.intensityProvider = intensityProvider;
	}

	public ChnlProvider getGradientProvider() {
		return gradientProvider;
	}

	public void setGradientProvider(ChnlProvider gradientProvider) {
		this.gradientProvider = gradientProvider;
	}

	public PixelScore getPixelScore() {
		return pixelScore;
	}

	public void setPixelScore(PixelScore pixelScore) {
		this.pixelScore = pixelScore;
	}

	public List<ChnlProvider> getListChnlProviderExtra() {
		return listChnlProviderExtra;
	}

	public void setListChnlProviderExtra(List<ChnlProvider> listChnlProviderExtra) {
		this.listChnlProviderExtra = listChnlProviderExtra;
	}

	public List<HistogramProvider> getListHistogramProviderExtra() {
		return listHistogramProviderExtra;
	}

	public void setListHistogramProviderExtra(
			List<HistogramProvider> listHistogramProviderExtra) {
		this.listHistogramProviderExtra = listHistogramProviderExtra;
	}


	public KeyValueParamsProvider getKeyValueParamsProvider() {
		return keyValueParamsProvider;
	}


	public void setKeyValueParamsProvider(KeyValueParamsProvider keyValueParamsProvider) {
		this.keyValueParamsProvider = keyValueParamsProvider;
	}


	public BinaryChnlProvider getMask() {
		return mask;
	}


	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}
}
