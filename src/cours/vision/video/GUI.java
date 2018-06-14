package cours.vision.video;

import javax.swing.JFrame;

public class GUI {
	public JFrame creerInterface(String titre,int hauteur,int largeur){
		JFrame cadre = new JFrame(titre);
		cadre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cadre.setSize(largeur,hauteur);
		return cadre;
	}
}
