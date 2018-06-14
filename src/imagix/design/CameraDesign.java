/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagix.design;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.swing.ImageIcon;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 *
 * @author monordi
 */
public class CameraDesign extends javax.swing.JFrame {

    /**
     * Creates new form CameraDesign
     */
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    public CameraDesign() {
        initComponents();
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
					afficheurImage.setIcon(new ImageIcon(tempImage));
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
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        afficheurImage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        afficheurImage.setText("jLabel1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addComponent(afficheurImage, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(80, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addComponent(afficheurImage, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(46, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CameraDesign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CameraDesign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CameraDesign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CameraDesign.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CameraDesign().setVisible(true);
                designer(args);
            }
            
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JLabel afficheurImage;
    // End of variables declaration//GEN-END:variables
}
