/*-
 * #%L
 * anchor-plugin-ij
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.imagej.bean.object.segment;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Rectangle;

/**
 * A flood fill implementation used by ImageJ's particle analyzer and floodFill() macro function.
 *
 * <p>This class implements both 4-connected and 8-connected flood fill algorithms, as well as a
 * specialized fill method for particle analysis.
 */
class IJFloodFiller {

    /** Maximum size of the stack used for flood fill. Will be increased as needed. */
    private int maxStackSize = 500;

    /** Stack for storing x-coordinates during flood fill. */
    private int[] xstack = new int[maxStackSize];

    /** Stack for storing y-coordinates during flood fill. */
    private int[] ystack = new int[maxStackSize];

    /** Current size of the stack. */
    private int stackSize;

    /** The image processor being operated on. */
    private ImageProcessor ip;

    /** Flag indicating whether the image processor is a {@link FloatProcessor}. */
    private boolean isFloat;

    /**
     * Constructs a new IJFloodFiller for the given ImageProcessor.
     *
     * @param ip the {@link ImageProcessor} to perform flood fill on
     */
    public IJFloodFiller(ImageProcessor ip) {
        this.ip = ip;
        isFloat = ip instanceof FloatProcessor;
    }

    /**
     * Performs a 4-connected flood fill starting from the given coordinates.
     *
     * @param x the x-coordinate of the starting point
     * @param y the y-coordinate of the starting point
     * @return the number of pixels filled
     */
    public int fill(int x, int y) {

        int width = ip.getWidth();
        int height = ip.getHeight();
        int color = ip.getPixel(x, y);
        fillLine(ip, x, x, y);
        int newColor = ip.getPixel(x, y);

        ip.putPixel(x, y, color);

        if (color == newColor) return 0;

        int added = 1;

        stackSize = 0;
        push(x, y);
        while (true) {
            x = popx();
            if (x == -1) {
                return added;
            }
            y = popy();
            if (ip.getPixel(x, y) != color) continue;
            int x1 = x;
            int x2 = x;
            while (ip.getPixel(x1, y) == color && x1 >= 0) x1--; // find start of scan-line
            x1++;
            while (ip.getPixel(x2, y) == color && x2 < width) x2++; // find end of scan-line
            x2--;
            added += fillLine(ip, x1, x2, y); // fill scan-line
            boolean inScanLine = false;
            for (int i = x1; i <= x2; i++) { // find scan-lines above this one
                if (!inScanLine && y > 0 && ip.getPixel(i, y - 1) == color) {
                    push(i, y - 1);
                    inScanLine = true;
                } else if (inScanLine && y > 0 && ip.getPixel(i, y - 1) != color)
                    inScanLine = false;
            }
            inScanLine = false;
            for (int i = x1; i <= x2; i++) { // find scan-lines below this one
                if (!inScanLine && y < height - 1 && ip.getPixel(i, y + 1) == color) {
                    push(i, y + 1);
                    inScanLine = true;
                } else if (inScanLine && y < height - 1 && ip.getPixel(i, y + 1) != color)
                    inScanLine = false;
            }
        }
    }

