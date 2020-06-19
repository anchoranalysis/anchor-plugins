package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.anchor.overlay.bean.objmask.writer.ObjMaskWriter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.bean.objmask.writer.RGBSolidWriter;

import ch.ethz.biol.cell.imageprocessing.stack.color.ColoredObjsStackCreator;

public abstract class StackProviderRGBFromObjMaskBase extends StackProviderWithBackground {

	// START BEAN PROPERTIES
	@BeanField
	private boolean outline = false;
	
	@BeanField
	private int outlineWidth = 1;
	
	@BeanField
	private boolean force2D = false;
	// END BEAN PROPERTIES
	
	protected Stack createStack( ObjectCollection objs, ColorList colors) throws CreateException {
		return ColoredObjsStackCreator.create(
			maybeFlatten(objs),
			outline,
			outlineWidth,
			force2D,
			maybeFlattenedBackground(),
			colors
		);
	}
	
	protected DisplayStack maybeFlattenedBackground() throws CreateException {
		return backgroundStack(!force2D);
	}
	
	protected ObjectCollection maybeFlatten( ObjectCollection objs ) {
		if (force2D) {
			return objs.stream().map(ObjectMask::flattenZ);
		} else {
			return objs;
		}
	}
	
	protected ObjMaskWriter createWriter() {
		if (outline) {
			return new RGBOutlineWriter(outlineWidth,force2D);
		} else {
			return new RGBSolidWriter();
		}
	}
	
	public boolean isOutline() {
		return outline;
	}

	public void setOutline(boolean outline) {
		this.outline = outline;
	}


	public int getOutlineWidth() {
		return outlineWidth;
	}

	public void setOutlineWidth(int outlineWidth) {
		this.outlineWidth = outlineWidth;
	}

	public boolean isForce2D() {
		return force2D;
	}

	public void setForce2D(boolean force2d) {
		force2D = force2d;
	}
}
