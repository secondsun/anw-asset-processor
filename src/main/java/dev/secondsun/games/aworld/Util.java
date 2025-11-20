package dev.secondsun.games.aworld;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public final class Util {
    private static final Logger LOG = Logger.getAnonymousLogger();

    private Util() {
    }


    public static int read8(InputStream memlistStream) throws IOException {
        return memlistStream.read() & 0x00FF;
    }

    public static int read16(InputStream memlistStream) throws IOException {
        int b2 = memlistStream.read() & 0x00FF;
        int b1 = memlistStream.read() & 0x00FF;
        return (b2 << 8) | b1;
    }

    public static long read32(InputStream memlistStream) throws IOException {
        int b2 = read16(memlistStream) & 0x00FFFF;
        int b1 = read16(memlistStream) & 0x00FFFF;
        return (b2 << 16) | b1;
    }

    public static long read32(int start, int[] memory) {
        int b2 = read16(start, memory) & 0x00FFFF;

        int b1 = read16(start + 2, memory) & 0x00FFFF;

        return (b2 << 16) | b1;
    }

    public static int read16(int start, int[] memory) {
        int b2 = (memory[start]) & 0x00FF;
        int b1 = memory[start + 1] & 0x00FF;
        return (b2 << 8) | b1;
    }

    public static void warning(String message, Object... vars) {
        //LOG.log(Level.WARNING, String.format(message, vars));
    }

    public static void debug(String key, String message, Object... vars) {
        //System.err.println(String.format(message, vars).trim());
    }

    public static void error(String message, Object... vars) {
        //LOG.log(Level.SEVERE, String.format(message, vars));
    }
}
