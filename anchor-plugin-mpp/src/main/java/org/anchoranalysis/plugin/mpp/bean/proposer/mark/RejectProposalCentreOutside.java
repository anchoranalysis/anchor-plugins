package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;

/*-
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.channel.Channel;

// Rejects a proposal if its centre is not found on a particular prob map
public class RejectProposalCentreOutside extends MarkProposerOne {

	// START BEAN
	@BeanField
	private BinaryChnlProvider binaryChnl;
	// END BEAN

	private BinaryChnl binaryImgChnl;

	@Override
	public void onInit(MPPInitParams pso) throws InitException {
		super.onInit(pso);
		try {
			binaryImgChnl = binaryChnl.create();
		} catch (CreateException e) {
			throw new InitException(e);
		}
	}


	@Override
	protected boolean propose(VoxelizedMarkMemo inputMark, ProposerContext context, MarkProposer source)
			throws ProposalAbnormalFailureException {
			
		boolean succ = source.propose(inputMark, context);
		
		if (!succ) {
			return false;	
		}
		
		Point3d cp = inputMark.getMark().centerPoint();
		
		int voxelVal = getVoxelFromChnl(
			binaryImgChnl.getChannel(),
			PointConverter.intFromDouble(cp)
		);
		if ( voxelVal==binaryImgChnl.getBinaryValues().getOffInt()) {
			context.getErrorNode().add("centre outside probmap");
			return false;
		}
				
		return true;
	}
	
	private static int getVoxelFromChnl(Channel raster, Point3i pnt) {
		return raster.getVoxelBox().asByte().getVoxel(pnt);
	}

	public BinaryChnlProvider getBinaryChnl() {
		return binaryChnl;
	}


	public void setBinaryChnl(BinaryChnlProvider binaryChnl) {
		this.binaryChnl = binaryChnl;
	}
}

