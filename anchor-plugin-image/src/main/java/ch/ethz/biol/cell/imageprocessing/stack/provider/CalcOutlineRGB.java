package ch.ethz.biol.cell.imageprocessing.stack.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedShort;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderIfPixelZero;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class CalcOutlineRGB {


	/**
	 * creates a stack given an outline, a background, and a blue-channel
	 * 
	 * Rules.
	 *  a) Outline-boundary replaces any existing pixels, and is shown in Green
	 *  b) The blue channel is shown, only if it has higher intensity than the equivalent blue in the background
	 * 	 
	 * @param outline
	 * @param background
	 * @param blueToAssign
	 * @return
	 * @throws CreateException
	 * @throws InitException
	 */
	public static Stack apply(
		BinaryChnl outline,
		DisplayStack background,
		Channel blueToAssign,
		boolean createShort	
	) throws CreateException {
		
		if (background.getNumChnl()==3) {
			return apply(
				outline,
				background.createChnlDuplicate(0),
				background.createChnlDuplicate(1),
				background.createChnlDuplicate(2),
				blueToAssign,
				createShort
			);
		} else {
			return apply(
				outline,
				background.createChnlDuplicate(0),
				background.createChnlDuplicate(0),
				background.createChnlDuplicate(0),
				blueToAssign,
				createShort
			);
		}
	}
	
	
	public static Stack apply(
		BinaryChnl outline,
		Channel backgroundRed,
		Channel backgroundGreen,
		Channel backgroundBlue,
		Channel blueToAssign,
		boolean createShort
	) throws CreateException {

		// Duplicate background and blue
		blueToAssign = blueToAssign.duplicate();
		
		// We zero the pixels on the background and blue that are on our outline
		zeroPixels(
			outline,
			new Channel[]{ backgroundBlue, backgroundGreen, blueToAssign }
		);

		VoxelDataType outputType = createShort ? VoxelDataTypeUnsignedShort.INSTANCE : VoxelDataTypeUnsignedByte.INSTANCE;
		
		Channel chnlGreen = imposeOutlineOnChnl(outline, backgroundGreen, outputType);
		Channel chnlBlue = MaxChnls.apply( backgroundBlue, blueToAssign, outputType );
		
		return StackProviderRGBChnlProvider.createRGBStack(
			backgroundRed,
			chnlGreen,
			chnlBlue,
			outputType
		);
	}
	
	private static Channel imposeOutlineOnChnl(BinaryChnl outline, Channel chnl,VoxelDataType outputType) {
				
		double multFact = (double) outputType.maxValue() / outline.getChannel().getVoxelDataType().maxValue();
		
		return ChnlProviderIfPixelZero.mergeViaZeroCheck(
			outline.getChannel(),
			chnl,
			outputType,
			multFact
		);
	}
	
	private static void zeroPixels( BinaryChnl outline, Channel[] chnlArr ) {
		ObjectMask omOutline = CreateFromEntireChnlFactory.createObjMask(outline);
		for( Channel chnl : chnlArr ) {
			chnl.getVoxelBox().any().setPixelsCheckMask(omOutline, 0);
		}
	}
	
		
}
