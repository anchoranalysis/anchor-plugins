package org.anchoranalysis.plugin.image.bean.object.provider.connected;

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
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.unitvalue.areavolume.UnitValueAreaOrVolume;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolumeVoxels;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.apache.commons.lang.time.StopWatch;

import lombok.Getter;
import lombok.Setter;

/**
 * Converts a binary-mask into its connected components
 * 
 * @author feehano
 *
 */
public class ConnectedComponentsFromMask extends ObjectCollectionProvider {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private BinaryChnlProvider binaryChnl;
	
	@BeanField @Getter @Setter
	private UnitValueAreaOrVolume minVolume = new UnitValueVolumeVoxels(1);
	
	@BeanField @Getter @Setter
	private boolean bySlices = false;
	
	/** If true uses 8 nghb rather than 4 nghb etc. in 2D, and similar in 3D */
	@BeanField @Getter @Setter
	private boolean bigNghb = false;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection create() throws CreateException {
	
		Mask bi = binaryChnl.create();
		
		StopWatch sw = new StopWatch();
		sw.start();
		
		try {
			int minNumberVoxels = (int) Math.round(
				minVolume.resolveToVoxels(
					Optional.of(bi.getDimensions().getRes())
				)
			);
			
			if (bySlices) {
				return createObjectsBySlice(bi, minNumberVoxels);
			} else {
				return createObjects3D(bi, minNumberVoxels);
			}
			
		} catch (UnitValueException e) {
			throw new CreateException(e);
		}
	}
	
	private CreateFromConnectedComponentsFactory createFactory( int minNumberVoxels ) {
		return new CreateFromConnectedComponentsFactory(bigNghb, minNumberVoxels);
	}
	
	private ObjectCollection createObjects3D( Mask bi, int minNumberVoxels ) throws CreateException {
		CreateFromConnectedComponentsFactory objectCreator = createFactory(minNumberVoxels);
		return objectCreator.createConnectedComponents(bi);
	}
	
	private ObjectCollection createObjectsBySlice(Mask chnl, int minNumberVoxels) throws CreateException {
		
		CreateFromConnectedComponentsFactory creator = createFactory(minNumberVoxels);
	
		return ObjectCollectionFactory.flatMapFromRange(
			0,
			chnl.getDimensions().getZ(),
			CreateException.class,
			z -> createForSlice(
				creator,
				createBinaryVoxelBox(chnl, z),
				z
			)
		);
	}
	
	private static BinaryVoxelBox<ByteBuffer> createBinaryVoxelBox(Mask chnl, int z) {
		return new BinaryVoxelBoxByte(
			chnl.getVoxelBox().extractSlice(z),
			chnl.getBinaryValues()
		);
	}
	
	private ObjectCollection createForSlice(
		CreateFromConnectedComponentsFactory objectCreator,
		BinaryVoxelBox<ByteBuffer> bvb,
		int z
	) throws CreateException {
		// respecify the z
		return objectCreator.createConnectedComponents(bvb).stream().mapBoundingBox( bbox->
			bbox.shiftToZ(z)
		);
	}
}