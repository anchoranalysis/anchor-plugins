package org.anchoranalysis.plugin.io.bean.input.manifest;

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


import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.manifest.deserializer.CachedManifestDeserializer;
import org.anchoranalysis.io.manifest.deserializer.ManifestDeserializer;
import org.anchoranalysis.io.manifest.deserializer.SimpleManifestDeserializer;
import org.anchoranalysis.plugin.io.manifest.ManifestCouplingDefinition;

public class CoupledManifestsInputManager extends InputManager<ManifestCouplingDefinition> {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private FileProvider manifestInputFileSet;
	
	@BeanField @OptionalBean
	private FileProvider manifestExperimentInputFileSet;
	
	@BeanField
	private ManifestDeserializer manifestDeserializer;
	// END BEAN PROPERTIES
	
	private ManifestCouplingDefinition mcd = null;
	
	public CoupledManifestsInputManager() {
		manifestDeserializer = new CachedManifestDeserializer( new SimpleManifestDeserializer(), 50 );
	}
	
	@Override
	public List<ManifestCouplingDefinition> inputObjects(InputManagerParams params)
			throws AnchorIOException {
		
		try {
			if (mcd==null) {
				mcd = createDeserializedList(params);
			}
		} catch (DeserializationFailedException e) {
			throw new AnchorIOException("Deserialization failed", e);
		}
		
		return Collections.singletonList(mcd);
	}
	
	public ManifestCouplingDefinition manifestCouplingDefinition( InputManagerParams params ) throws DeserializationFailedException {
		if (mcd==null) {
			mcd = createDeserializedList( params );
		}
		return mcd;
	}
	
	// Runs the experiment on a particular file
	private ManifestCouplingDefinition createDeserializedList( InputManagerParams params ) throws DeserializationFailedException {
		
		try {
			ManifestCouplingDefinition definition = new ManifestCouplingDefinition();
			
			// Uncoupled file manifests
			if (manifestInputFileSet!=null) {
				definition.addUncoupledFiles( manifestInputFileSet.create(params), manifestDeserializer );
			}
			
			if (manifestExperimentInputFileSet!=null) {
				Collection<File> matchingFiles = manifestExperimentInputFileSet.create(params);
				definition.addManifestExperimentFileSet(  matchingFiles, manifestDeserializer );	
			}
	
			return definition;
		} catch (FileProviderException e) {
			throw new DeserializationFailedException("Cannot find files to deserialize", e);
		} catch (OperationFailedException e) {
			throw new DeserializationFailedException("Cannot added uncoupled-files to the manifest", e);
		}
	}

	public FileProvider getManifestInputFileSet() {
		return manifestInputFileSet;
	}
	public void setManifestInputFileSet(FileProvider manifestInputFileSet) {
		this.manifestInputFileSet = manifestInputFileSet;
	}
	public ManifestDeserializer getManifestDeserializer() {
		return manifestDeserializer;
	}
	public void setManifestDeserializer(ManifestDeserializer manifestDeserializer) {
		this.manifestDeserializer = manifestDeserializer;
	}

	public FileProvider getManifestExperimentInputFileSet() {
		return manifestExperimentInputFileSet;
	}

	public void setManifestExperimentInputFileSet(
			FileProvider manifestExperimentInputFileSet) {
		this.manifestExperimentInputFileSet = manifestExperimentInputFileSet;
	}

	@Override
	public void checkMisconfigured( BeanInstanceMap defaultInstances ) throws BeanMisconfiguredException {
		super.checkMisconfigured( defaultInstances );
		
		if (manifestInputFileSet==null && manifestExperimentInputFileSet==null) {
			throw new BeanMisconfiguredException("Either the manifestInputList must be populated or manifestInputFileSet must be set or the manifestExperimentInputFileSet must be set");
		}
	}

}
