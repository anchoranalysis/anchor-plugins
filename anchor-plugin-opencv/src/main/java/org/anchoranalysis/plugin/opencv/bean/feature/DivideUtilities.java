package org.anchoranalysis.plugin.opencv.bean.feature;

import java.util.function.Function;

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.size.SizeXY;

class DivideUtilities {

	private DivideUtilities() {}
	
	/** Integer division of a value extracted by another value extracted */
	public static int divide(SizeXY big, SizeXY divider, Function<SizeXY,Integer> func ) {
		return func.apply(big) / func.apply(divider);
	}
		
	public static void checkDivisibleBy(SizeXY biggerNumber, SizeXY divisor, String biggerText, String divisorText) throws FeatureCalcException {
		if (!isDivisibleBy(biggerNumber.getWidth(), divisor.getWidth())) {
			throw notDivisibleException(biggerText, divisorText, "width");
		}
		
		if (!isDivisibleBy(biggerNumber.getHeight(), divisor.getHeight())) {
			throw notDivisibleException(biggerText, divisorText, "height");
		}
	}
	
	public static FeatureCalcException notDivisibleException(String biggerText, String divisorText, String propertyText) {
		return new FeatureCalcException(
			String.format("%s %s must be divisible by %s %s.", biggerText, propertyText, divisorText, propertyText)
		);
	}
		
	public static int ceilDiv( double toDivide, int divisor ) {
		return (int) Math.ceil(toDivide / divisor);
	}
		
	/** Is a big-number divisible by the divider? */
	private static boolean isDivisibleBy(int bigNumber, int divider) {
		return (bigNumber % divider)==0;
	}
}