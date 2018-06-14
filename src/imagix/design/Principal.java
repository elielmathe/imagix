/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagix.design;

import imagix.Processing;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import imagix.FloodFillFacade;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import javax.swing.JColorChooser;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

/**
 *
 * @author monordi
 */
public class Principal extends javax.swing.JFrame {
    private int tailleXimageEnCours,tailleYimageEnCours;
    private Image imageChargee;
    private Mat premierMat = new Mat();
    private Mat matEnCours;
    private Mat[] lesMat = new Mat[10];
    private int indexMatEnCours = 0;
    private String lienDossierEnreg;
    private String nomImgEnreg;
    private String extensionEnreg;
    private boolean automaticPreview;
    private Processing traitement = new Processing();
    private static String[] arg;
    
    //  FOR FLOOD FILLING
        private static final String colorString = "Color";
	private static final String grayscaleString = "Grayscale";
	private static final String onMaskString = "On";
	private static final String offMaskString = "Off";
	private static final String nullRangeString = "Null";
	private static final String fixedRangeString = "Fixed (seed)";
	private static final String floatingRangeString = "Floating (relative)";
	private static final String fourConnectivityString = "4-Connectivity";
	private static final String eightConnectivityString = "8-Connectivity";
        
        private String colorMode = colorString;
	private String maskFlag = onMaskString;
	private String rangeType = fixedRangeString;
	private String connectivityMode = fourConnectivityString;
	private FloodFillFacade floodFillFacade = new FloodFillFacade();
	private int lower = 20;
	private int upper = 20;
        
        //private JLabel imageView;
	//private JLabel maskView;
        
        private Mat originalImage;
	private Mat imageToFlood;
	private Mat grayImage = new Mat();
	private Mat mask = new Mat();
        
        
        private int advancedFlood = 1;
        //  END FLOOD FILLING
    
        private Color laCouleur = new Color(255,255,255);
        
   // private ImageIcon iconeImg = new ImageIcon();
    
