package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.anchor.overlay.Overlay;

/*
 * #%L
 * anchor-plugin-mpp
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.output.OutputWriteFailedException;

import ch.ethz.biol.cell.imageprocessing.io.generator.raster.CfgGenerator;
import ch.ethz.biol.cell.imageprocessing.io.generator.raster.ColoredCfgWithDisplayStack;
import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.cfg.provider.CfgProvider;
import ch.ethz.biol.cell.mpp.gui.videostats.internalframe.markredraw.ColoredCfg;

public class StackProviderOutlineFromCfg extends StackProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private CfgProvider cfgProvider;
	
	@BeanField
	private StackProvider backgroundProvider;
	
	@BeanField
	private int outlineWidth = 1;
	
	@BeanField
	private ColorSetGenerator colorSetGenerator = new HSBColorSetGenerator();
	
	@BeanField
	private int regionID = 0;
	
	@BeanField
	private boolean mip=false;
	// END BEAN PROPERTIES
	
	@Override
	public Stack create() throws CreateException {

		Stack background = backgroundProvider.create();
		Cfg cfg = cfgProvider.create();
		
		try {
			ColorList colorList = colorSetGenerator.genColors(cfg.size());
			
			ColoredCfg coloredCfg = new ColoredCfg(cfg, colorList);
			
			// Created colored cfg with display stack
			ColoredCfgWithDisplayStack coloredCfgWithDisplayStack;	
			
			coloredCfgWithDisplayStack = new ColoredCfgWithDisplayStack(
				coloredCfg,
				DisplayStack.create(background)
			);
			
			CfgGenerator cfgGenerator = new CfgGenerator(
				new RGBOutlineWriter(outlineWidth),
				coloredCfgWithDisplayStack,
				new IDGetterIter<Overlay>(),
				RegionMapSingleton.instance().membershipWithFlagsForIndex(regionID)
			);
			return cfgGenerator.generate();
			
		} catch (OperationFailedException | OutputWriteFailedException e1) {
			throw new CreateException(e1);
		} 
	}

	public int getOutlineWidth() {
		return outlineWidth;
	}

	public void setOutlineWidth(int outlineWidth) {
		this.outlineWidth = outlineWidth;
	}

	public ColorSetGenerator getColorSetGenerator() {
		return colorSetGenerator;
	}

	public void setColorSetGenerator(ColorSetGenerator colorSetGenerator) {
		this.colorSetGenerator = colorSetGenerator;
	}

	public CfgProvider getCfgProvider() {
		return cfgProvider;
	}

	public void setCfgProvider(CfgProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
	}

	public StackProvider getBackgroundProvider() {
		return backgroundProvider;
	}

	public void setBackgroundProvider(StackProvider backgroundProvider) {
		this.backgroundProvider = backgroundProvider;
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public boolean isMip() {
		return mip;
	}

	public void setMip(boolean mip) {
		this.mip = mip;
	}
}
