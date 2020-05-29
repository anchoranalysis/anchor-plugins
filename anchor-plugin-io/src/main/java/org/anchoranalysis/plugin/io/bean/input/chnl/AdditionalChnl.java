package org.anchoranalysis.plugin.io.bean.input.chnl;

import java.nio.file.Path;

import org.anchoranalysis.core.functional.Operation;
import org.anchoranalysis.image.io.bean.chnl.map.ImgChnlMapEntry;
import org.anchoranalysis.image.io.chnl.map.ImgChnlMap;
import org.anchoranalysis.io.error.AnchorIOException;

class AdditionalChnl {
	private String chnlName;
	private int chnlIndex;
	private Operation<Path,AnchorIOException> filePath;
	
	public AdditionalChnl(String chnlName, int chnlIndex, Operation<Path,AnchorIOException> filePath) {
		super();
		this.chnlName = chnlName;
		this.chnlIndex = chnlIndex;
		this.filePath = filePath;
	}

	public Path getFilePath() throws AnchorIOException {
		return filePath.doOperation();
	}
	
	public ImgChnlMap createChnlMap() {
		ImgChnlMap map = new ImgChnlMap();
		map.add( new ImgChnlMapEntry(chnlName, chnlIndex) );
		return map;
	}

	public String getChnlName() {
		return chnlName;
	}
}