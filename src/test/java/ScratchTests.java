import dev.secondsun.games.aworld.BitplaneAdjuster;
import dev.secondsun.games.aworld.ResourceReader;
import dev.secondsun.games.aworld.resource.MemEntry;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScratchTests {

    private static final int MAX_PALETTES = 32;
    private static final int NUM_COLORS = 16;
    private static final int SCREEN_W = 320;
    private static final int SCREEN_H = 200;

    @Test
    public void test() {
        var result = new ResourceReader().readAllResources();
        var data = result.resourcesBin();
        var memEntryList = result.memList();

        var adjuster = new BitplaneAdjuster();
        var videoMemory = adjuster.convertFromAmigaBitplaneToIndexedBitmap(memEntryList.get(18).bufPtr, data);
        int[] pal = new int[NUM_COLORS * 3];

        var palettePtr = memEntryList.get(0x14).bufPtr;
        for (int palNum = 0; palNum < MAX_PALETTES; palNum++) {
            int p = palettePtr + palNum * 32; //colors are coded on 2bytes (565) for 16 colors = 32

            // Moved to the heap, legacy code used to allocate the palette
            // on the stack.
            //uint8_t pal[NUM_COLORS * 3]; //3 = BYTES_PER_PIXEL

            for (int i = 0; i < NUM_COLORS; ++i) {
                int c1 = data[p];
                int c2 = data[p + 1];
                p += 2;
                pal[i * 3] = ((c1 & 0x0F) << 2) | ((c1 & 0x0F) >> 2); // r
                pal[i * 3 + 1] = ((c2 & 0xF0) >> 2) | ((c2 & 0xF0) >> 6); // g
                pal[i * 3 + 2] = ((c2 & 0x0F) >> 2) | ((c2 & 0x0F) << 2); // b
            }

            render(videoMemory, convertPalette(pal), "palette" + palNum + ".png");

        }

    }

    private void render(int[] videoMemory, int[] palette, String fileName) {
        BufferedImage image = new BufferedImage(SCREEN_W, SCREEN_H, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < SCREEN_H; y++) {
            for (int x = 0; x < SCREEN_W/2; x++) {
                var packedPixel = videoMemory[x + y * SCREEN_W/2];
                var pixelRight = packedPixel & 0x000000F;
                var pixelLeft = (packedPixel & 0x0000F0) >> 4;
                image.setRGB(x*2, y, palette[pixelLeft]);
                image.setRGB(x*2+1, y, palette[pixelRight]);
            }
        }
        try {
            ImageIO.write(image, "png", new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int[] convertPalette(int[] pal) {
        int[] palette = new int[NUM_COLORS];
        for (int i = 0; i < 16; ++i) {

            int[] c = new int[3];
            for (int j = 0; j < 3; j++) {
                int col = pal[i * 3 + j];
                c[j] = (col << 2) | (col & 3);
            }

            palette[i] = c[0] << 16 | c[1] << 8 | c[2];
        }
        return palette;
    }


}
