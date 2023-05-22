package com.cb200.minchess.gui;

import javax.imageio.ImageIO;

import com.cb200.chessutils.piece.Piece;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PieceGraphics {

    private static final String IMAGE_PATH = "MinChessV11/lib/pieceImages/";
    
    public static void drawPiece(Graphics g, int piece, int x, int y, int squareSize) {
        String imageName = getImageName(piece);
        System.out.println("Image name: " + imageName);
        BufferedImage image = loadImage(imageName);
        if (image != null) {
            System.out.println("Image drawn: " + imageName);
            g.drawImage(image, x, y, squareSize, squareSize, null);
        }
    }
    
    private static String getImageName(int piece) {
        return IMAGE_PATH + Piece.LONG_STRING[piece] + ".png";
    }
    
    private static BufferedImage loadImage(String imageName) {
        try (InputStream stream = PieceGraphics.class.getResourceAsStream(imageName)) {
            if (stream != null) {
                return ImageIO.read(stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

