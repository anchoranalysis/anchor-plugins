/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import org.anchoranalysis.image.orientation.Orientation;

// Calculates the eccentricity of the ellipse
// If it's an ellipsoid it calculates the Meridional Eccentricity i.e. the eccentricity
//    of the ellipse that cuts across the plane formed by the longest and shortest axes
public class EccentricityAxisAligned extends FeatureMarkEccentricity {

    @Override
    protected double calcEccentricityForEllipsoid(double[] radii, Orientation orientation) {
        double e0 = calcEccentricity(radii[1], radii[0]);
        double e1 = calcEccentricity(radii[2], radii[1]);
        double e2 = calcEccentricity(radii[2], radii[0]);
        return (e0 + e1 + e2) / 3;
    }
}
