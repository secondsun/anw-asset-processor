import dev.secondsun.games.aworld.BitplaneAdjuster;
import dev.secondsun.games.aworld.ResourceReader;
import dev.secondsun.games.aworld.resource.Resource;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ScratchTests {

    private static final int MAX_PALETTES = 32;
    private static final int NUM_COLORS = 16;
    private static final int SCREEN_W = 320;
    private static final int SCREEN_H = 200;
    private static final int SNES_SCREEN_W = 224;
    private static final int SNES_SCREEN_H = 160;

    @Test
    public void test() {
        var result = new ResourceReader().readAllResources();
        var data = result.resourcesBin();
        var memEntryList = result.memList();

        var adjuster = new BitplaneAdjuster();
        var bitmapMemEntryList = memEntryList.stream().filter(it -> it.type == Resource.RT_POLY_ANIM).toList();

        var interplayLogo = adjuster.convertFromAmigaBitplaneToIndexedBitmap(memEntryList.get(18).bufPtr, data);
        interplayLogo = adjuster.scale(SCREEN_W, SCREEN_H, SNES_SCREEN_W, SNES_SCREEN_H, interplayLogo);
        var logoPalette = adjuster.extractPalette(memEntryList.get(0x14).bufPtr, 7, data);

        byte[] snesTiles = adjuster.tileize(interplayLogo);
        byte[] snesPalette = adjuster.toSnesPalette(logoPalette);
        byte[] tileMap =  makeTileMap();
        
        File tilesFile = new File("tiles.bin");
        File paletteFile = new File("palette.bin");
        File tileMapFile = new File("tilemap.bin");

        try (FileOutputStream tilesOut = new FileOutputStream(tilesFile);
             FileOutputStream paletteOut = new FileOutputStream(paletteFile);
             FileOutputStream tileMapOut = new FileOutputStream(tileMapFile)) {

            tilesOut.write(snesTiles);
            paletteOut.write(snesPalette);
            tileMapOut.write(tileMap);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        var videoMemory = adjuster.convertFromAmigaBitplaneToIndexedBitmap(memEntryList.get(18).bufPtr, data);
//        videoMemory = adjuster.scale(SCREEN_W, SCREEN_H, SNES_SCREEN_W, SNES_SCREEN_H, videoMemory);
        int palNum2 = 8;
        var pal2 = adjuster.extractPalette(memEntryList.get(0x14).bufPtr, palNum2, data);
        render(interplayLogo, pal2, "image_" + 18  + "_palette_" + palNum2 + ".png");



        var atomicCounter = new AtomicInteger(0);
        bitmapMemEntryList.forEach( memEntry -> {

            var videoMemory = adjuster.convertFromAmigaBitplaneToIndexedBitmap(memEntry.bufPtr, data);
            videoMemory = adjuster.scale(SCREEN_W, SCREEN_H, SNES_SCREEN_W, SNES_SCREEN_H, videoMemory);
            for (int palNum = 0; palNum < MAX_PALETTES; palNum++) {
                var pal = adjuster.extractPalette(memEntryList.get(0x14).bufPtr, palNum, data);
                render(videoMemory, pal, "image_" + memEntry.index  + "_palette_" + palNum + ".png");
            }
            atomicCounter.getAndIncrement();
        });

    }

    private byte[] makeTileMap() {
        var toReturn = new byte[2048];
        int count = 0;
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                if (y*8>=160){
                    toReturn[(2*(x+y*32))] = (byte) (28*20+1);//should be the last tile that is all black
                    toReturn[(2*(x+y*32))+1] = (byte) 1;//should be the last tile that is all black
                } else if (x > 27) {
                    toReturn[2*(x+y*32)] = (byte) (28*20+1);
                    toReturn[(2*(x+y*32))+1] = (byte) 1;//should be the last tile that is all black
                } else {

                    toReturn[(2*(x+y*32))] = (byte) (count&0x00FF);
                    toReturn[(2*(x+y*32))+1] = (byte) ((count &0b100000000)>>8);//should be the last tile that is all black

                    count++;
                }
                
            }
        }
        return toReturn;
    }

    private void render(int[] videoMemory, int[] palette, String fileName) {
        IndexColorModel cm = createIndexColorModel(palette);
        BufferedImage image = getBufferedImage(videoMemory, cm);
        for (int colorIndex = 0; colorIndex < palette.length; colorIndex++) {
            var gr = image.getGraphics();
            gr.setColor(new Color(palette[colorIndex]));
            gr.fillRect(colorIndex % 16 * 16, 144, 16, 16);

        }
        try {
            ImageIO.write(image, "png", new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static IndexColorModel createIndexColorModel(int[] palette) {
        byte[] r = new byte[NUM_COLORS];
        byte[] g = new byte[NUM_COLORS];
        byte[] b = new byte[NUM_COLORS];

        for (int i = 0; i < NUM_COLORS; i++) {
            r[i] = (byte) ((palette[i] >> 16) & 0xFF);
            g[i] = (byte) ((palette[i] >> 8) & 0xFF);
            b[i] = (byte) (palette[i] & 0xFF);
        }
        // 8 bits per pixel in the raster, but only 16 colors used
        return new IndexColorModel(8, NUM_COLORS, r, g, b);
    }


    private static BufferedImage getBufferedImage(int[] videoMemory, IndexColorModel palette) {
        BufferedImage image = new BufferedImage(SNES_SCREEN_W, SNES_SCREEN_H, BufferedImage.TYPE_BYTE_INDEXED, palette);
        WritableRaster raster = image.getRaster();
        for (int y = 0; y < SNES_SCREEN_H; y++) {
            for (int x = 0; x < SNES_SCREEN_W; x++) {
                var packedPixel = videoMemory[x + y * SNES_SCREEN_W];
                raster.setSample(x, y, 0, packedPixel);
            }
        }
        return image;
    }



}
