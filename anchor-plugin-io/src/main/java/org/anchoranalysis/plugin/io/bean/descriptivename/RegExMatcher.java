/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename;

import java.io.File;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFileIndependent;

public class RegExMatcher extends DescriptiveNameFromFileIndependent {

    // START BEAN PROPERTIES
    @BeanField private RegEx regEx;
    // END BEAN PROPERTIES

    @Override
    protected String createDescriptiveName(File file, int index) {

        String filePath = file.getPath().replace('\\', '/');

        return regEx.match(filePath)
                .map(RegExMatcher::buildStrFromComponents)
                .orElse(String.format("descriptive-name regEx match failed on %s", filePath));
    }

    private static String buildStrFromComponents(String[] components) {
        return String.join("/", components);
    }

    public RegEx getRegEx() {
        return regEx;
    }

    public void setRegEx(RegEx regEx) {
        this.regEx = regEx;
    }
}
