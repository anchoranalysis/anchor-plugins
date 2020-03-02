package org.anchoranalysis.test.mpp;

/*
 * #%L
 * anchor-test-image
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


import java.nio.file.Path;

import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;

import ch.ethz.biol.cell.mpp.cfg.Cfg;
import ch.ethz.biol.cell.mpp.io.CfgDeserializer;

public class TestLoaderMPP {

	private TestLoader delegate;
	
	public TestLoaderMPP( TestLoader testLoader ) {
		this.delegate = testLoader;
	}
	
	public Cfg openCfgFromTestPath( String testPath ) throws TestDataLoadException {
		Path filePath = delegate.resolveTestPath( testPath);
		return openCfgFromFilePath(filePath);
	}
	
	public static Cfg openCfgFromFilePath( Path filePath ) throws TestDataLoadException {
		
		CfgDeserializer deserializer = new CfgDeserializer();
		try {
			return deserializer.deserialize( filePath );
		} catch (DeserializationFailedException e) {
			throw new TestDataLoadException(e);
		}
	}
}
