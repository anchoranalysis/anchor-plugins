package org.anchoranalysis.plugin.quick.bean.input;

import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;
import org.anchoranalysis.plugin.quick.bean.input.filepathappend.MatchedAppendCsv;
import lombok.Getter;
import lombok.Setter;

/**
 * A base for <i>quick</i> managers that make various assumptions about file-structure.
 * 
 * <p>This provides a quicker means to specify certain projects.
 * 
 * @author Owen Feehan
 *
 * @param <T> input-type
 */
public abstract class QuickBase<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    /** If non-empty then a rooted filesystem is used with this root */
    @BeanField @AllowEmpty @Getter @Setter private String rootName = "";
    
    /** A path to the main channel of each file */
    @BeanField @Getter @Setter private FileProviderWithDirectory fileProvider;
    
    @BeanField @Getter @Setter
    private DescriptiveNameFromFile descriptiveNameFromFile = new LastFolders();
    
    /** If set, a CSV is read with two columns: the names of images and a */
    @BeanField @OptionalBean @Getter @Setter private MatchedAppendCsv filterFilesCsv;
    
    /**
     * A regular-expression applied to the image file-path that matches three groups. The first
     * group should correspond to the unique name of the top-level owner The second group should
     * correspond to the unique name of the dataset. The third group should correspond to the unique
     * name of the experiment.
     */
    @BeanField @OptionalBean @Getter @Setter private String regex;
    // END BEAN PROPERTIES
    
    protected InputManager<FileInput> fileInputManager() throws BeanMisconfiguredException {
        return InputManagerFactory.createFiles(
                        rootName,
                        fileProvider,
                        descriptiveNameFromFile,
                        regex,
                        filterFilesCsv);
    }
}
