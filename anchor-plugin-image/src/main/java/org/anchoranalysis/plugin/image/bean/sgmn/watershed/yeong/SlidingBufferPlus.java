package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import java.util.Optional;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.SteepestCalc;

/** A sliding-buffer enhanced with other elements of internal state used to "visit" a pixel */
final class SlidingBufferPlus {
	
	private final SlidingBuffer<?> rbb;
	private final FindEqualVoxels findEqualVoxels;
	private final SteepestCalc steepestCalc;
	
	private final EncodedVoxelBox matS;
	private final Optional<MinimaStore> minimaStore;
	
	public SlidingBufferPlus(VoxelBox<?> vbImg, EncodedVoxelBox matS, Optional<ObjMask> mask, Optional<MinimaStore> minimaStore) {

		this.matS = matS;
		this.minimaStore = minimaStore;
		
		this.rbb = new SlidingBuffer<>( vbImg );
		
		boolean do3D = vbImg.extnt().getZ()>1;
		this.findEqualVoxels = new FindEqualVoxels( vbImg, matS, do3D, mask );
		this.steepestCalc = new SteepestCalc(rbb,matS.getEncoding(), do3D ,true, mask );
	}
	
	public SlidingBuffer<?> getSlidingBuffer() {
		return rbb;
	}
	
	public int offsetSlice(Point3i pnt) {
		return rbb.extnt().offsetSlice(pnt);
	}
	
	public int getG(int indxBuffer) {
		return rbb.getCentre().getInt(indxBuffer);
	}
	

	public EncodedIntBuffer getSPlane(int z) {
		return matS.getPixelsForPlane(z);
	}
	
	public void maybeAddMinima(Point3i pnt) {
		if (minimaStore.isPresent()) {
			minimaStore.get().addDuplicated(pnt);
		}
	}
	
	public void makePlateauAt(Point3i pnt) {
		new MakePlateauLowerComplete(
			findEqualVoxels.createPlateau(pnt),
			findEqualVoxels.isDo3D()
		).makeBufferLowerCompleteForPlateau(
			matS,
			minimaStore
		);
	}

	public int calcSteepestDescent(Point3i pnt, int val, int indxBuffer) {
		return steepestCalc.calcSteepestDescent(pnt, val, indxBuffer);
	}

	public boolean isPlateau(int code) {
		return matS.isPlateau(code);
	}

	public boolean isMinima(int code) {
		return matS.isMinima(code);
	}
}