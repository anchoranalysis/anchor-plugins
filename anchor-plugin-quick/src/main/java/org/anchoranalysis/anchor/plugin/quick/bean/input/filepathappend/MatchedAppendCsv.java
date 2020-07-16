/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;

/**
 * An append CSV and corresponding match value
 *
 * @author Owen Feehan
 */
public class MatchedAppendCsv extends AnchorBean<MatchedAppendCsv> {

    // START BEAN FIELDS
    @BeanField private AppendCsv appendCsv;

    @BeanField private String match;
    // END BEAN FIELDS

    public AppendCsv getAppendCsv() {
        return appendCsv;
    }

    public void setAppendCsv(AppendCsv appendCsv) {
        this.appendCsv = appendCsv;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }
}
