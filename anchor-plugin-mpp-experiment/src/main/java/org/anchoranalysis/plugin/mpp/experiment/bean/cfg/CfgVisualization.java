package org.anchoranalysis.plugin.mpp.experiment.bean.cfg;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.bean.object.writer.Filled;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * Maybe writes two raster visualizations of the cfg, one solid, and one with an outline
 *   
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class CfgVisualization {

	public static void write( Cfg cfg, BoundOutputManagerRouteErrors outputManager, DisplayStack backgroundStack ) throws OperationFailedException {
		ColorIndex colorIndex = outputManager.getOutputWriteSettings().genDefaultColorIndex(cfg.size());
		
		WriterRouterErrors writeIfAllowed = outputManager.getWriterCheckIfAllowed(); 
		ColoredCfgWithDisplayStack cfgWithStack = new ColoredCfgWithDisplayStack(
			new ColoredCfg(cfg,colorIndex,new IDGetterIter<Mark>()),
			backgroundStack
		);

		writeCfgGenerator(writeIfAllowed, "solid", new Filled(), cfgWithStack );		
		writeCfgGenerator(writeIfAllowed, "outline", new Outline(), cfgWithStack );
	}
	
	private static void writeCfgGenerator( WriterRouterErrors writeIfAllowed, String outputName, DrawObject maskWriter, ColoredCfgWithDisplayStack cfgWithStack ) {
		writeIfAllowed.write(
			outputName,
			() -> new CfgGenerator(
				maskWriter,
				cfgWithStack,
				new IDGetterIter<Overlay>()
			)
		);
	}
}
