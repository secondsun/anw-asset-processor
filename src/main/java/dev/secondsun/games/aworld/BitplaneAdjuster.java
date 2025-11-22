package dev.secondsun.games.aworld;

import dev.secondsun.games.aworld.snes.Tile;

import java.util.List;

/**
 * Some resources in AWorld are stored in a bitplane format and need to be adjusted to the snes bitplane format.
 * TODO : Confirm this
 */
public class BitplaneAdjuster {

    private static final int MAX_PALETTES = 32;
    private static final int NUM_COLORS = 16;
    private static final int SCREEN_W = 320;
    private static final int SCREEN_H = 200;
    private static final int SNES_SCREEN_W = 224;
    private static final int SNES_SCREEN_H = 160;


    /**
     * Bitplanes in the ANW data are stored in 1 bit per 8k page. This returns a bitmap
     * stream.
     *
     * @param src
     * @param memory
     * @return
     */
    public int[] convertFromAmigaBitplaneToIndexedBitmap(int src, int[] memory) {

        int dst = 0;
        int[] videoMemory = new int[200 * 160];
        int h = 200;
        while (h-- > 0) {
            int w = 40;
            while (w-- > 0) {
                int[] p = {
                        memory[(src + 8000 * 3)],
                        memory[(src + 8000 * 2)],
                        memory[(src + 8000)],
                        memory[(src)]
                };
                for (int j = 0; j < 4; ++j) {
                    int acc = 0;
                    for (int i = 0; i < 8; ++i) {
                        acc <<= 1;
                        acc |= ((p[i & 3] & 0x80) != 0) ? 1 : 0;
                        p[i & 3] <<= 1;
                    }
                    videoMemory[dst++] = 0xFF & acc;
                }
                ++src;
            }
        }
        return unpackPixels(videoMemory);
    }

    private int[] unpackPixels(int[] videoMemoryIn) {
        int[] videoMemoryOut = new int[200 * 320];
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 160; x++) {
                int pixel = videoMemoryIn[y * 160 + x];
                int l = (pixel & 0x000000F0) >> 4;
                int r = (pixel & 0x0000000F);
                videoMemoryOut[y * 320 + x*2] = l;
                videoMemoryOut[y * 320 + x*2 + 1] = r;
            }
        }
        return videoMemoryOut;
    }

    /**
     * Converts a 4bpp bitmap to a list of snes tiles. It is assumed the bitmap is 224x160
     * @param videoMemory
     * @return
     */
    public List<Tile> convertFromIndexedBitmapTo4BPPSnesTiles(int[] videoMemory) {
        return null;
    }

    /**
     * Reads a palette from ANW data and turns it into a 32-bit RGB palette
     * ANW palette data is in the on 2bytes (565) for 16 colors
     * @param src index in memory where palette starts
     * @param memory memory to read from
     * @return a 32-bit RGB palette of 16 colors
     */
    public int[] createPalette(int src, int[] memory) {

        return null;
    }

    /**
     * Scales a bitmap from source dimensions to target dimensions.
     * This method performs a simple scaling operation on the input bitmap to resize it
     * to the specified target dimensions.
     *
     * @param srcW    Width of the source bitmap
     * @param srcH    Height of the source bitmap
     * @param dstW    Desired width of the output bitmap
     * @param dtsH    Desired height of the output bitmap
     * @param bitmap  Source bitmap data as an array of integers
     * @return        Scaled bitmap data as an array of integers
     */
    public int[] scale(int srcW, int srcH, int dstW, int dtsH, int[] bitmap) {
        int[] scaled = new int[dstW * dtsH];
        float xRatio = srcW / (float) dstW;
        float yRatio = srcH / (float) dtsH;

        for (int y = 0; y < dtsH; y++) {
            for (int x = 0; x < dstW; x++) {
                int px = (int) (x * xRatio);
                int py = (int) (y * yRatio);
                scaled[y * dstW + x] = bitmap[py * srcW + px];
            }
        }
        return scaled;
    }

    /**
     * Takes in a palette and converts it to a 24bit RGB format.
     * @param pal
     * @return
     */
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

    /**
     * Extracts a palette from the ANW data and converts it to a 24bit RGB format
     *
     * @param palettePointer pointer to the palette data in data
     * @param palNum number of the palette to extract (max 32)
     * @param data data to extract the palette from
     * @return 24bit RGB palette
     */
    public int[] extractPalette(int palettePointer, int palNum, int[] data){
        int[] pal = new int[NUM_COLORS * 3];



            int p = palettePointer + palNum * 32; //colors are coded on 2bytes (565) for 16 colors = 32

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

            return convertPalette(pal);


    }

}
