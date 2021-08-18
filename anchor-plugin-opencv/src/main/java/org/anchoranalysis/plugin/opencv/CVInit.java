/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv;

import java.util.concurrent.CompletableFuture;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_cudaimgproc;
import org.bytedeco.opencv.opencv_java;

/**
 * Provides for initialization of the JavaCPP bridge to OpenCV.
 *
 * <p>Initialization is tricky as {@code Loader.load(opencv_java.class)} needs to be called once
 * before any OpenCV libraries can be used. However, this is an expensive operation that can last
 * several seconds.
 *
 * <p>To avoid delays, this class will begin the loading as soon as {@link
 * #alwaysExecuteBeforeCallingLibrary()} is called (typically from a {@code static} block in a class
 * that uses OpenCV, but in a separate thread that doesn't block other (typically non-OpenCV)
 * operations.
 *
 * <p>Non-OpenCV code continues unaffected, but OpenCV code should not be prevented from being
 * called until loading is complete. Therefore, code using the openCV libraries should always first
 * call {@link #blockUntilLoaded} before immediately before the first call to an OpenCV library.
 * This will force the thread to wait, if loading is not yet completed.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CVInit {

    /** To synchronize on for changing {@code alreadyInit}. Value is arbitrary. */
    private static final Object LOCK_ALREADY_INIT = new Object();

    /** To synchronize on for changing {@code loaded}. Value is arbitrary. */
    private static final Object LOCK_LOADED = new Object();

    private static boolean alreadyInit = false;

    private static boolean loaded = false;

    /**
     * This routine must always be executed at least once before calling any routines in the OpenCV
     * library
     */
    public static void alwaysExecuteBeforeCallingLibrary() {
        synchronized (LOCK_ALREADY_INIT) {
            if (!alreadyInit) {
                alreadyInit = true;
                CompletableFuture.runAsync(
                        () -> {
                            synchronized (LOCK_LOADED) {
                                // When run on the command-line with the EXE bootrapping then
                                // nu.pattern.OpenCV.loadShared( seems to stall so, using
                                //  loadLocally instead as per the suggestion in:
                                // https://github.com/openpnp/opencv#api
                                Loader.load(opencv_java.class);
                                Loader.load(opencv_cudaimgproc.class);

                                loaded = true;
                                LOCK_LOADED.notifyAll();
                            }
                        });
            }
        }
    }

    /** Blocks a thread until the initialization has completed. */
    public static void blockUntilLoaded() {
        try {
            synchronized (LOCK_LOADED) {
                while (!loaded) {
                    LOCK_LOADED.wait();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
