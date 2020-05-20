package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import java.util.Optional;

import org.anchoranalysis.core.geometry.Point3i;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;

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
	
	public void writePoint( Point3i pnt ) {
		// We write a connected component id based upon the first
		if (id==-1) {
			id = matS.extnt().offset(pnt);
			
			if (minimaStore.isPresent()) {
				minimaStore.get().add(pnt);
			}
		}
		
		matS.setPointConnectedComponentID(pnt, id);
	}
}