    /**
     * Creates new form Principal
     */
    
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    public Principal() {
        initComponents();
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        imagePropre.setAlignmentX(Component.CENTER_ALIGNMENT);
        imagePropre.setAlignmentY(Component.CENTER_ALIGNMENT);
        zoneImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        zoneImage.setAlignmentY(Component.CENTER_ALIGNMENT);
        //chargerImg("/home/monordi/NetBeansProjects/imagix/src/img/5.jpg");
        ajouterMorphologyCbx();
        ajouterMorphologyShapeCbx();
        //slideZoom.setMinimum(20);
        //slideZoom.setMaximum(200);
        //slideZoom.setValue(100);
       
        chargerValeursFlood();
         zoomCbx.removeAllItems();        
        zoomCbx.addItem("100");
        zoomCbx.addItem("20");
        zoomCbx.addItem("50");
        zoomCbx.addItem("70");
        zoomCbx.addItem("160");
        zoomCbx.addItem("200");
        zoomCbx.addItem("300");
        zoomCbx.addItem("500");
        
        java.net.URL url = ClassLoader.getSystemResource("img/icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.setIconImage(img);
        //modifierZone();
    }
    
    private void initialiserFlood(){
        imageToFlood = new Mat();
        mask.create(new Size(matEnCours.cols()+2, matEnCours.rows()+2), CvType.CV_8UC1);
	mask.setTo(new Scalar(0));
        Imgproc.cvtColor(matEnCours, grayImage,Imgproc.COLOR_BGR2GRAY);
        processOperation();
    }
    
    private void chargerValeursFlood(){
        floodTypeRangeCbx.removeAllItems();
        floodTypeRangeCbx.addItem("Null");
        floodTypeRangeCbx.addItem("Fixed (seed)");
        floodTypeRangeCbx.addItem("Floating (relative)");
        floodConnectivityCbx.removeAllItems();
        floodConnectivityCbx.addItem("4-connectivity");
        floodConnectivityCbx.addItem("8-connectivity");
         panneauAdvancedFlood.setVisible(false);
    }
    
    private void changerValeurs(){
        lower = (int)limiteInferieur.getValue();
        upper = (int)limiteSuperieure.getValue();
        Color couleur =laCouleur;
        if(floodGrayScale.isSelected()){
            colorMode = "Grayscale";
        }else if(floodColored.isSelected()){
            colorMode = "Color";
        }else{
            colorMode = "Color";  
        }
        
        String typeFloodRange = (String)floodTypeRangeCbx.getSelectedItem();
        String connectivite = (String)floodConnectivityCbx.getSelectedItem();
        
        if(floodShowMaskOn.isSelected()){
            maskFlag = onMaskString;
        }else if(floodShowMaskOff.isSelected()){
             maskFlag = offMaskString;
        }
           
    }
    
    private void chargerLower(){
        lower = (int)limiteInferieur.getValue();
        processOperation();
    }
    
    private void chargerUpper(){
        upper = (int)limiteSuperieure.getValue();
        processOperation();
    }
    
    private void processOperation(){
        floodFillFacade.setColored(colorMode.equals(colorString));
	floodFillFacade.setMasked( maskFlag.equals(onMaskString));

	if(rangeType.equals(nullRangeString)){
            floodFillFacade.setRange(FloodFillFacade.NULL_RANGE);
	}else if(rangeType.equals(fixedRangeString)){
            floodFillFacade.setRange(FloodFillFacade.FIXED_RANGE);
	}else if(rangeType.equals(floatingRangeString)){
            floodFillFacade.setRange(FloodFillFacade.FLOATING_RANGE);
	}
	floodFillFacade.setConnectivity(fourConnectivityString.equals(connectivityMode)?4:8);
	floodFillFacade.setLowerDiff(lower);
	floodFillFacade.setUpperDiff(upper);
    }
    
    private void afficherPremierMat(){
        premierMat = matEnCours.clone();
        ImageIcon monIcon = new ImageIcon(imageChargee);
        monIcon = new ImageIcon(monIcon.getImage().getScaledInstance(200, -1, Image.SCALE_DEFAULT));
        firstImageLabel.setIcon(monIcon);
    }
    
    
    
    private void ajouterMatMemoire(){
        if(indexMatEnCours == 8){
            for(int i = 1;i < 9;i++){
                lesMat[i - 1] = lesMat[i];
            }
        }
        
        indexMatEnCours = indexMatEnCours + 1;
        lesMat[indexMatEnCours] = new Mat();
        lesMat[indexMatEnCours] = matEnCours.clone();
        System.out.println("Ajout du mat " + indexMatEnCours);
    }
    
    private int indexMatMax = 0;
    
    private void chargerMatPrecedent(){
        if(indexMatEnCours > 0){
            if(indexMatMax == indexMatEnCours) indexMatMax = indexMatEnCours;
            indexMatEnCours = indexMatEnCours - 1;
            matEnCours = lesMat[indexMatEnCours];
            System.out.println("Nous venons du mat " + ( indexMatEnCours + 1) + " au mat " + indexMatEnCours);
            afficherImg((BufferedImage)traitement.toBufferedImage(matEnCours));
        }
    }
    
    private void chargerMatSuivant(){
        if(indexMatMax != 0 && indexMatEnCours != indexMatMax){
            indexMatEnCours = indexMatEnCours + 1;
            matEnCours = lesMat[indexMatEnCours];
            afficherImg((BufferedImage)traitement.toBufferedImage(matEnCours));
        }
        if(indexMatMax == indexMatEnCours){
            System.out.println("Finit!");
        }
    }
    
    private void ajouterBruit(){
        if(matEnCours == null || TailleKernel == null){
            return;
        }
        if(gaussianRadioBtn.isSelected()){
            imageChargee = traitement.toBufferedImage(traitement.applyGaussianBlur(matEnCours));
            afficherImg((BufferedImage) imageChargee);
        }else if(medianRadioBtn.isSelected()){
            imageChargee = traitement.toBufferedImage(traitement.applyMedianBlur(matEnCours));
            afficherImg((BufferedImage) imageChargee);
        }else if(blurRadioBtn.isSelected()){
            imageChargee = traitement.toBufferedImage(traitement.applyBlur(matEnCours,(int)TailleKernel.getValue()));
            afficherImg((BufferedImage) imageChargee);
        }else{
            imageChargee = traitement.toBufferedImage(traitement.blur(matEnCours,(int)TailleKernel.getValue()));
            afficherImg((BufferedImage) imageChargee);
            afficherImg((BufferedImage) imageChargee);
        }
        
       
    }
    
    private void enregistrerImg(String lien,String nom,String extension){
        System.out.println("On enregistre");
        lienDossierEnreg = lien;
        nomImgEnreg = nom;
        extensionEnreg = extension;
        try {
            File outputfile = new File(lien);
            ImageIO.write((BufferedImage)imageChargee, extension, outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void modifierMorphology(){
        if(morphologyOperationCbx.getSelectedItem() == "Erode"){
            if(morphologyShapeCbx.getSelectedItem() == "Rectangular"){
                matEnCours = traitement.erode(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_RECT);               
            }else if(morphologyShapeCbx.getSelectedItem() == "Cross"){
                matEnCours = traitement.erode(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_CROSS);               
            }else if(morphologyShapeCbx.getSelectedItem() == "Ellipse"){
                 matEnCours = traitement.erode(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_ELLIPSE);               
            }
        }else if(morphologyOperationCbx.getSelectedItem() == "Dilation"){
            if(morphologyShapeCbx.getSelectedItem() == "Rectangular"){
                matEnCours = traitement.dilate(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_RECT);               
            }else if(morphologyShapeCbx.getSelectedItem() == "Cross"){
                matEnCours = traitement.dilate(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_CROSS);               
            }else if(morphologyShapeCbx.getSelectedItem() == "Ellipse"){
                 matEnCours = traitement.dilate(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_ELLIPSE);               
            }
        }else if(morphologyOperationCbx.getSelectedItem() == "Open"){
            if(morphologyShapeCbx.getSelectedItem() == "Rectangular"){
                matEnCours = traitement.open(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_RECT);               
            }else if(morphologyShapeCbx.getSelectedItem() == "Cross"){
                matEnCours = traitement.open(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_CROSS);               
            }else if(morphologyShapeCbx.getSelectedItem() == "Ellipse"){
                matEnCours = traitement.open(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_ELLIPSE);               
            }
        }else if(morphologyOperationCbx.getSelectedItem() == "Close"){
             if(morphologyShapeCbx.getSelectedItem() == "Rectangular"){
                matEnCours = traitement.close(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_RECT);               
            }else if(morphologyShapeCbx.getSelectedItem() == "Cross"){
                matEnCours = traitement.close(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_CROSS);               
            }else if(morphologyShapeCbx.getSelectedItem() == "Ellipse"){
                matEnCours = traitement.close(matEnCours,(int) morphologyKernelSize.getValue(), Imgproc.CV_SHAPE_ELLIPSE);               
            }
        }
        imageChargee = traitement.toBufferedImage(matEnCours);
        afficherImg((BufferedImage) imageChargee);
        
    }
    
    private void ajouterMorphologyCbx(){
        morphologyOperationCbx.removeAllItems();
        morphologyOperationCbx.addItem("Erode");
        morphologyOperationCbx.addItem("Dilation");
        morphologyOperationCbx.addItem("Open");
        morphologyOperationCbx.addItem("Close");
    }
    
    private void ajouterMorphologyShapeCbx(){
        morphologyShapeCbx.removeAllItems();
        morphologyShapeCbx.addItem("Rectangular");
        morphologyShapeCbx.addItem("Ellipse");
        morphologyShapeCbx.addItem("Cross");
        
    }
    
    private static class LoadWebcam extends Thread{
            private VideoCapture capture;
            public static void main(String[] args){
                
            }
            private static Processing traitement;
            private static Image tempImage;
            public void debuter(String args[]){
               Mat imgWebcam = new Mat();		
		capture = new VideoCapture(0);
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH,320);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,240);
		int i = 0;
                
                if(capture.isOpened()){
                    while(true){
			capture.read(imgWebcam);
			if(!(imgWebcam.empty())){
                            tempImage = traitement.toBufferedImage(imgWebcam);
                            imagePropre.setIcon(new ImageIcon(tempImage));
                            
                        }else{
                            System.out.println("Ca c'est pas ca!");
			}
                        
                        System.out.println("Boucle");
                        //i++;
                       /* if(i % 5 == 0){
                            try{
                                java.lang.Thread.sleep((long)2000.0);
                            }catch(InterruptedException ex){
                                ex.printStackTrace();
                            }
                        }*/
                    }
		}else{
			System.out.println("Camera non ouvert!");
		}
            }
            
        }
    
    private void chargerDuWebcam(){
        //traitement.launchWebcam(arg, imagePropre);
        /*LoadWebcam leWebcam = new LoadWebcam();
        leWebcam.start(); 
        leWebcam.debuter(arg*/
        WebCam monWebcam = new WebCam();
       // monWebcam.start();
        
        
    }
    
    private void chargerEnregImg(){
        JFileChooser fc=new JFileChooser();
	//String Photo="";
        FileFilter filter = new FileNameExtensionFilter("JPEG file",new String[]{"jpg","jpeg"});
        FileFilter filter2 = new FileNameExtensionFilter("PNG file",new String[]{"png"});
        fc.setFileFilter(filter);
        fc.addChoosableFileFilter(filter);
        fc.addChoosableFileFilter(filter2);
        //fc.setTitle("Save your file ");
	int choice=fc.showSaveDialog(fc);
        
        
        if(choice==JFileChooser.APPROVE_OPTION){
            File choisir=fc.getSelectedFile();
            //System.out.println(choisir.getPath());
            //return choisir.getPath();
            System.out.println("On enregistre dans " + choisir.getName() + "\n\t" + choisir.getPath() + "\n\t" +  choisir.getAbsolutePath());
            if(choisir.getName() != null)
            enregistrerImg(choisir.getPath(), choisir.getName(),"png");
            else enregistrerImg(choisir.getPath(), "image1","png");
            
        } 
    }
    
    private void chargerImgExterne(){
        String leLien = null;
        
	JFileChooser fc=new JFileChooser();
        
        FileFilter filter = new FileNameExtensionFilter("JPEG file",new String[]{"jpg","jpeg"});
        FileFilter filter2 = new FileNameExtensionFilter("PNG file",new String[]{"png"});
        fc.setFileFilter(filter);
        fc.addChoosableFileFilter(filter);
        fc.addChoosableFileFilter(filter2);
        
	String Photo="";
	int choice=fc.showOpenDialog(fc);
        if(choice==JFileChooser.APPROVE_OPTION){
            File choisir=fc.getSelectedFile();
            //System.out.println(choisir.getPath());
            //return choisir.getPath();
            leLien = choisir.getPath();
            chargerImg(leLien);
            ajouterMatMemoire();
        }        
       // return leLien;
    }
    
    private void chargerImg(String lien){
       // Mat newImage = Imgcodecs.imread(lien);
        System.out.println("Charger " + lien);
        matEnCours = Imgcodecs.imread(lien);
        //Mat mat = Imgcodecs.imread(lien);
        Processing leTraitement = new Processing();
        imageChargee = (Image)leTraitement.toBufferedImage(matEnCours);
      //  System.out.println(imageChargee.toString();
        //imagePropre.setText("Hello world");
      //  imagePropre.setIcon(new ImageIcon(imageChargee));
        imageOriginale = imageChargee;
        afficherImg((BufferedImage)imageChargee);
        afficherPremierMat();
        //System.out.println("Ca c'est tres bien");
        //Image img = null;
        /*try{
           // img = ImageIO.read(new File(lien));
            tailleXimageEnCours = imageChargee.getHeight(jTree1);
            tailleYimageEnCours = imageChargee.getWidth(jTree1);
            //imageChargee = img;
       }catch(Exception err){
           System.out.println(err);
       }*/
        initialiserFlood();
    }
    
    private void chargerPremierMat(){
       // System.out.println("Nous chargeons le premier mat");
        matEnCours = premierMat;
        ajouterMatMemoire();
        afficherImg((BufferedImage)traitement.toBufferedImage(matEnCours));
    }
    
    private void changerCouleur(){
        int limiteInf = (int)limiteInferieur.getValue();
        int limiteSup = (int)limiteSuperieure.getValue();
        traitement.fill(matEnCours, matEnCours, WIDTH, WIDTH);
        traitement.setColored(true);
        //traitement.setLowerDiff();
        
        
    }
    
    private void afficherImg(BufferedImage uneImage){
        actuelleTaille = premiereTaille = 600;
        ImageIcon monIcon = new ImageIcon(uneImage);
        monIcon = new ImageIcon(monIcon.getImage().getScaledInstance(actuelleTaille, -1, Image.SCALE_DEFAULT));
        imagePropre.setIcon(monIcon);
        //imagePropre.setIcon(new ImageIcon(uneImage));
    }
    
    private void afficherTaille(int largeur,int longueur){
        affichageTailleImage.setText(largeur + " X " + longueur);
    }
    
    private Image imageOriginale;
    private int locker = 1;
    private int premiereTaille;
    private int actuelleTaille;
    
    private void changerZoom(int pourcentage){
        if(imageChargee == null) return;
        actuelleTaille = premiereTaille * pourcentage / 100;        
        ImageIcon monIcon = new ImageIcon(imageChargee);
        monIcon = new ImageIcon(monIcon.getImage().getScaledInstance(actuelleTaille, -1, Image.SCALE_DEFAULT));
        imagePropre.setIcon(monIcon);
        
    }
    
    private void cacherTousLesOutils(){
        outilNoise.setVisible(false);
        fillingTools.setVisible(false);
        morphologyTools.setVisible(false);
        pyramidTools.setVisible(false);
    }
    
    private void montrerOutilNoise(){
        cacherTousLesOutils();
        outilNoise.setVisible(true);
    }
    
    private void montrerOutilFilling(){
        cacherTousLesOutils();
        fillingTools.setVisible(true);
    }
    
    private void montrerOutilPyramid(){
        cacherTousLesOutils();
        pyramidTools.setVisible(true);
    }
    
    private void montrerOutilMorphology(){
        cacherTousLesOutils();
        morphologyTools.setVisible(true);
    }
    
    
    private Image changerTailleImg(int largeur,int hauteur){
      /* sliderTop.setMaximum(largeur);
       sliderTop.setValue(0);
       sliderLeft.setMaximum(hauteur);
       sliderLeft.setValue(0);*/
       return imageChargee.getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
    }
  //  private TexturePaint dessin;   
    
    
    /*private void modifierZone(BufferedImage bi){
        //TexturePaint dessin;
        zoneImage = new TexturePaint(bi,new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
        Graphics g;
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(dessin);
        g2.fill(new Rectangle(0,0,getWidth(),getHeight()));
        
    }*/
    
    /*public class TextuePanel extends JPanel{
        private TexturePaint paint;
        public TexturePanel(BufferedImage bi){
            super();
            this.paint = new TexturePaint(bi,new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
        }
        
        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(paint);
            g2.fill(new Rectangle(0,0,getWidth(),getHeight()));
        }
    }*/
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem8 = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jToolBar2 = new javax.swing.JToolBar();
        jLabel14 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jToolBar3 = new javax.swing.JToolBar();
        jLabel15 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        zoneCote = new javax.swing.JLayeredPane();
        zoneBlur = new javax.swing.JInternalFrame();
        jLabel17 = new javax.swing.JLabel();
        outilNoise = new javax.swing.JInternalFrame();
        TailleKernel = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        gaussianRadioBtn = new javax.swing.JRadioButton();
        medianRadioBtn = new javax.swing.JRadioButton();
        blurRadioBtn = new javax.swing.JRadioButton();
        jButton5 = new javax.swing.JButton();
        pyramidTools = new javax.swing.JInternalFrame();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        morphologyTools = new javax.swing.JInternalFrame();
        jLabel18 = new javax.swing.JLabel();
        morphologyOperationCbx = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        morphologyKernelSize = new javax.swing.JSpinner();
        jLabel22 = new javax.swing.JLabel();
        morphologyShapeCbx = new javax.swing.JComboBox();
        jButton7 = new javax.swing.JButton();
        fillingTools = new javax.swing.JInternalFrame();
        panneauStandardFlood = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        limiteInferieur = new javax.swing.JSpinner();
        jLabel24 = new javax.swing.JLabel();
        limiteSuperieure = new javax.swing.JSpinner();
        jLabel25 = new javax.swing.JLabel();
        couleurChoisie = new javax.swing.JButton();
        chosenColor = new javax.swing.JLabel();
        floodGrayScale = new javax.swing.JRadioButton();
        floodColored = new javax.swing.JRadioButton();
        panneauAdvancedFlood = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        floodTypeRangeCbx = new javax.swing.JComboBox();
        jLabel29 = new javax.swing.JLabel();
        floodConnectivityCbx = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        floodShowMaskOn = new javax.swing.JRadioButton();
        floodShowMaskOff = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jButton12 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        OpenCVTools = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        zoneImage = new javax.swing.JLayeredPane();
        firstImageLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        imagePropre = new javax.swing.JLabel();
        imageMask1 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jToolBar4 = new javax.swing.JToolBar();
        pourcentageZoom = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        zoomCbx = new javax.swing.JComboBox();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel33 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        affichageTailleImage = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu8 = new javax.swing.JMenu();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        noiseToolsMenu = new javax.swing.JCheckBoxMenuItem();
        morphologyToolsMenu = new javax.swing.JCheckBoxMenuItem();
        pyramidToolsMenu = new javax.swing.JCheckBoxMenuItem();
        fillingToolsMenu = new javax.swing.JCheckBoxMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();

        jMenuItem8.setText("jMenuItem8");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("imagIX");

        jToolBar1.setRollover(true);

        jLabel6.setText("    ");
        jToolBar1.add(jLabel6);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/new.png"))); // NOI18N
        jLabel3.setText("New");
        jLabel3.setToolTipText("");
        jLabel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel3.setIconTextGap(6);
        jToolBar1.add(jLabel3);

        jLabel9.setText("    ");
        jToolBar1.add(jLabel9);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/open.png"))); // NOI18N
        jLabel1.setText("Open");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });
        jToolBar1.add(jLabel1);

