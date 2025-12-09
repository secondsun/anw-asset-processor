package dev.secondsun.games.aworld.resource.audio;

public class Paula {
    public static final int Frequency = 7159090;
    public static final int Carrier = Frequency >> 2;
    
    public static final short[] frequencyTable = {
        (short) (Carrier / 1076),
        (short) (Carrier / 1016),
        (short) (Carrier /  960),
        (short) (Carrier /  906),
        (short) (Carrier /  856),
        (short) (Carrier /  808),
        (short) (Carrier /  762),
        (short) (Carrier /  720),
        (short) (Carrier /  678),
        (short) (Carrier /  640),
        (short) (Carrier /  604),
        (short) (Carrier /  570),
        (short) (Carrier /  538),
        (short) (Carrier /  508),
        (short) (Carrier /  480),
        (short) (Carrier /  453),
        (short) (Carrier /  428),
        (short) (Carrier /  404),
        (short) (Carrier /  381),
        (short) (Carrier /  360),
        (short) (Carrier /  339),
        (short) (Carrier /  320),
        (short) (Carrier /  302),
        (short) (Carrier /  285),
        (short) (Carrier /  269),
        (short) (Carrier /  254),
        (short) (Carrier /  240),
        (short) (Carrier /  226),
        (short) (Carrier /  214),
        (short) (Carrier /  202),
        (short) (Carrier /  190),
        (short) (Carrier /  180),
        (short) (Carrier /  170),
        (short) (Carrier /  160),
        (short) (Carrier /  151),
        (short) (Carrier /  143),
        (short) (Carrier /  135),
        (short) (Carrier /  127),
        (short) (Carrier /  120),
        (short) (Carrier /  113)
    };

}
