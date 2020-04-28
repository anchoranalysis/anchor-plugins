package ch.ethz.biol.cell.mpp.nrg.feature.stack;

import java.nio.ByteBuffer;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateBinaryChnlInput extends FeatureCalculation<FeatureInputSingleObj, FeatureInputStack> {

	private BinaryChnl chnl;
		
	public CalculateBinaryChnlInput(BinaryChnl chnl) {
		super();
		this.chnl = chnl;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputStack input) throws FeatureCalcException {
		
		BinaryVoxelBox<ByteBuffer> bvb = binaryVoxelBox(chnl);
		
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
	public boolean equals(Object obj) {
		 if(obj instanceof CalculateBinaryChnlInput){
			 final CalculateBinaryChnlInput other = (CalculateBinaryChnlInput) obj;
		        return new EqualsBuilder()
		            .append(chnl, other.chnl)
		            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(chnl)
			.toHashCode();
	}
}