    /**
     * Performs an 8-connected flood fill starting from the given coordinates.
     *
     * @param x the x-coordinate of the starting point
     * @param y the y-coordinate of the starting point
     * @return true if the fill operation was successful, false otherwise
     */
    public boolean fill8(int x, int y) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int color = ip.getPixel(x, y);
        int wm1 = width - 1;
        int hm1 = height - 1;
        fillLine(ip, x, x, y);
        int newColor = ip.getPixel(x, y);
        ip.putPixel(x, y, color);
        if (color == newColor) return false;
        stackSize = 0;
        push(x, y);
        while (true) {
            x = popx();
            if (x == -1) return true;
            y = popy();
            int x1 = x;
            int x2 = x; // NOSONAR
            if (ip.getPixel(x1, y) == color) {
                while (ip.getPixel(x1, y) == color && x1 >= 0) x1--; // find start of scan-line
                x1++;
                while (ip.getPixel(x2, y) == color && x2 < width) x2++; // find end of scan-line
                x2--;
                fillLine(ip, x1, x2, y); // fill scan-line
            }
            if (y > 0) {
                if (x1 > 0 && ip.getPixel(x1 - 1, y - 1) == color) {
                    push(x1 - 1, y - 1);
                }
                if (x2 < wm1 && ip.getPixel(x2 + 1, y - 1) == color) {
                    push(x2 + 1, y - 1);
                }
            }
            if (y < hm1) {
                if (x1 > 0 && ip.getPixel(x1 - 1, y + 1) == color) {
                    push(x1 - 1, y + 1);
                }
                if (x2 < wm1 && ip.getPixel(x2 + 1, y + 1) == color) {
                    push(x2 + 1, y + 1);
                }
            }
            boolean inScanLine = false;
            for (int i = x1; i <= x2; i++) { // find scan-lines above this one
                if (!inScanLine && y > 0 && ip.getPixel(i, y - 1) == color) {
                    push(i, y - 1);
                    inScanLine = true;
                } else if (inScanLine && y > 0 && ip.getPixel(i, y - 1) != color)
                    inScanLine = false;
            }
            inScanLine = false;
            for (int i = x1; i <= x2; i++) { // find scan-lines below this one
                if (!inScanLine && y < hm1 && ip.getPixel(i, y + 1) == color) {
                    push(i, y + 1);
                    inScanLine = true;
                } else if (inScanLine && y < hm1 && ip.getPixel(i, y + 1) != color)
                    inScanLine = false;
            }
        }
    }

    /**
     * Specialized flood fill method used by the particle analyzer to remove interior holes from
     * particle masks.
     *
     * @param x the x-coordinate of the starting point
     * @param y the y-coordinate of the starting point
     * @param level1 the lower threshold level
     * @param level2 the upper threshold level
     * @param mask the {@link ImageProcessor} representing the mask
     * @param bounds the {@link Rectangle} representing the bounds of the particle
     */
    public void particleAnalyzerFill(
            int x, int y, double level1, double level2, ImageProcessor mask, Rectangle bounds) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        mask.setColor(0);
        mask.fill();
        mask.setColor(255);
        stackSize = 0;
        push(x, y);
        while (true) {
            x = popx();
            if (x == -1) return;
            y = popy();
            if (!inParticle(x, y, level1, level2)) continue;
            int x1 = x;
            int x2 = x; // NOSONAR
            while (inParticle(x1, y, level1, level2) && x1 >= 0) x1--; // find start of scan-line
            x1++;
            while (inParticle(x2, y, level1, level2) && x2 < width) x2++; // find end of scan-line
            x2--;
            fillLine(mask, x1 - bounds.x, x2 - bounds.x, y - bounds.y); // fill scan-line i mask
            fillLine(ip, x1, x2, y); // fill scan-line in image
            boolean inScanLine = false;
            if (x1 > 0) {
                x1--;
            }
            if (x2 < width - 1) {
                x2++;
            }
            for (int i = x1; i <= x2; i++) { // find scan-lines above this one
                if (!inScanLine && y > 0 && inParticle(i, y - 1, level1, level2)) {
                    push(i, y - 1);
                    inScanLine = true;
                } else if (inScanLine && y > 0 && !inParticle(i, y - 1, level1, level2))
                    inScanLine = false;
            }
            inScanLine = false;
            for (int i = x1; i <= x2; i++) { // find scan-lines below this one
                if (!inScanLine && y < height - 1 && inParticle(i, y + 1, level1, level2)) {
                    push(i, y + 1);
                    inScanLine = true;
                } else if (inScanLine && y < height - 1 && !inParticle(i, y + 1, level1, level2))
                    inScanLine = false;
            }
        }
    }

    /**
     * Checks if a pixel is part of the particle based on its intensity value.
     *
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @param level1 the lower threshold level
     * @param level2 the upper threshold level
     * @return true if the pixel is part of the particle, false otherwise
     */
    private boolean inParticle(int x, int y, double level1, double level2) {
        if (isFloat) return ip.getPixelValue(x, y) >= level1 && ip.getPixelValue(x, y) <= level2;
        else {
            int v = ip.getPixel(x, y);
            return v >= level1 && v <= level2;
        }
    }

    /**
     * Pushes coordinates onto the stack.
     *
     * @param x the x-coordinate to push
     * @param y the y-coordinate to push
     */
    private void push(int x, int y) {
        stackSize++;
        if (stackSize == maxStackSize) {
            int[] newXStack = new int[maxStackSize * 2];
            int[] newYStack = new int[maxStackSize * 2];
            System.arraycopy(xstack, 0, newXStack, 0, maxStackSize);
            System.arraycopy(ystack, 0, newYStack, 0, maxStackSize);
            xstack = newXStack;
            ystack = newYStack;
            maxStackSize *= 2;
        }
        xstack[stackSize - 1] = x;
        ystack[stackSize - 1] = y;
    }

    /**
     * Pops an x-coordinate from the stack.
     *
     * @return the popped x-coordinate, or -1 if the stack is empty
     */
    private int popx() {
        if (stackSize == 0) return -1;
        else return xstack[stackSize - 1];
    }

    /**
     * Pops a y-coordinate from the stack.
     *
     * @return the popped y-coordinate
     */
    private int popy() {
        int value = ystack[stackSize - 1];
        stackSize--;
        return value;
    }

    /**
     * Fills a horizontal line in the image.
     *
     * @param ip the {@link ImageProcessor} to fill the line in
     * @param x1 the starting x-coordinate of the line
     * @param x2 the ending x-coordinate of the line
     * @param y the y-coordinate of the line
     * @return the number of pixels filled
     */
    private int fillLine(ImageProcessor ip, int x1, int x2, int y) {
        // Swap if necessary
        if (x1 > x2) {
            int t = x1;
            x1 = x2;
            x2 = t;
        }

        for (int x = x1; x <= x2; x++) {
            ip.drawPixel(x, y);
        }

        return x2 - x1 + 1;
    }
}
