package org.anchoranalysis.anchor.bean.image.sgmn;

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

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.ObjectCollectionFactory;
import org.anchoranalysis.image.objectmask.factory.CreateFromPointsFactory;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderChnlSource;
import net.imglib2.Localizable;
import net.imglib2.algorithm.componenttree.mser.Mser;
import net.imglib2.algorithm.componenttree.mser.MserTree;
import net.imglib2.img.Img;
import net.imglib2.type.Type;

/**
 * Applies the MSER algorithm from imglib2
 * 
 * @author FEEHANO
 *
 */
public class ObjMaskProviderMser extends ObjMaskProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField
	private long minSize = 100;
	
	@BeanField
	private long maxSize = 10000000;
	
	@BeanField
	private double maxVar = 0.5;
	
	@BeanField
	private double minDiversity = 0;
	
	@BeanField
	private double delta = 1;
	// END BEAN PROPERTIES

	@SuppressWarnings("unchecked")
	@Override
	protected ObjectCollection createFromChnl(Channel chnl) throws CreateException {
		
		@SuppressWarnings("rawtypes")
		Img img = ImgLib2Wrap.wrap( chnl.getVoxelBox() );
		
		final MserTree<?> treeDarkToBright = MserTree.buildMserTree(
			img,
			delta,
			minSize,
			maxSize,
			maxVar,
			minDiversity,
			true
		);
		
		return convertOutputToObjs( treeDarkToBright );
	}
	
	private <T extends Type<T>> ObjectCollection convertOutputToObjs( MserTree<T> tree ) throws CreateException {
		return ObjectCollectionFactory.mapFrom(
			tree,
			mser -> CreateFromPointsFactory.create(
				convertPixelPoints(mser)
			)
		);		
	}
	
	
	private static List<Point3i> convertPixelPoints( Mser<?> mser ) {
		List<Point3i> out = new ArrayList<>();
		for(Localizable l : mser) {
			out.add( new Point3i( l.getIntPosition(0), l.getIntPosition(1), 0 ) );
		}
		return out;
	}

	public double getMaxVar() {
		return maxVar;
	}

	public void setMaxVar(double maxVar) {
		this.maxVar = maxVar;
	}

	public double getMinDiversity() {
		return minDiversity;
	}

	public void setMinDiversity(double minDiversity) {
		this.minDiversity = minDiversity;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public long getMinSize() {
		return minSize;
	}

	public void setMinSize(long minSize) {
		this.minSize = minSize;
	}

	public long getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}
}
