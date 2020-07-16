/* (C)2020 */
package org.anchoranalysis.plugin.points.calculate.ellipse;

import lombok.Value;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.image.object.ObjectMask;

@Value
public class ObjectWithEllipse {

    private final ObjectMask object;
    private final MarkEllipse mark;
}