        jLabel10.setText("    ");
        jToolBar1.add(jLabel10);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/camera.png"))); // NOI18N
        jLabel4.setText("Camera");
        jLabel4.setToolTipText("");
        jLabel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ouvrirWebCam(evt);
            }
        });
        jToolBar1.add(jLabel4);

        jToolBar2.setRollover(true);

        jLabel14.setText("    ");
        jToolBar2.add(jLabel14);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/save.png"))); // NOI18N
        jLabel2.setText("Save");
        jLabel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });
        jToolBar2.add(jLabel2);

        jLabel11.setText("    ");
        jToolBar2.add(jLabel11);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/print.png"))); // NOI18N
        jLabel5.setText("Print");
        jLabel5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });
        jToolBar2.add(jLabel5);

        jToolBar3.setRollover(true);

        jLabel15.setText("    ");
        jToolBar3.add(jLabel15);

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/undo.png"))); // NOI18N
        jLabel7.setText("Undo");
        jLabel7.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });
        jToolBar3.add(jLabel7);

        jLabel12.setText("    ");
        jToolBar3.add(jLabel12);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/redo.png"))); // NOI18N
        jLabel8.setText("Redo");
        jLabel8.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });
        jToolBar3.add(jLabel8);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setAutoscrolls(true);

        zoneCote.setBackground(new java.awt.Color(146, 239, 185));

        zoneBlur.setBackground(new java.awt.Color(59, 39, 19));
        zoneBlur.setVisible(true);

        jLabel17.setBackground(new java.awt.Color(90, 60, 31));
        jLabel17.setText("Le projet de vingt");

        javax.swing.GroupLayout zoneBlurLayout = new javax.swing.GroupLayout(zoneBlur.getContentPane());
        zoneBlur.getContentPane().setLayout(zoneBlurLayout);
        zoneBlurLayout.setHorizontalGroup(
            zoneBlurLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zoneBlurLayout.createSequentialGroup()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        zoneBlurLayout.setVerticalGroup(
            zoneBlurLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zoneBlurLayout.createSequentialGroup()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 1075, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2039, Short.MAX_VALUE))
        );

        outilNoise.setIconifiable(true);
        outilNoise.setResizable(true);
        outilNoise.setTitle("Noise tools");
        outilNoise.setToolTipText("");
        outilNoise.setVisible(true);

        jLabel16.setText("Size of kernel");

        buttonGroup1.add(gaussianRadioBtn);
        gaussianRadioBtn.setText("Gaussian blur");

        buttonGroup1.add(medianRadioBtn);
        medianRadioBtn.setText("Median blur");

        buttonGroup1.add(blurRadioBtn);
        blurRadioBtn.setText("Blur");
        blurRadioBtn.setToolTipText("");

        jButton5.setText("Apply");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout outilNoiseLayout = new javax.swing.GroupLayout(outilNoise.getContentPane());
        outilNoise.getContentPane().setLayout(outilNoiseLayout);
        outilNoiseLayout.setHorizontalGroup(
            outilNoiseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outilNoiseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(outilNoiseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(outilNoiseLayout.createSequentialGroup()
                        .addGroup(outilNoiseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(outilNoiseLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(18, 18, 18)
                                .addComponent(TailleKernel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(gaussianRadioBtn)
                            .addComponent(medianRadioBtn)
                            .addComponent(blurRadioBtn))
                        .addGap(0, 92, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outilNoiseLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton5)))
                .addContainerGap())
        );
        outilNoiseLayout.setVerticalGroup(
            outilNoiseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outilNoiseLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(outilNoiseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TailleKernel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gaussianRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(medianRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(blurRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5)
                .addContainerGap(96, Short.MAX_VALUE))
        );

        pyramidTools.setTitle("Pyramid tools");
        pyramidTools.setVisible(true);

        jButton8.setText("Apply pyramid down");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setText("Apply pyramid up");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("Apply Laplacian");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pyramidToolsLayout = new javax.swing.GroupLayout(pyramidTools.getContentPane());
        pyramidTools.getContentPane().setLayout(pyramidToolsLayout);
        pyramidToolsLayout.setHorizontalGroup(
            pyramidToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pyramidToolsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pyramidToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                    .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pyramidToolsLayout.setVerticalGroup(
            pyramidToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pyramidToolsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10)
                .addContainerGap(90, Short.MAX_VALUE))
        );

        morphologyTools.setIconifiable(true);
        morphologyTools.setMaximizable(true);
        morphologyTools.setResizable(true);
        morphologyTools.setTitle("Morphology tools");
        morphologyTools.setVisible(true);

        jLabel18.setText("Operation");

        morphologyOperationCbx.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel21.setText("Kernel size");

        jLabel22.setText("Shape");

        morphologyShapeCbx.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton7.setText("Apply");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout morphologyToolsLayout = new javax.swing.GroupLayout(morphologyTools.getContentPane());
        morphologyTools.getContentPane().setLayout(morphologyToolsLayout);
        morphologyToolsLayout.setHorizontalGroup(
            morphologyToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(morphologyToolsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(morphologyToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(morphologyToolsLayout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                        .addComponent(morphologyOperationCbx, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(morphologyToolsLayout.createSequentialGroup()
                        .addGroup(morphologyToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21)
                            .addComponent(jLabel22))
                        .addGap(8, 8, 8)
                        .addGroup(morphologyToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(morphologyKernelSize)
                            .addComponent(morphologyShapeCbx, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, morphologyToolsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton7)))
                .addContainerGap())
        );
        morphologyToolsLayout.setVerticalGroup(
            morphologyToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(morphologyToolsLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(morphologyToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(morphologyOperationCbx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(morphologyToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(morphologyKernelSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(morphologyToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(morphologyShapeCbx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton7)
                .addContainerGap(84, Short.MAX_VALUE))
        );

        fillingTools.setTitle("Flood filling tools");
        fillingTools.setToolTipText("");
        fillingTools.setVisible(true);

        jLabel23.setText("Lower threshold");

        jLabel24.setText("Upper threshold");

        jLabel25.setText("Choose a color");

        couleurChoisie.setText("...");
        couleurChoisie.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                couleurChoisieActionPerformed(evt);
            }
        });

        chosenColor.setText("Color");

        buttonGroup3.add(floodGrayScale);
        floodGrayScale.setText("Grayscale");
        floodGrayScale.setToolTipText("");

        buttonGroup3.add(floodColored);
        floodColored.setText("Colored");
        floodColored.setToolTipText("");

        javax.swing.GroupLayout panneauStandardFloodLayout = new javax.swing.GroupLayout(panneauStandardFlood);
        panneauStandardFlood.setLayout(panneauStandardFloodLayout);
        panneauStandardFloodLayout.setHorizontalGroup(
            panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panneauStandardFloodLayout.createSequentialGroup()
                .addGroup(panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panneauStandardFloodLayout.createSequentialGroup()
                        .addComponent(floodGrayScale)
                        .addGap(51, 51, 51)
                        .addComponent(floodColored))
                    .addGroup(panneauStandardFloodLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addComponent(jLabel24)
                            .addComponent(jLabel23))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addGroup(panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panneauStandardFloodLayout.createSequentialGroup()
                                .addComponent(couleurChoisie)
                                .addGap(3, 3, 3)
                                .addComponent(chosenColor))
                            .addComponent(limiteInferieur, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(limiteSuperieure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        panneauStandardFloodLayout.setVerticalGroup(
            panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panneauStandardFloodLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(limiteInferieur, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(limiteSuperieure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel25)
                    .addGroup(panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(couleurChoisie)
                        .addComponent(chosenColor, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(panneauStandardFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(floodGrayScale)
                    .addComponent(floodColored))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel27.setText("Type range");

        floodTypeRangeCbx.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel29.setText("Connectivity");

        floodConnectivityCbx.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel28.setText("Show mask");

        buttonGroup4.add(floodShowMaskOn);
        floodShowMaskOn.setText("On");
        floodShowMaskOn.setToolTipText("");
        floodShowMaskOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                floodShowMaskOnActionPerformed(evt);
            }
        });

        buttonGroup4.add(floodShowMaskOff);
        floodShowMaskOff.setText("Off");
        floodShowMaskOff.setToolTipText("");
        floodShowMaskOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                floodShowMaskOffActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panneauAdvancedFloodLayout = new javax.swing.GroupLayout(panneauAdvancedFlood);
        panneauAdvancedFlood.setLayout(panneauAdvancedFloodLayout);
        panneauAdvancedFloodLayout.setHorizontalGroup(
            panneauAdvancedFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panneauAdvancedFloodLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panneauAdvancedFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(jLabel29)
                    .addComponent(jLabel28))
                .addGap(22, 22, 22)
                .addGroup(panneauAdvancedFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panneauAdvancedFloodLayout.createSequentialGroup()
                        .addComponent(floodShowMaskOn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(floodShowMaskOff))
                    .addComponent(floodConnectivityCbx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(floodTypeRangeCbx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panneauAdvancedFloodLayout.setVerticalGroup(
            panneauAdvancedFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panneauAdvancedFloodLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(panneauAdvancedFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(floodTypeRangeCbx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panneauAdvancedFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(floodConnectivityCbx, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panneauAdvancedFloodLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(floodShowMaskOn)
                    .addComponent(floodShowMaskOff))
                .addGap(0, 11, Short.MAX_VALUE))
        );

        jButton12.setText("Advanced");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton15.setText("Apply");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                .addComponent(jButton15)
                .addGap(49, 49, 49))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton12)
                .addComponent(jButton15))
        );

        javax.swing.GroupLayout fillingToolsLayout = new javax.swing.GroupLayout(fillingTools.getContentPane());
        fillingTools.getContentPane().setLayout(fillingToolsLayout);
        fillingToolsLayout.setHorizontalGroup(
            fillingToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillingToolsLayout.createSequentialGroup()
                .addGroup(fillingToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panneauStandardFlood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 30, Short.MAX_VALUE))
            .addComponent(panneauAdvancedFlood, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        fillingToolsLayout.setVerticalGroup(
            fillingToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillingToolsLayout.createSequentialGroup()
                .addComponent(panneauStandardFlood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panneauAdvancedFlood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout zoneCoteLayout = new javax.swing.GroupLayout(zoneCote);
        zoneCote.setLayout(zoneCoteLayout);
        zoneCoteLayout.setHorizontalGroup(
            zoneCoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zoneCoteLayout.createSequentialGroup()
                .addGroup(zoneCoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zoneBlur)
                    .addGroup(zoneCoteLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(zoneCoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(outilNoise, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pyramidTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(morphologyTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(zoneCoteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fillingTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        zoneCoteLayout.setVerticalGroup(
            zoneCoteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zoneCoteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(outilNoise, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pyramidTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(morphologyTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(fillingTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6997, 6997, 6997)
                .addComponent(zoneBlur, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(2995, Short.MAX_VALUE))
        );
        zoneCote.setLayer(zoneBlur, javax.swing.JLayeredPane.DEFAULT_LAYER);
        zoneCote.setLayer(outilNoise, javax.swing.JLayeredPane.DEFAULT_LAYER);
        zoneCote.setLayer(pyramidTools, javax.swing.JLayeredPane.DEFAULT_LAYER);
        zoneCote.setLayer(morphologyTools, javax.swing.JLayeredPane.DEFAULT_LAYER);
        zoneCote.setLayer(fillingTools, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel19.setText("Image tools");

        jLabel20.setText("Choose your stuff");

        jButton1.setText("Noise");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Free Drawing");

        jButton3.setText("Black and white");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Change background");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton13.setText("Black sketch");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setText("White sketch");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jButton11.setText("Make texture");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout OpenCVToolsLayout = new javax.swing.GroupLayout(OpenCVTools);
        OpenCVTools.setLayout(OpenCVToolsLayout);
        OpenCVToolsLayout.setHorizontalGroup(
            OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OpenCVToolsLayout.createSequentialGroup()
                .addComponent(jLabel19)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(OpenCVToolsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addComponent(jButton14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(OpenCVToolsLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(OpenCVToolsLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(OpenCVToolsLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        OpenCVToolsLayout.setVerticalGroup(
            OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OpenCVToolsLayout.createSequentialGroup()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20)
                .addGap(79, 79, 79)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(90, 90, 90)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58))
            .addGroup(OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(OpenCVToolsLayout.createSequentialGroup()
                    .addGap(62, 62, 62)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(423, Short.MAX_VALUE)))
            .addGroup(OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(OpenCVToolsLayout.createSequentialGroup()
                    .addGap(188, 188, 188)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(297, Short.MAX_VALUE)))
            .addGroup(OpenCVToolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, OpenCVToolsLayout.createSequentialGroup()
                    .addContainerGap(399, Short.MAX_VALUE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(126, 126, 126)))
        );

        zoneImage.setBackground(new java.awt.Color(89, 217, 54));

        firstImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        firstImageLabel.setText("First image");
        firstImageLabel.setToolTipText("");
        firstImageLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                firstImageLabelMouseDragged(evt);
            }
        });
        firstImageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                firstImageLabelMouseClicked(evt);
            }
        });

        imagePropre.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imagePropre.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Cadre.png"))); // NOI18N
        imagePropre.setToolTipText("");
        imagePropre.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clickSurImage(evt);
            }
        });
        jScrollPane1.setViewportView(imagePropre);

        imageMask1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageMask1.setText("Mask");
        imageMask1.setToolTipText("");

        javax.swing.GroupLayout zoneImageLayout = new javax.swing.GroupLayout(zoneImage);
        zoneImage.setLayout(zoneImageLayout);
        zoneImageLayout.setHorizontalGroup(
            zoneImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zoneImageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 821, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, zoneImageLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(firstImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(93, 93, 93))
            .addGroup(zoneImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(zoneImageLayout.createSequentialGroup()
                    .addGap(22, 22, 22)
                    .addComponent(imageMask1, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(470, Short.MAX_VALUE)))
        );
        zoneImageLayout.setVerticalGroup(
            zoneImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(zoneImageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 396, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(firstImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(zoneImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, zoneImageLayout.createSequentialGroup()
                    .addContainerGap(430, Short.MAX_VALUE)
                    .addComponent(imageMask1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(2, 2, 2)))
        );
        zoneImage.setLayer(firstImageLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        zoneImage.setLayer(jScrollPane1, javax.swing.JLayeredPane.DEFAULT_LAYER);
        zoneImage.setLayer(imageMask1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jButton6.setText("jButton6");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jToolBar4.setRollover(true);

        pourcentageZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/zoom.png"))); // NOI18N
        jToolBar4.add(pourcentageZoom);

        jLabel26.setText("    ");
        jToolBar4.add(jLabel26);

        zoomCbx.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        zoomCbx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomCbxActionPerformed(evt);
            }
        });
        jToolBar4.add(zoomCbx);

        jLabel30.setText(" %");
        jToolBar4.add(jLabel30);

        jLabel31.setText("    ");
        jToolBar4.add(jLabel31);

        jCheckBox1.setText("Automatic preview");
        jCheckBox1.setFocusable(false);
        jCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCheckBox1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar4.add(jCheckBox1);

        jLabel33.setText("                                                                      ");
        jToolBar4.add(jLabel33);

        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icon1.png"))); // NOI18N
        jLabel32.setToolTipText("");
        jToolBar4.add(jLabel32);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(OpenCVTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(zoneImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(zoneCote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(714, 714, 714)
                        .addComponent(jButton6)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(105, 105, 105))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(5145, 5145, 5145)
                    .addComponent(jButton6))
                .addComponent(OpenCVTools, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(zoneImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(zoneCote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2.setBackground(new java.awt.Color(58, 79, 223));

        jLabel13.setForeground(new java.awt.Color(254, 254, 254));
        jLabel13.setText("Nouvelle taille de l'image");

        affichageTailleImage.setFont(new java.awt.Font("Ubuntu", 0, 18)); // NOI18N
        affichageTailleImage.setForeground(new java.awt.Color(254, 254, 254));
        affichageTailleImage.setText("120 X 400 px");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(affichageTailleImage)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(407, 407, 407)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(affichageTailleImage))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenuBar1.setBackground(new java.awt.Color(0, 0, 0));

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/new.png"))); // NOI18N
        jMenuItem1.setText("Open");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/camera.png"))); // NOI18N
        jMenuItem7.setText("New from webcam");
        jMenu1.add(jMenuItem7);

        jMenu3.setText("Open recent");
        jMenu1.add(jMenu3);

        jMenuItem11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/save.png"))); // NOI18N
        jMenuItem11.setText("Save");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem11);

        jMenuItem3.setText("Exit");
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenu8.setText("Background");

        jMenuItem12.setText("Arreter");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem12);

        jMenu2.add(jMenu8);

        jMenuBar1.add(jMenu2);

        jMenu6.setText("View");

        noiseToolsMenu.setSelected(true);
        noiseToolsMenu.setText("Noise tools");
        noiseToolsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noiseToolsMenuActionPerformed(evt);
            }
        });
        jMenu6.add(noiseToolsMenu);

        morphologyToolsMenu.setSelected(true);
        morphologyToolsMenu.setText("Morphology tools");
        morphologyToolsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                morphologyToolsMenuActionPerformed(evt);
            }
        });
        jMenu6.add(morphologyToolsMenu);

        pyramidToolsMenu.setSelected(true);
        pyramidToolsMenu.setText("Pyramid tools");
        pyramidToolsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pyramidToolsMenuActionPerformed(evt);
            }
        });
        jMenu6.add(pyramidToolsMenu);

        fillingToolsMenu.setSelected(true);
        fillingToolsMenu.setText("Filling tools");
        fillingToolsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fillingToolsMenuActionPerformed(evt);
            }
        });
        jMenu6.add(fillingToolsMenu);

        jMenuBar1.add(jMenu6);

        jMenu5.setText("Tools");
        jMenuBar1.add(jMenu5);

        jMenu7.setText("Share");

        jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/facebook.png"))); // NOI18N
        jMenuItem9.setText("Facebook");
        jMenu7.add(jMenuItem9);

        jMenuItem10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/googleplus.png"))); // NOI18N
        jMenuItem10.setText("Google plus");
        jMenu7.add(jMenuItem10);

        jMenuBar1.add(jMenu7);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(488, 488, 488))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 2096, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1316, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(2885, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        chargerImgExterne();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void changerValeur(javax.swing.JSlider leJslide,int valeur){
        leJslide.setValue(valeur);
    }
    
    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        // TODO add your handling code here:
        chargerImgExterne();
    }//GEN-LAST:event_jLabel1MouseClicked

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        // TODO add your handling code here:
        if(lienDossierEnreg == null || nomImgEnreg == null || extensionEnreg == null){
            System.out.println("Dans quel dossier voulez vous enregistrer?");
            chargerEnregImg();
           // enregistrerImg();
        }else{
            System.out.println("On enregistre ici");
        }
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jButton6ActionPerformed

    private void noiseToolsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noiseToolsMenuActionPerformed
        // TODO add your handling code here:
        //System.out.println(noiseToolsMenu.isSelected());
        if(noiseToolsMenu.isSelected()){
           // System.out.println("C'est selectionne");
            //montrerOutilNoise();
            outilNoise.setVisible(true);
        }else{
            outilNoise.setVisible(false);
            //System.out.println("Ce n'est pas selectionne");
           
        }
    }//GEN-LAST:event_noiseToolsMenuActionPerformed

    private void morphologyToolsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_morphologyToolsMenuActionPerformed
        // TODO add your handling code here:
        if(morphologyToolsMenu.isSelected()){
           // System.out.println("C'est selectionne");
            //montrerOutilMorphology();
            morphologyTools.setVisible(true);
        }else{
            morphologyTools.setVisible(false);
            //System.out.println("Ce n'est pas selectionne");
           
        }
    }//GEN-LAST:event_morphologyToolsMenuActionPerformed

    private void pyramidToolsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pyramidToolsMenuActionPerformed
        // TODO add your handling code here:
        if(pyramidToolsMenu.isSelected()){
           // System.out.println("C'est selectionne");
            //montrerOutilNoise();
            pyramidTools.setVisible(true);
        }else{
            pyramidTools.setVisible(false);
            //System.out.println("Ce n'est pas selectionne");
           
        }
    }//GEN-LAST:event_pyramidToolsMenuActionPerformed

    private void fillingToolsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fillingToolsMenuActionPerformed
        // TODO add your handling code here:
        
        if(fillingToolsMenu.isSelected()){
           // System.out.println("C'est selectionne");
            //montrerOutilNoise();
            fillingTools.setVisible(true);
        }else{
            fillingTools.setVisible(false);
            //System.out.println("Ce n'est pas selectionne");
           
        }
    }//GEN-LAST:event_fillingToolsMenuActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        ajouterBruit();
        ajouterMatMemoire();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void ouvrirWebCam(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ouvrirWebCam
        // TODO add your handling code here:
        chargerDuWebcam();
    }//GEN-LAST:event_ouvrirWebCam

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        modifierMorphology();
        ajouterMatMemoire();
    }//GEN-LAST:event_jButton7ActionPerformed

    private boolean activiteClick = false;
    
    private void clickSurImage(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clickSurImage
        // TODO add your handling code here:
        if(activiteClick == true){
        if(colorString.equals(colorMode)){
            floodFillFacade.fill(matEnCours, mask, evt.getX(), evt.getY(),laCouleur);	
	}else{
            floodFillFacade.fill(grayImage, mask, evt.getX(), evt.getY(),laCouleur);
	}
        imageChargee = traitement.toBufferedImage(matEnCours);
        afficherImg((BufferedImage)imageChargee);
        ajouterMatMemoire();
        chargerMask(mask);
        }
    }//GEN-LAST:event_clickSurImage

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // TODO add your handling code here:
        changerValeurs();
        ajouterMatMemoire();
        activiteClick = true;
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
        if(advancedFlood == 1){
            panneauAdvancedFlood.setVisible(true);
            ((javax.swing.JButton)evt.getSource()).setText("Standard");
            panneauAdvancedFlood.setPreferredSize(new Dimension(100,250));
            advancedFlood = 2;
        }else{
            panneauAdvancedFlood.setVisible(false);
            ((javax.swing.JButton)evt.getSource()).setText("Advanced");
             
            advancedFlood = 1;
        }
    }//GEN-LAST:event_jButton12ActionPerformed

    private void floodShowMaskOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_floodShowMaskOnActionPerformed
        // TODO add your handling code here:
        cacherMask();
        chargerMask(mask);
    }//GEN-LAST:event_floodShowMaskOnActionPerformed

    private void floodShowMaskOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_floodShowMaskOffActionPerformed
        // TODO add your handling code here:
        montrerMask();
    }//GEN-LAST:event_floodShowMaskOffActionPerformed

    private void couleurChoisieActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_couleurChoisieActionPerformed
        // TODO add your handling code here:
        laCouleur = JColorChooser.showDialog(this, "Choose a fill color", Color.yellow);
    }//GEN-LAST:event_couleurChoisieActionPerformed

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        // TODO add your handling code here:
        chargerMatPrecedent();
        System.out.println("Mat precedentt(" + indexMatEnCours + ") charge");
    }//GEN-LAST:event_jLabel7MouseClicked

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        // TODO add your handling code here:
        chargerMatSuivant();
        System.out.println("Mat suivant(" + indexMatEnCours + ") charge");
    }//GEN-LAST:event_jLabel8MouseClicked

    private void firstImageLabelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_firstImageLabelMouseDragged
        // TODO add your handling code here:
        //chargerPremierMat();
    }//GEN-LAST:event_firstImageLabelMouseDragged

    private void firstImageLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_firstImageLabelMouseClicked
        // TODO add your handling code here:
        int a = javax.swing.JOptionPane.showConfirmDialog(rootPane, "Are you sure to reset?", "Confirmation", WIDTH);
        if(a == 0){
            chargerPremierMat();
        }else{
            
        }
    }//GEN-LAST:event_firstImageLabelMouseClicked

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        matEnCours = traitement.pyramidDown(matEnCours);
        afficherImg((BufferedImage) traitement.toBufferedImage(matEnCours));
        ajouterMatMemoire();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        matEnCours = traitement.pyramidUp(matEnCours);
        afficherImg((BufferedImage) traitement.toBufferedImage(matEnCours));
        ajouterMatMemoire();
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        matEnCours = traitement.laplacian(matEnCours);
        afficherImg((BufferedImage) traitement.toBufferedImage(matEnCours));
        ajouterMatMemoire();
    }//GEN-LAST:event_jButton10ActionPerformed

    private void zoomCbxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomCbxActionPerformed
        // TODO add your handling code here:
        if((String)zoomCbx.getSelectedItem() != null){
            String donnee = (String)zoomCbx.getSelectedItem();
            Integer nbre = new Integer(donnee);
            int leNbre = nbre.intValue();
            changerZoom(leNbre);
        }
    }//GEN-LAST:event_zoomCbxActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
        matEnCours = traitement.erode(matEnCours,10, Imgproc.CV_SHAPE_CROSS);
        imageChargee = traitement.toBufferedImage(matEnCours);
        afficherImg((BufferedImage) imageChargee);
        ajouterMatMemoire();
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO add your handling code here:
        matEnCours = traitement.dilate(matEnCours,10, Imgproc.CV_SHAPE_CROSS);
        imageChargee = traitement.toBufferedImage(matEnCours);
        afficherImg((BufferedImage) imageChargee);
        ajouterMatMemoire();
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        matEnCours = traitement.noirEtBlanc(matEnCours);
        imageChargee = traitement.toBufferedImage(matEnCours);
        afficherImg((BufferedImage) imageChargee);
        ajouterMatMemoire();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
       // ajouterBruit();
        matEnCours = traitement.blur(matEnCours, 12);
        imageChargee = traitement.toBufferedImage(matEnCours);
        afficherImg((BufferedImage) imageChargee);
        ajouterMatMemoire();
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        outilNoise.setVisible(false);noiseToolsMenu.setSelected(false);
        pyramidTools.setVisible(false);morphologyToolsMenu.setSelected(false);
        morphologyTools.setVisible(false);pyramidToolsMenu.setSelected(false);
        fillingTools.setVisible(true);
        activiteClick = true;
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        // TODO add your handling code here:
        activiteClick = false;
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        // TODO add your handling code here:
        if(lienDossierEnreg == null || nomImgEnreg == null || extensionEnreg == null){
            System.out.println("Dans quel dossier voulez vous enregistrer?");
            chargerEnregImg();
           // enregistrerImg();
        }else{
            System.out.println("On enregistre ici");
        }
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        // TODO add your handling code here:
        imprimer();
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton11ActionPerformed

    private void imprimer(){
       /* PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new Printable(){
            public int print(Graphics graphics,PageFormat pageFormat, int pageIndex) throws PrinterException{
                if(pageIndex != 0){
                    return NO_SUCH_PAGE;
                }
                graphics.drawImage(imageChargee, imageChargee.getWidth(), imageChargee.getHeight(), zoneBlur)
               // graphics.drawImage(imageChargee, 0, 0, imageChargee.getWidth(),imageChargee.getHeight(),null);
                return PAGE_EXISTS;
            }
        });
        try{
            printJob.print();
        }catch(PrinterException e1){
            e1.printStackTrace();
        }*/
    }
    
    
    private void chargerMask(Mat element){
        ImageIcon uneImage = new ImageIcon((BufferedImage) traitement.toBufferedImage(mask));
        imageMask1.setIcon(uneImage);
        
    }
    
    private void cacherMask(){
        imageMask1.setVisible(true);
    }
    
    private void montrerMask(){
        imageMask1.setVisible(false);
    }
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
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        arg = args;
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Principal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OpenCVTools;
    private javax.swing.JSpinner TailleKernel;
    private javax.swing.JLabel affichageTailleImage;
    private javax.swing.JRadioButton blurRadioBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.JLabel chosenColor;
    private javax.swing.JButton couleurChoisie;
    private javax.swing.JInternalFrame fillingTools;
    private javax.swing.JCheckBoxMenuItem fillingToolsMenu;
    private javax.swing.JLabel firstImageLabel;
    private javax.swing.JRadioButton floodColored;
    private javax.swing.JComboBox floodConnectivityCbx;
    private javax.swing.JRadioButton floodGrayScale;
    private javax.swing.JRadioButton floodShowMaskOff;
    private javax.swing.JRadioButton floodShowMaskOn;
    private javax.swing.JComboBox floodTypeRangeCbx;
    private javax.swing.JRadioButton gaussianRadioBtn;
    private javax.swing.JLabel imageMask1;
    public static javax.swing.JLabel imagePropre;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JSpinner limiteInferieur;
    private javax.swing.JSpinner limiteSuperieure;
    private javax.swing.JRadioButton medianRadioBtn;
    private javax.swing.JSpinner morphologyKernelSize;
    private javax.swing.JComboBox morphologyOperationCbx;
    private javax.swing.JComboBox morphologyShapeCbx;
    private javax.swing.JInternalFrame morphologyTools;
    private javax.swing.JCheckBoxMenuItem morphologyToolsMenu;
    private javax.swing.JCheckBoxMenuItem noiseToolsMenu;
    private javax.swing.JInternalFrame outilNoise;
    private javax.swing.JPanel panneauAdvancedFlood;
    private javax.swing.JPanel panneauStandardFlood;
    private javax.swing.JLabel pourcentageZoom;
    private javax.swing.JInternalFrame pyramidTools;
    private javax.swing.JCheckBoxMenuItem pyramidToolsMenu;
    private javax.swing.JInternalFrame zoneBlur;
    private javax.swing.JLayeredPane zoneCote;
    private javax.swing.JLayeredPane zoneImage;
    private javax.swing.JComboBox zoomCbx;
    // End of variables declaration//GEN-END:variables
}
