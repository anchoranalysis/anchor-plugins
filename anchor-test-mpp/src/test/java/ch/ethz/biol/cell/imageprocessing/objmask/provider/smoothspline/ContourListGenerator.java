package ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline;

/*
 * #%L
 * anchor-mpp-io
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


import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointList;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointListFactory;
import org.anchoranalysis.anchor.overlay.Overlay;
import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.io.generator.raster.RasterGenerator;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.ShuffleColorSetGenerator;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.generator.IterableObjectGenerator;
import org.anchoranalysis.io.generator.ObjectGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;

class ContourListGenerator extends RasterGenerator implements IterableObjectGenerator<List<Contour>, Stack> {

	private CfgGenerator delegate;
	
	private List<Contour> contourList;
	private DisplayStack stack;
	private ColorIndex colorIndex;
	
	public static ColorSetGenerator DEFAULT_COLOR_SET_GENERATOR = new ShuffleColorSetGenerator( new HSBColorSetGenerator() );
	
	public ContourListGenerator( DisplayStack stack ) {
		this( new RGBOutlineWriter(1,true), null, stack );
	}
	
	public ContourListGenerator( DisplayStack stack, List<Contour> contours ) throws SetOperationFailedException {
		this( stack );
		setIterableElement(contours);
	}
	
	public ContourListGenerator( ObjMaskWriter objMaskWriter, ColorIndex colorIndex, DisplayStack stack ) {
		this.stack = stack;
		delegate = new CfgGenerator(objMaskWriter, new IDGetterIter<Overlay>() );
		delegate.setManifestDescriptionFunction("contourRepresentationRGB");
		this.colorIndex = colorIndex;
	}

	@Override
	public boolean isRGB() {
		return delegate.isRGB();
	}

	@Override
	public Stack generate() throws OutputWriteFailedException {
		return delegate.generate();
	}

	@Override
	public Optional<ManifestDescription> createManifestDescription() {
		return delegate.createManifestDescription();
	}

	private static Cfg createCfgFromContourList( List<Contour> contourList ) {
		
		Cfg cfg = new Cfg();
		
		for (Iterator<Contour> itr = contourList.iterator(); itr.hasNext();) {
			Contour c = itr.next();
			
			Mark mark = createMarkForContour(c, false);
			cfg.add(mark);
		}
		return cfg;
	}

	@Override
	public void start() throws OutputWriteFailedException {
		delegate.start();
	}

	@Override
	public void end() throws OutputWriteFailedException {
		delegate.end();
	}

	@Override
	public List<Contour> getIterableElement() {
		return contourList;
	}

	@Override
	public void setIterableElement(List<Contour> element) throws SetOperationFailedException {
		
		this.contourList = element;
		
		try {
			ColoredCfg cfg =  new ColoredCfg( createCfgFromContourList( contourList ), genColors(contourList.size()), new IDGetterIter<Mark>() );
			delegate.setIterableElement( new ColoredCfgWithDisplayStack(cfg, stack) );
			
		} catch (OperationFailedException e) {
			throw new SetOperationFailedException(e);
		}
		
	}
	
	private ColorIndex genColors(int size) throws OperationFailedException {
		if (colorIndex!=null) {
			return colorIndex;
		} else {
			return DEFAULT_COLOR_SET_GENERATOR.genColors(size);
		}
	}

	@Override
	public ObjectGenerator<Stack> getGenerator() {
		return delegate.getGenerator();
	}
	
	private static MarkPointList createMarkForContour(Contour c, boolean round ) {
		return MarkPointListFactory.createMarkFromPoints3f( c.getPoints());
	}
}
