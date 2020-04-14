package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.test.feature.plugins.NRGStackFixture;
import org.anchoranalysis.test.feature.plugins.objs.IntersectingCircleObjsFixture;

class MultiInputFixture {

	public static final String OBJS_NAME = "objsTest";
	
	public static MultiInput createInput() {
		MultiInput input = new MultiInput(
			"input",
			new StackAsProviderFixture(
				NRGStackFixture.create().getNrgStack().asStack(),
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
