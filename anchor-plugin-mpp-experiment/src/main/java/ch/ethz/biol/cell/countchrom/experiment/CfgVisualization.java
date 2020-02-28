package ch.ethz.biol.cell.countchrom.experiment;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.bean.objmask.writer.RGBSolidWriter;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.writer.WriterRouterErrors;

import ch.ethz.biol.cell.imageprocessing.io.generator.raster.CfgGenerator;
import ch.ethz.biol.cell.imageprocessing.io.generator.raster.ColoredCfgWithDisplayStack;
import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.gui.videostats.internalframe.markredraw.ColoredCfg;


/**
 * Maybe writes two raster visualizations of the cfg, one solid, and one with an outline
 *   
 * @author owen
 *
 */
class CfgVisualization {

	public static void write( Cfg cfg, BoundOutputManagerRouteErrors outputManager, DisplayStack backgroundStack ) throws OperationFailedException {
		ColorIndex colorIndex = outputManager.getOutputWriteSettings().genDefaultColorIndex(cfg.size());
		
		WriterRouterErrors writeIfAllowed = outputManager.getWriterCheckIfAllowed(); 
		ColoredCfgWithDisplayStack cfgWithStack = new ColoredCfgWithDisplayStack(
			new ColoredCfg(cfg,colorIndex,new IDGetterIter<Mark>()),
			backgroundStack
		);

		writeCfgGenerator(writeIfAllowed, "solid", new RGBSolidWriter(), cfgWithStack );		
		writeCfgGenerator(writeIfAllowed, "outline", new RGBOutlineWriter(), cfgWithStack );
	}
	
	private static void writeCfgGenerator( WriterRouterErrors writeIfAllowed, String outputName, ObjMaskWriter maskWriter, ColoredCfgWithDisplayStack cfgWithStack ) {
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
