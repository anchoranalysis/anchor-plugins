package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderSgmn extends BinaryChnlProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn sgmn;
	
	@BeanField @OptionalBean 
	private HistogramProvider histogramProvider;
	
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	// END BEAN PROPERTIES
	
	@Override
	protected BinaryChnl createFromSource(Chnl chnlSource) throws CreateException {
		return new BinaryChnl(
				sgmnResult(chnlSource),
				chnlSource.getDimensions().getRes(),
				ChnlFactory.instance().get(VoxelDataTypeUnsignedByte.instance)
			);
	}
	
	private BinaryVoxelBox<ByteBuffer> sgmnResult(Chnl chnl) throws CreateException {
		Optional<ObjMask> omMask = mask(chnl.getDimensions());
		
		BinarySgmnParameters params = createParams(chnl.getDimensions()); 

		try {
			return sgmn.sgmn(chnl.getVoxelBox(), params, omMask);
		
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
	}

	private BinarySgmnParameters createParams(ImageDim dim) throws CreateException {
		return new BinarySgmnParameters(
			dim.getRes(),
			OptionalFactory.create(histogramProvider)
		);
	}
	
	private Optional<ObjMask> mask(ImageDim dim) throws CreateException {
		Optional<BinaryChnl> maskChnl = ChnlProviderNullableCreator.createOptionalCheckSize(mask, "mask", dim);
		return maskChnl.map( chnl->
			new ObjMask(chnl.binaryVoxelBox())
		);
	}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn sgmn) {
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
