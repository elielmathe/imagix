/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagix;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;


public class Processing{
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
        
        private static final String colorString = "Color";
	private static final String grayscaleString = "Grayscale";
	private static final String onMaskString = "On";
	private static final String offMaskString = "Off";
	private static final String nullRangeString = "Null";
	private static final String fixedRangeString = "Fixed (seed)";
	private static final String floatingRangeString = "Floating (relative)";
	private static final String fourConnectivityString = "4-Connectivity";
	private static final String eightConnectivityString = "8-Connectivity";
	private JLabel imageView;
	private JLabel maskView;
	private String windowName;
	private Mat originalImage;
	private Mat image;
	private Mat grayImage = new Mat();
	private Mat mask = new Mat();

	//private final Processing imageProcessor = new Processing();
	
	private String colorMode = colorString;
	private String maskFlag = onMaskString;
	private String rangeType = fixedRangeString;
	private String connectivityMode = fourConnectivityString;
	private FloodFillFacade floodFillFacade = new FloodFillFacade();
	private int lower = 20;
	private int upper = 20;

	
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public Mat effectuerGaussian(Mat imageEntree){
		return imageEntree;
	}
        
        public Mat noirEtBlanc(Mat imageEntree){
            Mat sortie = new Mat();
            Imgproc.cvtColor(imageEntree, sortie,Imgproc.COLOR_BGR2GRAY);
            return sortie;
        }
        
        public void launchWebcam(String[] args,JLabel leLabel){
		//leLabel.setText("Hello world");
                //if(1 == 1) return;
                Mat imgWebcam = new Mat();
		Image tempImage;
		VideoCapture capture = new VideoCapture(0);
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH,320);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,240);
		int i = 0;
		if(capture.isOpened()){
                    while(true){
			capture.read(imgWebcam);
			if(!(imgWebcam.empty())){
                            tempImage = toBufferedImage(imgWebcam);
                            leLabel.setIcon(new ImageIcon(tempImage));
                        }else{
                            System.out.println("Ca c'est pas ca!");
			}
                        i++;
                        if(i == 10) break;
                    }
		}else{
			System.out.println("Camera non ouvert!");
		}
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
        
        private void processFill() {
		setColored(colorMode.equals(colorString));
		floodFillFacade.setMasked( maskFlag.equals(onMaskString));

		if(rangeType.equals(nullRangeString)){
			floodFillFacade.setRange(FloodFillFacade.NULL_RANGE);
		}
		else if(rangeType.equals(fixedRangeString)){
			floodFillFacade.setRange(FloodFillFacade.FIXED_RANGE);
		}
		else if(rangeType.equals(floatingRangeString)){
			floodFillFacade.setRange(FloodFillFacade.FLOATING_RANGE);
		}
		floodFillFacade.setConnectivity(fourConnectivityString.equals(connectivityMode)?4:8);
		floodFillFacade.setLowerDiff(lower);
		floodFillFacade.setUpperDiff(upper);
		
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
                Mat gp2 = new Mat();
		Imgproc.pyrDown(image, gp1);
                gp2 = gp1;
		Imgproc.pyrUp(gp1, gp2);
		Core.subtract(image, gp1, gp2);
		return gp2;		
	}
        
        public Mat applyBlur(Mat inputImg,int size1){
            Mat output;
            output = new Mat(inputImg.rows(), inputImg.cols(), inputImg.type());
            Size size = new Size(size1, size1);
            Imgproc.blur(inputImg, output, size);
            return output;
        }
        
        public Mat applyGaussianBlur(Mat inputImg){
            Mat output;
            output = new Mat(inputImg.rows(), inputImg.cols(), inputImg.type());
            Size size = new Size(3.0, 3.0);
            Imgproc.GaussianBlur(inputImg, output, size, 0);
            return output;
        }
        
        public Mat applyMedianBlur(Mat inputImg){
            Mat output;
            output = new Mat(inputImg.rows(), inputImg.cols(), inputImg.type());
            Size size = new Size(3.0, 3.0);
            Imgproc.medianBlur(inputImg, output,3);
            return output;
        }
	
        public Mat applyBilateralFilter(Mat inputImg){
            Mat output;
            output = new Mat(inputImg.rows(), inputImg.cols(), inputImg.type());
            Size size = new Size(3.0, 3.0);
            Imgproc.bilateralFilter(inputImg, output, 9, 100, 100);
            return output;
        }
}

