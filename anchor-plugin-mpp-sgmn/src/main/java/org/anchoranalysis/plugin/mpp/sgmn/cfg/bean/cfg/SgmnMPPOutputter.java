package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgWithNrgTotal;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.generator.serialized.ObjectOutputStreamGenerator;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.generator.text.StringGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;
import org.anchoranalysis.mpp.io.cfg.generator.CfgMIPGenerator;
import org.anchoranalysis.mpp.io.cfg.generator.CfgMaskCollectionDifferentValuesGenerator;
import org.anchoranalysis.mpp.sgmn.optscheme.DualStack;

class SgmnMPPOutputter {
	
	public static void outputResults(
		CfgNRG cfgNRG,
		DualStack dualStack,
		RegionMembershipWithFlags rm,
		LogErrorReporter logger,
		BoundOutputManagerRouteErrors outputManager
	) throws OperationFailedException {
		
		ColoredCfg coloredCfg = new ColoredCfg(
			cfgNRG.getCfg(),
			outputManager.getOutputWriteSettings().genDefaultColorIndex(20),
			new IDGetterIter<Mark>()
		);
		
		ColoredCfgWithDisplayStack coloredCfgDisplayStack = new ColoredCfgWithDisplayStack(coloredCfg, dualStack.getBgStack());

		WriterRouterErrors writer = outputManager.getWriterCheckIfAllowed();
		
		writeCfg( cfgNRG, writer );
		
		writeOutline("finalOutline", coloredCfgDisplayStack, rm, writer, 1);
		writeOutline("finalOutlineVisual", coloredCfgDisplayStack, rm, writer, 3);
		
		writeCfgMaskCollection( coloredCfgDisplayStack, writer, rm );
				
		writeFinalCfg( cfgNRG.getCfg(), writer );

		outputCfgSize( cfgNRG.getCfgWithTotal(), writer, logger );
	}
	
	private static void writeCfg( CfgNRG cfgNRG, WriterRouterErrors writer ) {
		writer.write(
			"finalCfgNRG",
			() -> new XStreamGenerator<>(cfgNRG, "cfgNRG")
		);
	}
	
	private static void writeCfgMaskCollection( ColoredCfgWithDisplayStack coloredCfgDisplayStack, WriterRouterErrors writer, RegionMembershipWithFlags rm ) {
		writer.write(
			"finalCfgRaster",
			() -> new CfgMaskCollectionDifferentValuesGenerator(
				coloredCfgDisplayStack.getStack().getDimensions(),
				rm,
				coloredCfgDisplayStack.getCfg().getCfg()
			)
		);		
	}
	
	private static void writeOutline( String outputNamePrefix, ColoredCfgWithDisplayStack coloredCfgDisplayStack, RegionMembershipWithFlags rm, WriterRouterErrors writer, int outlineWidth ) {
		
		ObjMaskWriter outlineWriter = new RGBOutlineWriter(outlineWidth);
		writer.write(
			outputNamePrefix,
			() -> new CfgGenerator( outlineWriter, coloredCfgDisplayStack, new IDGetterIter<Overlay>(),  rm)
		);
		writer.write(
			outputNamePrefix + "MIP",
			() -> new CfgMIPGenerator( outlineWriter, coloredCfgDisplayStack, new IDGetterIter<Overlay>(), rm )
		);
	}
	
	private static void writeFinalCfg( Cfg cfg, WriterRouterErrors writer ) {
		writer.write(
			"finalCfg",
			() -> new XStreamGenerator<>(cfg, "cfg")
		);
		writer.write(
			"finalCfgBinary",
			() -> new ObjectOutputStreamGenerator<>(cfg, "cfg")
		);
	}
	
	private static void outputCfgSize( CfgWithNrgTotal cfgNRG, WriterRouterErrors writer, LogErrorReporter logger ) {
		writer.write(
			"cfgSize",
			() -> new StringGenerator( String.format("%d", cfgNRG.getCfg().size() ) )
		);
		logger.getLogReporter().log( "Cfg size = " + cfgNRG.getCfg().size() );
	}
}