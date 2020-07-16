/* (C)2020 */
package org.anchoranalysis.io.manifest.reportfeature;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;

public class RootFilePathRegEx extends ReportFeatureForManifest {

    // START BEAN PROPERTIES
    @BeanField private int groupNum;
    // END BEAN PROPERTIES

    @Override
    public String genFeatureStringFor(ManifestRecorderFile obj, Logger logger)
            throws OperationFailedException {

        // We get the last three
        Path path = obj.getRootPath();

        String sep = File.separatorChar == '\\' ? "\\\\" : "/";

        Pattern p =
                Pattern.compile(
                        ".*" + sep + "-?(.+)" + sep + "(.+)" + sep + "(.+)" + sep + "(.+)$");

        Matcher m = p.matcher(path.toString());

        if (!m.matches()) {
            throw new OperationFailedException("Does not match reg ex");
        }

        if (m.groupCount() != 4) {
            throw new OperationFailedException(
                    "Does not match reg ex (incorrect number of groups)");
        }

        if (groupNum == 0) {
            return idFromRegEx(m);
        }

        return m.group(groupNum);
    }

    @Override
    public boolean isNumeric() {
        return groupNum >= 2;
    }

    public int getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(int groupNum) {
        this.groupNum = groupNum;
    }

    @Override
    public String genTitleStr() throws OperationFailedException {
        switch (groupNum) {
            case 0:
                return "id";
            case 1:
                return "set";
            case 2:
                return "group";
            case 3:
                return "imageid";
            case 4:
                return "objid";
            default:
                throw new OperationFailedException("groupNum must be between 0 and 4 inclusive");
        }
    }

    private String idFromRegEx(Matcher m) {
        return String.format("%s_%s_%s_%s", m.group(1), m.group(2), m.group(3), m.group(4));
    }
}
