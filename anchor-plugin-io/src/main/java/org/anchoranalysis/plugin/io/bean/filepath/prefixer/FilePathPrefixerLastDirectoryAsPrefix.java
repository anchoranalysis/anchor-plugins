package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.nio.file.Path;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;


/**
 * Looks for the last directory-name, and removes it in favour of using it as a prefix on a filename
 * 
 * <p>e.g. <pre>/a/b/c/d/e/somename.tif</pre> instead becomes <pre>/a/b/c/d/e_somename.tif</pre></p>
 * 
 *  * @author owen
 *
 */
public class FilePathPrefixerLastDirectoryAsPrefix extends FilePathPrefixerAvoidResolve {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private FilePathPrefixerAvoidResolve filePathPrefixer;
	
	@BeanField
	private String delimiter = "_";
	// END BEAN PROPERTIES
	
	@Override
	protected FilePathPrefix outFilePrefixFromPath(Path path, String descriptiveName, Path root)
			throws AnchorIOException {
		
		FilePathPrefix fpp = filePathPrefixer.outFilePrefixFromPath(path, descriptiveName, root);
		
		Path dir = fpp.getFolderPath();
		
		if (dir.getNameCount()>0) {
			
			String finalDirName = dir.getName( dir.getNameCount()-1 ).toString();
			
			// Remove the final directory from the output
			fpp.setFolderPath( fpp.getFolderPath().resolve("..").normalize() );
			
			if (fpp.getFilenamePrefix()!=null) {
				fpp.setFilenamePrefix( finalDirName + delimiter + fpp.getFilenamePrefix() );
			} else {
				fpp.setFilenamePrefix(finalDirName);
			}
			
			return fpp;
	
		} else {
			// Nothing to do
			return fpp;
		}
	}

	public FilePathPrefixerAvoidResolve getFilePathPrefixer() {
		return filePathPrefixer;
	}

	public void setFilePathPrefixer(FilePathPrefixerAvoidResolve filePathPrefixer) {
		this.filePathPrefixer = filePathPrefixer;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
}
