package org.anchoranalysis.plugin.io.manifest;



/*
 * #%L
 * anchor-io
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
import java.util.Optional;

import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathDifferenceFromFolderPath;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.manifest.ManifestRecorder;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;

import lombok.Getter;

// A file manifest together with the overall manifest for the experiment
public class CoupledManifests implements InputFromManager {

	@Getter
	private final Optional<ManifestRecorder> experimentManifest;
	
	@Getter
	private final ManifestRecorderFile fileManifest;
	
	private final String name;
	
	public CoupledManifests(
		ManifestRecorder experimentManifest,
		ManifestRecorderFile fileManifest,
		LogErrorReporter logger
	) throws AnchorIOException {
		super();
		this.experimentManifest = Optional.of(experimentManifest);
		this.fileManifest = fileManifest;
		name = generateName(logger);
	}
	
	public CoupledManifests(
		ManifestRecorderFile fileManifest,
		int numFoldersInDescription,
		LogErrorReporter logger
	) {
		super();
		this.experimentManifest = Optional.empty();
		this.fileManifest = fileManifest;
		name = generateNameFromFolders(numFoldersInDescription,logger);
	}
	
	private String generateName(LogErrorReporter logger) throws AnchorIOException {
			
		if (experimentManifest.isPresent()) {
			Path experimentRootFolder = getExperimentManifest().get().getRootFolder().calcPath();
			
			FilePathDifferenceFromFolderPath ff = new FilePathDifferenceFromFolderPath();
			ff.init(experimentRootFolder,fileManifest.getRootPath());
			return ff.getRemainderCombined().toString();
			
		} else {
			return generateNameFromFolders(0, logger);
		}
	}
	
	private String generateNameFromFolders(
		int numFoldersInDescription,
		LogErrorReporter logger
	) {
		LastFolders dnff = new LastFolders();
		dnff.setNumFoldersInDescription(numFoldersInDescription);
		dnff.setRemoveExtensionInDescription(false);
		return dnff.descriptiveNameFor(
			fileManifest.getRootPath().toFile(),
			"<unknown>",
			logger
		).getDescriptiveName();
	}

	@Override
	public String descriptiveName() {
		return name;
	}

	@Override
	public Optional<Path> pathForBinding() {
		return Optional.of(
			fileManifest.getRootPath()
		);
	}
}