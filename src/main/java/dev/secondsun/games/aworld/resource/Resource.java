package dev.secondsun.games.aworld.resource;

public class Resource {
    public static final int RT_SOUND = 0;
    public static final int RT_MUSIC = 1;
    public static final int RT_POLY_ANIM = 2; // full screen video buffer, size=0x7D00

    // FCS: 0x7D00=32000...but 320x200 = 64000 ??
    // Since the game is 16 colors, two pixels palette indices can be stored in one byte
    // that's why we can store two pixels palette indice in one byte and we only need 320*200/2 bytes for
    // an entire screen.

    public static final int RT_PALETTE = 3; // palette (1024=vga + 1024=ega), size=2048
    public static final int RT_BYTECODE = 4;
    public static final int RT_POLY_CINEMATIC = 5;
}
