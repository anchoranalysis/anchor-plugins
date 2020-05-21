package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import java.util.Optional;

import org.anchoranalysis.core.geometry.Point3i;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

/**
 * Writes all points in a particular connected-component using the same ID
 * 
 * @author Owen Feehan
 *
 */
final class ConnectedComponentWriter {

	private final EncodedVoxelBox matS;
	private final Optional<MinimaStore> minimaStore;

	/** Keeps track of the IDs used */
	private int id = -1;
	
	public ConnectedComponentWriter(EncodedVoxelBox matS, Optional<MinimaStore> minimaStore) {
		super();
		this.matS = matS;
		this.minimaStore = minimaStore;
	}
	
	/**
	 * 
	 * @param pnt a point that is treated immutably
	 */
	public void writePoint( Point3i pnt ) {
		// We write a connected component id based upon the first voxel encountered
		if (id==-1) {
			id = matS.extnt().offset(pnt);
			
			if (minimaStore.isPresent()) {
				minimaStore.get().add(
					new Point3i(pnt)	// Duplicated, as Point3i is mutable and changes over hte iteration
				);
			}
		}
		
		matS.setPointConnectedComponentID(pnt, id);
	}
}