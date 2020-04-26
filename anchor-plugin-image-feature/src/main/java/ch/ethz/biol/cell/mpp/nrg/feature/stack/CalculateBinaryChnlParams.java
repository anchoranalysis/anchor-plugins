package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateBinaryChnlParams extends FeatureCalculation<FeatureInputSingleObj, FeatureInputStack> {

	// None of the following are considered in hash-coding, as considered always singular for all caculations in the same session
	private BinaryImgChnlProvider binaryImgChnlProvider;
		
	public CalculateBinaryChnlParams(BinaryImgChnlProvider binaryImgChnlProvider) {
		super();
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputStack params) throws FeatureCalcException {

		try {
			BinaryChnl bc = binaryImgChnlProvider.create();
			return deriveParams(
				params,
				binaryVoxelBox(bc)
			);
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	private static FeatureInputSingleObj deriveParams(FeatureInputStack input, BinaryVoxelBox<ByteBuffer> bvb ) {
		return new FeatureInputSingleObj(
			new ObjMask(bvb),
			input.getNrgStackOptional()
		);
	}
	
	private static BinaryVoxelBox<ByteBuffer> binaryVoxelBox( BinaryChnl bic ) throws FeatureCalcException {
		VoxelBox<ByteBuffer> vb;
		try {
			vb = bic.getChnl().getVoxelBox().asByte();
		} catch (IncorrectVoxelDataTypeException e1) {
			throw new FeatureCalcException("binaryImgChnlProvider returned incompatible data type", e1);
		}
		
		return new BinaryVoxelBoxByte( vb, bic.getBinaryValues() );
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof CalculateBinaryChnlParams;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
