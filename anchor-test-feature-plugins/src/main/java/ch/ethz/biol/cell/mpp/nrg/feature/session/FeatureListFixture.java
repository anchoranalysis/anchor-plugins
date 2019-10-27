package ch.ethz.biol.cell.mpp.nrg.feature.session;

/*-
 * #%L
 * anchor-test-feature-plugins
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

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;

import org.anchoranalysis.bean.xml.BeanXmlLoader;
import org.anchoranalysis.bean.xml.error.BeanXmlException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;

import anchor.test.TestLoader;

public class FeatureListFixture {

	private static TestLoader loader = TestLoader.createFromExecutingJARDirectory(FeatureListFixture.class);
	
	protected static FeatureList createFromFile(String xmlPath, TestLoader loader) throws CreateException {
		Path pathStatic = loader.resolveTestPath(xmlPath);
		try {
			FeatureListProvider provider = BeanXmlLoader.loadBean( pathStatic );
			FeatureList features = provider.create();
			assertTrue( features.size() > 0 );	
			return features;
		} catch (BeanXmlException e) {
			throw new CreateException(e);
		}
		
	}
	
	/** creates a feature-list associated with the fixture
	 *  
	 * @throws CreateException 
	 * */
	public static FeatureList histogram() throws CreateException {
		return createFromFile("histogramFeatureList.xml", loader);
	}
	
	/** creates a feature-list associated with obj-mask
	 *  
	 * @throws CreateException 
	 * */
	public static FeatureList objMask() throws CreateException {
		return createFromFile("objMaskFeatureList.xml", loader);
	}	
}
