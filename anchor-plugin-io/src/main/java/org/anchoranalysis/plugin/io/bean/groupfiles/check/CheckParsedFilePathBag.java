/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.groupfiles.check;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;

public abstract class CheckParsedFilePathBag extends AnchorBean<CheckParsedFilePathBag> {

    public abstract boolean accept(ParsedFilePathBag parsedBag);
}
