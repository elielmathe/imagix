package cours.vision.video;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.management.timer.Timer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Exemple1 {
	private static GUI leCadre = new GUI();
	private static JFrame cadre = leCadre.creerInterface("Webcam app",400, 400);
	private static JButton btnCommencer = new JButton("Commencer");
	private static JButton btnArreter = new JButton("Arreter");
	private static JPanel panneau1 = new JPanel();
	private static JPanel panneau2 = new JPanel();
	private static JLabel conteneurPrinc = new JLabel();
	private static String[] args1;
	
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void main(String[] args) throws Exception{
		args1 = args;
		ajouterOutils(args);
		designer(args);
	}
	
	
	
	private static void designer(String[] args){
		Mat imgWebcam = new Mat();
		Image tempImage;
		VideoCapture capture = new VideoCapture(0);
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH,320);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,240);
		
		if(capture.isOpened()){
			while(true){
				capture.read(imgWebcam);
				//capture.
				if(!(imgWebcam.empty())){
					tempImage = convertMatImg(imgWebcam);
					conteneurPrinc.setIcon(new ImageIcon(tempImage));
					//cadre.pack();
					
				}else{
					System.out.println("Ca c'est pas ca!");
				}
			}
		}else{
			System.out.println("Camera non ouvert!");
		}
		
		
	}
	
	public static Image convertMatImg(Mat theImage){		
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if(theImage.channels() > 1){
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferedSize = theImage.channels() * theImage.cols() * theImage.rows();
		byte[] buffer = new byte[bufferedSize];
		theImage.get(0,0,buffer);

		BufferedImage image1 = new BufferedImage(theImage.cols(),theImage.rows(),type);
		final byte[] targetPixel = ((DataBufferByte) image1.getRaster().getDataBuffer()).getData();
		System.arraycopy(buffer, 0, targetPixel, 0, buffer.length);
		return image1;
	}
	
	private static void ajouterOutils(String[] args){
		btnCommencer.setText("Commencer");
		btnCommencer.setBackground(new Color(0,0,255));
		btnCommencer.setForeground(new Color(255,255,255));
		btnArreter.setBackground(new Color(255,0,0));
		btnArreter.setForeground(new Color(255,255,255));
		panneau1.setPreferredSize(new Dimension(150,350));
		panneau2.setPreferredSize(new Dimension(200,350));
		
		panneau1.add(btnCommencer);
		panneau1.add(btnArreter);
		
		//conteneurPrinc.setText("Ca c'est du texte!");
		//conteneurPrinc.setPreferredSize(new Dimension(120,200));
		conteneurPrinc.setBackground(new Color(128,128,128));
		
		panneau1.add(conteneurPrinc);

		cadre.add(panneau1);
		
		cadre.pack();
		cadre.setVisible(true);
		
		btnCommencer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				lireLesImages(args1);
			}
		});
		
		btnArreter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				stopperLesImages();
			}
		});
	}
	
	private static void lireLesImages(String[] args){
		designer(args);
	}
	
	private static void stopperLesImages(){
		
	}
}
