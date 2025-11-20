package dev.secondsun.games.aworld;

import dev.secondsun.games.aworld.snes.Tile;

import java.util.List;

/**
 * Some resources in AWorld are stored in a bitplane format and need to be adjusted to the snes bitplane format.
 * TODO : Confirm this
 */
public class BitplaneAdjuster {

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
        return videoMemory;
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
}
