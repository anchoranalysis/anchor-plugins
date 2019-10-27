package org.anchoranalysis.image.io.objs;

/*-
 * #%L
 * anchor-test-image
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

import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.anchoranalysis.image.io.objs.HelperReadWriteObjs.*;

/**
 * Writes an ObjMaskCollection to the file-system, then reads it back again,
 *   and makes sure it is identical
 *   
 * @author feehano
 *
 */
public class ObjMaskCollectionWriterTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private ObjMaskCollectionFixture fixture = new ObjMaskCollectionFixture();
		
	@Before
    public void setUp() {
		RegisterBeanFactories.registerAllPackageBeanFactories(false);
    }
	
	@Test
	public void testHdf5() throws SetOperationFailedException, DeserializationFailedException {
		testWriteRead(true);
	}
	
	@Test
	public void testTIFFDirectory() throws SetOperationFailedException, DeserializationFailedException {
		testWriteRead(false);	
	}
		
	private void testWriteRead(boolean hdf5) throws SetOperationFailedException, DeserializationFailedException {
		Path path = folder.getRoot().toPath();
		
		ObjMaskCollection objs = fixture.createMockObjs(2, 7);
		writeObjs(objs, path, generator(hdf5,false) );
		
		ObjMaskCollection objsRead = readObjs(path.resolve(TEMPORARY_FOLDER_OUT));
		
		assertTrue( objs.equalsDeep(objsRead) );
	}

}
