package pontoeletronico.util;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author marcosbispo
 */

public class SwingUtils {

    /**
     * converte imagem do tipo Image para imagem do tipo BufferedImage
     * @param image
     * @return BufferedImage
     */
    public static BufferedImage ImagetoBufferedImage(Image image) {
        
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        
        image = new ImageIcon(image).getImage();
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
        }
        
        if (bimage == null) {
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        
        Graphics g = bimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }
    
    /**
     * This method takes in an image as a ImageIcon (currently supports GIF, JPG, PNG and possibly other formats) and
     * resizes it to have a width no greater than the larguraMaxima parameter in pixels. It converts the image to a standard
     * quality JPG and returns the byte array of that JPG image.
     *
     * @param imageIcon
     *                Icone a ser redimensionado
     * @param larguraMaxima
     *                Largura máxima em pixels ou 0 para manter a largura original
     * @return imagem JPG com o novo tamanho
     * @throws IOException
     *                 Se ocorrer erro na manipulação da imagem
     */
    public static byte[] resizeImageIconAsJPG(ImageIcon imageIcon, int larguraMaxima) throws IOException {
	
        int width = imageIcon.getIconWidth();
	int height = imageIcon.getIconHeight();

	if (larguraMaxima > 0 && width > larguraMaxima) {
	    double ratio = (double) larguraMaxima / imageIcon.getIconWidth();
	    height = (int) (imageIcon.getIconHeight() * ratio);
	    width = larguraMaxima;
	}
	
        BufferedImage bufferedResizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	Graphics2D g2d = bufferedResizedImage.createGraphics();
	g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	g2d.drawImage(imageIcon.getImage(), 0, 0, width, height, null);
	g2d.dispose();

        ByteArrayOutputStream encoderOutputStream = new ByteArrayOutputStream();
	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(encoderOutputStream);
	encoder.encode(bufferedResizedImage);
	byte[] resizedImageByteArray = encoderOutputStream.toByteArray();
        bufferedResizedImage = null;
	return resizedImageByteArray;
        
    }       

}
