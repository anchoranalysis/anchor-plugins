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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderAssignFromKeyValueParams extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private String keyValueParamsID = "";
	
	@BeanField
	private String key;
	// END BEAN PROPERTIES
	
	@Override
	public Channel createFromChnl(Channel chnl) throws CreateException {
		
		KeyValueParams params;
		try {
			params = getInitializationParameters().getParams().getNamedKeyValueParamsCollection().getException(keyValueParamsID);
		} catch (NamedProviderGetException e) {
			throw new CreateException(
				String.format("Cannot find KeyValueParams '%s'",keyValueParamsID),
				e
			);
		}
		
		if (!params.containsKey(key)) {
			throw new CreateException( String.format("Cannot find key '%s'",key) );
		}
		
		byte valueByte = (byte) Double.parseDouble( params.getProperty(key) );
		
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();

		int volumeXY = vb.extent().getVolumeXY();
		for( int z=0; z<vb.extent().getZ(); z++) {
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			
			int offset = 0;
			while(offset<volumeXY) {
				bb.put(offset++,valueByte);
			}
		}
		
		return chnl;
	}

	public String getKeyValueParamsID() {
		return keyValueParamsID;
	}

	public void setKeyValueParamsID(String keyValueParamsID) {
		this.keyValueParamsID = keyValueParamsID;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
