package org.anchoranalysis.plugin.io.outputmanager;

/*-
 * #%L
 * anchor-plugin-io
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

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.bean.xml.factory.AnchorDefaultBeanFactory;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.plugin.io.bean.rasterreader.BioformatsReader;
import org.anchoranalysis.plugin.io.bean.rasterreader.ReadVoxelExtentXml;
import org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats.options.ForceTimeSeriesToStack;
import org.anchoranalysis.plugin.io.bean.rasterwriter.ij.IJTiffWriter;
import org.anchoranalysis.plugin.io.rasterwriter.bioformats.ConfigureBioformatsLogging;
import org.apache.commons.configuration.beanutils.BeanHelper;

public class TestReaderWriterUtilities {

	public static void ensureRasterReader() {
		ConfigureBioformatsLogging.instance().makeSureConfigured();
		addIfMissing( RasterReader.class, createReader() );
	}
	
	public static void ensureRasterWriter() {
		addIfMissing( RasterWriter.class, new IJTiffWriter() );
	}

	private static RasterReader createReader() {
		ReadVoxelExtentXml reader = new ReadVoxelExtentXml();
		
		reader.setRasterReader(
			new BioformatsReader( new ForceTimeSeriesToStack() )
		);
		return reader;
	}
		
	private static AnchorDefaultBeanFactory getOrCreateBeanFactory() {
		if (!RegisterBeanFactories.isCalledRegisterAllPackage()) {
			return RegisterBeanFactories.registerAllPackageBeanFactories(false);
		} else {
			return (AnchorDefaultBeanFactory) BeanHelper.getDefaultBeanFactory();
		}		
	}
	
	private static void addIfMissing( Class<?> cls, Object obj ) {
		AnchorDefaultBeanFactory defaultFactory = getOrCreateBeanFactory();
		if (defaultFactory.getDefaultInstances().get(cls)==null) {
			add( defaultFactory, cls, obj );
		}
	}
	
	private static void add( AnchorDefaultBeanFactory defaultFactory, Class<?> cls, Object obj ) {
		BeanInstanceMap instanceMap = new BeanInstanceMap();
		instanceMap.put(cls, obj );
		
		defaultFactory.getDefaultInstances().addFrom(instanceMap);
	}
}
