package dev.secondsun.games.aworld.resource;

public class MemEntry {
    public int state;         // 0x0
    public int type;          // 0x1, Resource::ResType
    public int bufPtr;       // 0x2
    public int bankId;       // 0x7
    public long bankOffset;      // 0x8 0xA
    public int packedSize;   // 0xE
    // All ressources are packed (for a gain of 28% according to Chahi)

    public int size; // 0x12


    @Override
    public String toString() {
        return "MemEntry{" +
                "state=" + state +
                ", type=" + type +
                ", bufPtr=" + bufPtr +
                ", bankId=" + bankId +
                ", bankOffset=" + bankOffset +
                ", packedSize=" + packedSize +
                ", size=" + size +
                '}';
    }
}
