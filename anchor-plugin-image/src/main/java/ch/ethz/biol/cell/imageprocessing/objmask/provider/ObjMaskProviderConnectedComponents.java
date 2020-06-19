package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.unitvalue.areavolume.UnitValueAreaOrVolume;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolumeVoxels;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.unitvalue.UnitValueException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.apache.commons.lang.time.StopWatch;

/**
 * Converts a binary-mask into its connected components
 * 
 * @author feehano
 *
 */
public class ObjMaskProviderConnectedComponents extends ObjMaskProvider {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryChnlProvider binaryChnl;
	
	@BeanField
	private UnitValueAreaOrVolume minVolume = new UnitValueVolumeVoxels(1);
	
	@BeanField
	private boolean bySlices = false;
	
	/** If true uses 8 nghb rather than 4 nghb etc. in 2D, and similar in 3D */
	@BeanField
	private boolean bigNghb = false;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection create() throws CreateException {
	
		BinaryChnl bi = binaryChnl.create();
		
		StopWatch sw = new StopWatch();
		sw.start();
		
		try {
			int minNumberVoxels = (int) Math.round(
				minVolume.rslv(
					Optional.of(bi.getDimensions().getRes())
				)
			);
			
			if (bySlices) {
				return createObjsBySlice(bi, minNumberVoxels);
			} else {
				return createObjs3D(bi, minNumberVoxels);
			}
			
		} catch (UnitValueException e) {
			throw new CreateException(e);
		}
	}
	
	private CreateFromConnectedComponentsFactory createFactory( int minNumberVoxels ) {
		return new CreateFromConnectedComponentsFactory(bigNghb, minNumberVoxels);
	}
	
	private ObjectCollection createObjs3D( BinaryChnl bi, int minNumberVoxels ) throws CreateException {
		CreateFromConnectedComponentsFactory createObjMasks = createFactory(minNumberVoxels);
		return createObjMasks.createConnectedComponents(bi);
	}
	
	private ObjectCollection createObjsBySlice( BinaryChnl bi, int minNumberVoxels ) throws CreateException {
		
		ObjectCollection out = new ObjectCollection();
		CreateFromConnectedComponentsFactory createObjMasks = createFactory(minNumberVoxels);

		for( int z=0; z<bi.getDimensions().getZ(); z++) {
		
			VoxelBox<ByteBuffer> vb = bi.getVoxelBox().extractSlice(z);
			BinaryVoxelBox<ByteBuffer> bvb = new BinaryVoxelBoxByte(vb,bi.getBinaryValues());
			
			out.addAll(
				createForSlice(createObjMasks, bvb, z)
			);
		}
		
		return out;
	}
	
	private ObjectCollection createForSlice(
		CreateFromConnectedComponentsFactory createObjMasks,
		BinaryVoxelBox<ByteBuffer> bvb,
		int z
	) throws CreateException {
		// respecify the z
		return createObjMasks.createConnectedComponents(bvb).stream().mapBoundingBox( bbox->
			bbox.shiftToZ(z)
		);
	}

	public UnitValueAreaOrVolume getMinVolume() {
		return minVolume;
	}

	public void setMinVolume(UnitValueAreaOrVolume minVolume) {
		this.minVolume = minVolume;
	}

	public boolean isBySlices() {
		return bySlices;
	}

	public void setBySlices(boolean bySlices) {
		this.bySlices = bySlices;
	}

	public boolean isBigNghb() {
		return bigNghb;
	}

	public void setBigNghb(boolean bigNghb) {
		this.bigNghb = bigNghb;
	}

	public BinaryChnlProvider getBinaryChnl() {
		return binaryChnl;
	}

	public void setBinaryChnl(BinaryChnlProvider binaryChnl) {
		this.binaryChnl = binaryChnl;
	}
}
