package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.test.feature.plugins.objs.IntersectingCircleObjsFixture;

class MultiInputFixture {

	public static final String OBJS_NAME = "objsTest";
	
	public static MultiInput createInput( NRGStack nrgStack) {
		MultiInput input = new MultiInput(
			"input",
			new StackAsProviderFixture(
				nrgStack.asStack(),
				"someName"
			)
		);
		input.objs().add(
			OBJS_NAME,
			() -> IntersectingCircleObjsFixture.generateIntersectingObjs(4, 2, false)
		);
		
		return input;
	}
}
