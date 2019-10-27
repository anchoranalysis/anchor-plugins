package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent;

/*
 * #%L
 * anchor-mpp
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


import java.util.List;

import org.anchoranalysis.anchor.mpp.proposer.PositionProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;

import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.pxlmark.PxlMark;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;
import ch.ethz.biol.cell.mpp.mark.regionmap.RegionMembership;
import ch.ethz.biol.cell.mpp.mark.GlobalRegionIdentifiers;


// Proposes a position from somewhere in a list of Memos, with some extra conditions
// NOT INTENDED TO BE IN A CONFIG FILE
class PositionProposerMemoList implements PositionProposer {

	private Mark markBlock;
	
	private List<PxlMarkMemo> listPxlMarkMemo;
	private int regionID;
	
	
	public PositionProposerMemoList(List<PxlMarkMemo> listPxlMarkMemo, int regionID, Mark markBlock) {
		super();
		this.listPxlMarkMemo = listPxlMarkMemo;
		this.regionID = regionID;
		this.markBlock = markBlock;
	}
	
	private static Point3d randomPosition( BoundingBox BoundingBox, RandomNumberGenerator re ) {
		
		Extent extnt = BoundingBox.extnt();
		
		int x = BoundingBox.getCrnrMin().getX() + (int) (re.nextDouble() * extnt.getX() );
		int y = BoundingBox.getCrnrMin().getY() + (int) (re.nextDouble() * extnt.getY() );
		int z = BoundingBox.getCrnrMin().getZ() + (int) (re.nextDouble() * extnt.getZ() );
		
		return new Point3d(x,y,z);
	}

	@Override
	public Point3d propose(ProposerContext context) {
		
		RegionMembership rm = context.getRegionMap().membershipForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE);
		
		byte flags = rm.flags();
		
		try {
			if (listPxlMarkMemo.size()==0) {
				return null;
			}
			
			// ASSUMES a single channel
			
			int numTries = 20;
	
			Point3d pnt;
			
			int i = 0;
			while(true) {
			
				if (i++==numTries) {
					return null;
				}
				
				// We keep randomly picking a memo from the list 
				// And randomly taking positions until we find a position that matches
				int pmmIndex = (int) (context.getRe().nextDouble() * listPxlMarkMemo.size());
				PxlMarkMemo pmm = listPxlMarkMemo.get(pmmIndex);
				PxlMark pm = pmm.doOperation();
				
				BoundingBox bbox = pm.getBoundingBox(regionID);
				pnt = randomPosition(bbox, context.getRe());
				
				int relX = (int) pnt.getX() - bbox.getCrnrMin().getX();
				int relY = (int) pnt.getY() - bbox.getCrnrMin().getY();
				int relZ = (int) pnt.getZ() - bbox.getCrnrMin().getZ();
				
				byte membershipExst = pm.getVoxelBox().getPixelsForPlane(relZ).get( bbox.extnt().offset(relX, relY) );
				
				// If it's not inside our mark, then we don't consider it
				if (!rm.isMemberFlag(membershipExst, flags)) {
					continue;
				}
				
				byte membership = markBlock.evalPntInside(pnt);
				
				// If it's inside our block mark, then we don't consider it
				if (rm.isMemberFlag(membership, flags)) {
					continue;
				}
				
				break;
			}
	
			return pnt;
		} catch (ExecuteException e) {
			context.getErrorNode().add(e);
			return null;
		}							
	}
}
