/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename;

import java.io.File;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFileIndependent;
import org.apache.commons.io.FilenameUtils;

@NoArgsConstructor
public class LastFolders extends DescriptiveNameFromFileIndependent {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int numFoldersInDescription = 0;

    @BeanField @Getter @Setter private boolean removeExtensionInDescription = true;

    @BeanField @Getter @Setter private int skipFirstNumFolders = 0;

    @BeanField @Getter @Setter private boolean skipFileName = false;
    // END BEAN PROPERTIES

    public LastFolders(int numFoldersInDescription) {
        this.numFoldersInDescription = numFoldersInDescription;
    }

    @Override
    protected String createDescriptiveName(File file, int index) {

        String nameOut = "";
        if (!skipFileName) {
            nameOut = maybeRemoveExtension(new File(file.getPath()).getName());
        }

        File currentFile = file;
        for (int i = 0; i < numFoldersInDescription; i++) {

            currentFile = currentFile.getParentFile();

            if (currentFile == null) {
                break;
            }

            if (i >= skipFirstNumFolders) {
                nameOut = prependMaybeWithSlash(currentFile.getName(), nameOut);
            }
        }
        return nameOut;
    }

    private String prependMaybeWithSlash(String prepend, String existing) {
        if (existing.isEmpty()) {
            return prepend;
        } else {
            return prepend + "/" + existing;
        }
    }

    private String maybeRemoveExtension(String path) {
        if (removeExtensionInDescription) {
            return FilenameUtils.removeExtension(path);
        } else {
            return path;
        }
    }
}
