package org.anchoranalysis.plugin.opencv.text;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

// TODO not implemented yet
public class BinaryImgChnlProviderExtractText extends BinaryImgChnlProvider {

	static {
		nu.pattern.OpenCV.loadShared();
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private StackProvider stackProvider;
	// END BEAN PROPERTIES

	private static List<String> layerNames = Arrays.asList(
      	"feature_fusion/Conv_7/Sigmoid",
      	"feature_fusion/concat_3"
	);
	
	@Override
	public BinaryChnl create() throws CreateException {

		Mat input = resized(
			MatConverter.fromStack( stackProvider.create() )
		);
		
		
				
		@SuppressWarnings("unused")
		Net net = Dnn.readNetFromTensorflow("C:\\\\Users\\\\owen\\\\Desktop\\frozen_east_text_detection.pb");
		
		Scalar meanSubtractionConstants = new Scalar(123.68, 116.78, 103.94);
		
		Mat blob = Dnn.blobFromImage(input, 1.0, input.size(), meanSubtractionConstants, true, false );
		
		net.setInput(blob);

		//imshow(blob);
		
		// Calculated seperately due to a bug in the OpenCV java library which returns an exception (know further details)
		//  when they are calculated together. A similar bug looks to be reported here:
		// https://answers.opencv.org/question/214676/android-java-dnnforward-multiple-output-layers-segmentation-fault/
		Mat scores = net.forward("feature_fusion/Conv_7/Sigmoid");
		Mat geometry = net.forward("feature_fusion/concat_3");
		
		Size scoresSize = scores.size();
		float arr[] = new float[320];
		float arr0[] = new float[6400];
		float arr1[] = new float[6400];
		
		for( int y=0; y<scoresSize.width; y++) {
			
			//geometry.get(0, y, arr);
		}
		
		
		
		Mat scores2 = scores.reshape(1, 1);
		Mat geometry2 = geometry.reshape(1, (int) (input.size().height *  input.size().width));
		
		geometry2.get(0, 0, arr0);
		geometry2.get(0, 1, arr1);
		
		return null;
	}
	
	public void imshow(Mat src){
	    BufferedImage bufImage = null;
	    try {
	        MatOfByte matOfByte = new MatOfByte();
	        Imgcodecs.imencode(".jpg", src, matOfByte); 
	        byte[] byteArray = matOfByte.toArray();
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);

	        JFrame frame = new JFrame("Image");
	        frame.getContentPane().setLayout(new FlowLayout());
	        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
	        frame.pack();
	        frame.setVisible(true);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private Mat resized( Mat src ) {
		Mat dst = new Mat();
		Size sz = new Size(320,320);
		Imgproc.resize(src, dst, sz);
		return dst;
	}

	public StackProvider getStackProvider() {
		return stackProvider;
	}

	public void setStackProvider(StackProvider stackProvider) {
		this.stackProvider = stackProvider;
	}

}
