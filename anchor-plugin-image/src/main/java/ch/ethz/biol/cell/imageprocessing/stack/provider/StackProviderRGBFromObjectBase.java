package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.object.writer.Filled;
import org.anchoranalysis.io.bean.object.writer.Outline;

import ch.ethz.biol.cell.imageprocessing.stack.color.ColoredObjectsStackCreator;
import lombok.Getter;
import lombok.Setter;

public abstract class StackProviderRGBFromObjectBase extends StackProviderWithBackground {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private boolean outline = false;
	
	@BeanField @Getter @Setter
	private int outlineWidth = 1;
	
	@BeanField @Getter @Setter
	private boolean force2D = false;
	// END BEAN PROPERTIES
	
	protected Stack createStack( ObjectCollection objects, ColorList colors) throws CreateException {
		return ColoredObjectsStackCreator.create(
			maybeFlatten(objects),
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
	
	protected ObjectCollection maybeFlatten( ObjectCollection objects ) {
		if (force2D) {
			return objects.stream().map(ObjectMask::flattenZ);
		} else {
			return objects;
		}
	}
	
	protected DrawObject createDrawer() {
		if (outline) {
			return new Outline(outlineWidth,force2D);
		} else {
			return new Filled();
		}
	}
}
