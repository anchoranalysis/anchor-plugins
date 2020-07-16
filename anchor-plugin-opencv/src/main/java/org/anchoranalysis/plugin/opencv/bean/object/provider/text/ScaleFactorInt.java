/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.extent.Extent;

class ScaleFactorInt {

    private int x;
    private int y;

    public ScaleFactorInt(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public Point2i scale(Point2i point) {
        return new Point2i(scaledX(point.getX()), scaledY(point.getY()));
    }

    public Extent scale(Extent extent) {
        return new Extent(scaledX(extent.getX()), scaledY(extent.getY()), extent.getZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private int scaledX(int val) {
        return val * x;
    }

    private int scaledY(int val) {
        return val * y;
    }
}
