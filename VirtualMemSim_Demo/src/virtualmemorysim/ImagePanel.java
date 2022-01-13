/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package virtualmemorysim;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author 15002
 */
public class ImagePanel extends JPanel {

    private BufferedImage image;

    private int offsetX, offsetY;

    public ImagePanel(String addr, int x, int y) {
        super();
        try {
            offsetX = x;
            offsetY = y;
            image = ImageIO.read(getClass().getResource(addr));
        } catch (IOException e) {
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(image, offsetX, offsetY, this);
    }
}
