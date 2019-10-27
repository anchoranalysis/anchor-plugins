package ch.ethz.biol.cell.imageprocessing.chnl.provider.level;

import org.anchoranalysis.core.error.CreateException;

/*
 * #%L
 * anchor-image-bean
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


import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.bean.threshold.calculatelevel.CalculateLevel;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.morph.MorphologicalDilation;

public class LevelResultCollectionFactory {

	public static LevelResultCollection createCollection( Chnl chnl, ObjMaskCollection objMasks, CalculateLevel calculateLevel, int numDilations, LogErrorReporter logErrorReporter ) throws CreateException {
		
		LevelResultCollection all = new LevelResultCollection(); 
		
		for( ObjMask om : objMasks ) {
			
			ObjMask omForCalculateLevel;
			
			if (logErrorReporter!=null) {
				Point3d pnt = om.centerOfGravity();
				logErrorReporter.getLogReporter().logFormatted("Creating level result %f,%f,%f", pnt.getX(), pnt.getY(), pnt.getZ() );
			}
			
			// Optional dilation
			if (numDilations!=0) {
				omForCalculateLevel = MorphologicalDilation.createDilatedObjMask( om, chnl.getDimensions().getExtnt(), chnl.getDimensions().getZ()>1, numDilations, false );
			} else {
				omForCalculateLevel = om;
			}
			
			ObjMaskCollection omcSingle = new ObjMaskCollection(omForCalculateLevel);
			
			Histogram h = HistogramFactoryUtilities.create(chnl, omcSingle);
			int level;
			try {
				level = calculateLevel.calculateLevel(h);
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
			
			//System.out.printf("Mask centre-point=%s  level=%d\n", om.getBoundingBox().midpoint().toString(), level );
			
			LevelResult res = new LevelResult(level, om, h);
			
			if (logErrorReporter!=null) {
				logErrorReporter.getLogReporter().logFormatted("Level result is %d", res.getLevel());
			}
			
			all.add(res);
		}
		return all;
	}
}
