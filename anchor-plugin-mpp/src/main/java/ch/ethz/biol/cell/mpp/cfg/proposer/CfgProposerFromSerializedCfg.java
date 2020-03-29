package ch.ethz.biol.cell.mpp.cfg.proposer;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;

/*
 * #%L
 * anchor-plugin-mpp
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



import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.deserializer.ObjectInputStreamDeserializer;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;

public class CfgProposerFromSerializedCfg extends CfgProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7196359218788713244L;
	
	// START BEAN PROPERTIES
	@BeanField
	private String filePath;
	// END BEAN PROPERTIES

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipse;
	}

	@Override
	public Cfg propose(CfgGen cfgGen, ProposerContext context) {
		
		ObjectInputStreamDeserializer<Cfg> deserializer = new ObjectInputStreamDeserializer<>();
		try {
			Path path = Paths.get(filePath);
			Cfg deserializedCfg = deserializer.deserialize( path );
			
			// We apply any corrections after deserialization, as the contents of the mark might change, but the deserialized versions will not
			// TODO apply corrections
			
			return deserializedCfg;
		} catch (DeserializationFailedException e) {
			//errorNode.add( String.format("Exception when deserializing: %s", e.toString()) );
			return null;
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public String getBeanDscr() {
		return toString();
	}
}
