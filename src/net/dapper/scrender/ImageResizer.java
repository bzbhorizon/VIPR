package net.dapper.scrender;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.ImageIcon;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.raster.JimiRasterImage;

public class ImageResizer {

  /**
   * @param sourceImage
   * @param width
   * @param destImage
   * @throws Exception
   */
  public static void resizeImage(String sourceImage, int width ,  String  destImage) throws Exception 
  {
    Image inImage = new ImageIcon(sourceImage).getImage();
    int originalHeight = inImage.getHeight(null);
    int originalWidth = inImage.getWidth(null);
     
     
    float proportion = (float)width / (float)originalWidth;
    int newHeight = (int) (proportion*originalHeight);
     
    Image resizedImg = inImage.getScaledInstance(width, newHeight, Image.SCALE_SMOOTH);
    File outputFile = new File(destImage);
    outputFile.delete();
      
    JimiRasterImage raster = Jimi.createRasterImage(resizedImg.getSource());
    FileOutputStream fos = new FileOutputStream(outputFile);
    Jimi.putImage("image/jpeg", raster, fos);
    fos.flush();
    fos.close();
    
  }
  
  public static void resizeCropImage(String sourceImage, int width ,  int height , String  destImage) throws Exception 
  {
    resizeImage(sourceImage, width, destImage);
    Image inImage = new ImageIcon(destImage).getImage();
    int originalHeight = inImage.getHeight(null);
    int originalWidth = inImage.getWidth(null);
     
    float proportion = (float)width / (float)originalWidth;
    int newHeight = (int) (proportion*originalHeight);
     
    BufferedImage croppedImage=null;
    if (height < newHeight)
    {
      croppedImage = new BufferedImage(width , height , BufferedImage.TYPE_INT_RGB);
      Graphics graphics = croppedImage.getGraphics();
      graphics.drawImage(inImage , 0, 0, width , newHeight,  null);
      
      graphics.dispose();
    }
    else
    {
      croppedImage = new BufferedImage(width , height , BufferedImage.TYPE_INT_RGB);
      Graphics graphics = croppedImage.getGraphics();
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0,0, width, height);
      graphics.drawImage(inImage , 0, 0, width , newHeight,  null);
      graphics.dispose();
    }
    
    
    File outputFile = new File(destImage);
    outputFile.delete();
      
    JimiRasterImage raster = Jimi.createRasterImage(croppedImage.getSource());
    FileOutputStream fos = new FileOutputStream(outputFile);
    Jimi.putImage("image/jpeg", raster, fos);
    fos.flush();
    fos.close();
    
  }
  
}
