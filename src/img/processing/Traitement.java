import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class Traitement {
	public static final int NULL_RANGE = 0;
	public static final int FIXED_RANGE = 1;
	public static final int FLOATING_RANGE = 2;
	private boolean colored = true;
	private boolean masked = true;
	private int range = FIXED_RANGE;
	private Random random = new Random();
	private int connectivity = 4;
	private int newMaskVal = 255;
	private int lowerDiff = 20;
	private int upperDiff = 20;
	
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public Mat effectuerGaussian(Mat imageEntree){
		return imageEntree;
	}
	
	public BufferedImage toBufferedImage(Mat matrix){
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if ( matrix.channels() > 1 ) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = matrix.channels()*matrix.cols()*matrix.rows();
		byte [] buffer = new byte[bufferSize];
		matrix.get(0,0,buffer); // get all the pixels
		BufferedImage image = new BufferedImage(matrix.cols(),matrix.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);  
		return image;
	}
	
	public Mat blur(Mat input, int numberOfTimes){
		Mat sourceImage = new Mat();
		Mat destImage = input.clone();
		for(int i=0;i<numberOfTimes;i++){
			sourceImage = destImage.clone();
			Imgproc.blur(sourceImage, destImage, new Size(3.0, 3.0));
		}
		return destImage;
	}
	
	public Mat erode(Mat input, int elementSize, int elementShape){
		Mat outputImage = new Mat();
		Mat element = getKernelFromShape(elementSize, elementShape);
		Imgproc.erode(input,outputImage, element);
		return outputImage;
	}

	

	public Mat dilate(Mat input, int elementSize, int elementShape) {
		Mat outputImage = new Mat();
		Mat element = getKernelFromShape(elementSize, elementShape);
		Imgproc.dilate(input,outputImage, element);
		return outputImage;
	}

	public Mat open(Mat input, int elementSize, int elementShape) {
		Mat outputImage = new Mat();
		Mat element = getKernelFromShape(elementSize, elementShape);
		Imgproc.morphologyEx(input,outputImage, Imgproc.MORPH_OPEN, element);
		return outputImage;
	}

	public Mat close(Mat input, int elementSize, int elementShape) {
		Mat outputImage = new Mat();
		Mat element = getKernelFromShape(elementSize, elementShape);
		Imgproc.morphologyEx(input,outputImage, Imgproc.MORPH_CLOSE, element);
		return outputImage;
	}
	
	private Mat getKernelFromShape(int elementSize, int elementShape) {
		return Imgproc.getStructuringElement(elementShape, new Size(elementSize*2+1, elementSize*2+1), new Point(elementSize, elementSize) );
	}
	

	@Override
	public String toString() {
		return "FloodFillFacade [colored=" + colored + ", masked=" + masked
				+ ", range=" + range + ", random=" + random + ", connectivity="
				+ connectivity + ", newMaskVal=" + newMaskVal + ", lowerDiff="
				+ lowerDiff + ", upperDiff=" + upperDiff + "]";
	}


	public int fill(Mat image, Mat mask, int x, int y) {
		Point seedPoint = new Point(x,y);
		
		int b = random.nextInt(256);
	    int g = random.nextInt(256);
	    int r = random.nextInt(256);
	    Rect rect = new Rect();

	    Scalar newVal = isColored() ? new Scalar(b, g, r) : new Scalar(r*0.299 + g*0.587 + b*0.114);
	    
		Scalar lowerDifference = new Scalar(lowerDiff,lowerDiff,lowerDiff);
		Scalar upperDifference = new Scalar(upperDiff,upperDiff,upperDiff);
		if(range == NULL_RANGE){
			lowerDifference = new Scalar (0,0,0);
			upperDifference = new Scalar (0,0,0);
		}
		int flags = connectivity + (newMaskVal << 8) +
               (
            		   (range == FIXED_RANGE ? Imgproc.FLOODFILL_FIXED_RANGE : 0)
            		   |
            		   0);//Imgproc.FLOODFILL_MASK_ONLY);
	    int area = 0;
	    if(masked){
	    	area = Imgproc.floodFill(image, mask, seedPoint, newVal, rect, lowerDifference,
	    			upperDifference, flags);
	    }
	    else{
	    	area = Imgproc.floodFill(image, new Mat(), seedPoint, newVal, rect, lowerDifference,
	    			upperDifference, flags);
	    }
	    		
		return area;
		
	}
	

	public int getConnectivity() {
		return connectivity;
	}

	public void setConnectivity(int connectivity) {
		this.connectivity = connectivity;
	}

	public boolean isColored() {
		return colored;
	}

	public void setColored(boolean colored) {
		this.colored = colored;
	}

	public boolean isMasked() {
		return masked;
	}

	public void setMasked(boolean masked) {
		this.masked = masked;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}


	public int getLowerDiff() {
		return lowerDiff;
	}

	public void setLowerDiff(int lowerDiff) {
		this.lowerDiff = lowerDiff;
	}

	public int getUpperDiff() {
		return upperDiff;
	}

	public void setUpperDiff(int upperDiff) {
		this.upperDiff = upperDiff;
	} 
	

	public Mat pyramidDown(Mat image){
		Mat image2 = new Mat();
		Imgproc.pyrDown(image, image2);
		return image2;
	}
	
	public Mat pyramidUp(Mat image){
		Mat image2 = new Mat();
		Imgproc.pyrUp(image, image2);
		return image2;
	}
	
	public Mat laplacian(Mat image){
		Mat gp1 = new Mat();
		Imgproc.pyrDown(image, gp1);
		Imgproc.pyrUp(gp1, gp1);
		Core.subtract(image, gp1, gp1);
		return gp1;		
	}
	
}
