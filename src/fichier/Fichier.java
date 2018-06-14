/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fichier;

import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 *
 * @author monordi
 */
public class Fichier {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    public double obtenirTaille(){
        return 0.0;
    }
    
    public void enregistrerSous(){
        
    }
    
    public void enregImgSous(Mat matSrc,String destination,String format){
        
    }
    
    public void ouvrir(){
        
    }
    
    public void imprimer(){
        
    }
    
}
