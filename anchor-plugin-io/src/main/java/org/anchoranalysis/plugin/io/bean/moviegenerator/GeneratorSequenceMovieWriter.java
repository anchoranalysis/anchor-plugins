package org.anchoranalysis.plugin.io.bean.moviegenerator;

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


import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.index.SetOperationFailedException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.bean.moviewriter.MovieWriter;
import org.anchoranalysis.image.io.movie.MovieOutputHandle;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.rgb.RGBStack;
import org.anchoranalysis.io.generator.IterableObjectGenerator;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncremental;
import org.anchoranalysis.io.manifest.sequencetype.SequenceType;
import org.anchoranalysis.io.manifest.sequencetype.SequenceTypeException;
import org.anchoranalysis.io.namestyle.OutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundOutputManager;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;


class GeneratorSequenceMovieWriter<GeneratorType> implements GeneratorSequenceNonIncremental<GeneratorType> {

	private BoundOutputManager outputManager = null;
	
	private IterableObjectGenerator<GeneratorType,Stack> iterableGenerator;
	
	private boolean firstAdd = true;
	
	private int totalNumAdd = -1;
	
	private int framesPerSecond = 10;
	
	private SequenceType sequenceType;
	
	private OutputNameStyle outputName;
	
	private Optional<MovieOutputHandle> movieOutputHandle;
	
	// Automatically create a ManifestDescription for the folder from the Generator
	public GeneratorSequenceMovieWriter( BoundOutputManager outputManager, OutputNameStyle outputName, IterableObjectGenerator<GeneratorType,Stack> iterableGenerator, int framesPerSecond ) {
		this.outputManager = outputManager;
		this.iterableGenerator = iterableGenerator;
		this.outputName = outputName;
		this.framesPerSecond = framesPerSecond;
	}
	
	
	


	private Optional<MovieOutputHandle> initOnFirstAdd( ImageDim dim, int numFrames ) throws InitException {
		
		// Assume we are incrementing + 1
		
		// we fix this later
		
		// We calculate the number of frames from the sequenceType
		
		
		
		try {
			//String filePath = outputManager.writeGenerateFilename( outputName.getOutputName(), writer.getDefaultFileExt(), null, "", "", "");
			return writeMovie(
				outputManager,
				outputName,
				dim,
				numFrames,
				3,
				framesPerSecond
			);
			
		} catch (OutputWriteFailedException e) {
			throw new InitException(e);
		}
	}
	
	private static Optional<MovieOutputHandle> writeMovie( BoundOutputManager outputManager, OutputNameStyle outputNameStyle, ImageDim dim, int numFrames, int numChnl, int framesPerSecond ) throws OutputWriteFailedException {
		
		MovieWriter movieWriter = (MovieWriter) outputManager.getOutputWriteSettings().getWriterInstance(MovieWriter.class);
		
		try {
			Optional<Path> filePath = outputManager.getWriterCheckIfAllowed().writeGenerateFilename(
				outputNameStyle.getOutputName(),
				movieWriter.getDefaultFileExt(),
				Optional.empty(),
				"",
				"",
				""
			);
			if (filePath.isPresent()) {
				return Optional.of(
					movieWriter.writeMovie(
						filePath.get(),
						dim,
						numFrames,
						numChnl,
						framesPerSecond
					)
				);
			} else {
				return Optional.empty();
			}
		} catch (IOException e) {
			throw new OutputWriteFailedException(e);
		}
	}


	@Override
	public void start( SequenceType sequenceType, int totalNumAdd ) throws OutputWriteFailedException {
		
		if (totalNumAdd<0) {
			throw new OutputWriteFailedException("Number of additions must be known in advance");
		}
		iterableGenerator.start();
		this.sequenceType = sequenceType;
		this.totalNumAdd = totalNumAdd;
	}

	@Override
	public void end() throws OutputWriteFailedException {
		iterableGenerator.end();
		
		try {
			if (movieOutputHandle.isPresent()) {
				movieOutputHandle.get().close();
			}
		} catch (IOException e) {
			throw new OutputWriteFailedException(e);
		}
	}

	@Override
	public void add(GeneratorType element, String index)
			throws OutputWriteFailedException {

		try {
			
			iterableGenerator.setIterableElement( element );
	
			if (!firstAdd && !movieOutputHandle.isPresent()) {
				return;
			}
			
			RGBStack stack = new RGBStack( iterableGenerator.getGenerator().generate() );
			
			// We delay the initialisation of subFolder until the first iteration and we have a valid generator
			if (firstAdd==true) {
				movieOutputHandle = initOnFirstAdd( stack.getChnl(0).getDimensions(), totalNumAdd );
				firstAdd = false;
			}
	
			if (!movieOutputHandle.isPresent()) {
				return;
			}
			
			sequenceType.update( index );
	
			if (!outputManager.isOutputAllowed(outputName.getOutputName())) {
				return;
			}
		
			movieOutputHandle.get().add(stack);

		} catch (IOException | SequenceTypeException | InitException | SetOperationFailedException | CreateException e) {
			throw new OutputWriteFailedException(e);
		}
		
	}

	@Override
	public void setSuppressSubfolder(boolean suppressSubfolder) {
		assert false;
	}
}
