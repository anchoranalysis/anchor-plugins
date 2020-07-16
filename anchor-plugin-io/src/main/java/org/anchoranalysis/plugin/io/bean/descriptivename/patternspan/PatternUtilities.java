/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename.patternspan;

import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class PatternUtilities {

    /** This is a hack to extract the constant value from the element */
    public static String constantElementAsString(PatternElement element) {
        return element.describe(10000000);
    }
}
