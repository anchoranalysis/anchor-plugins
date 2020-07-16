/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;

/**
 * An append CSV and corresponding match value
 *
 * @author Owen Feehan
 */
public class MatchedAppendCsv extends AnchorBean<MatchedAppendCsv> {

    // START BEAN FIELDS
    @BeanField @Getter @Setter private AppendCsv appendCsv;

    @BeanField @Getter @Setter private String match;
    // END BEAN FIELDS
}
