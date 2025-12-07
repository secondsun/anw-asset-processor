package dev.secondsun.games.aworld;

import dev.secondsun.games.aworld.resource.Bank;
import dev.secondsun.games.aworld.resource.MemEntry;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.secondsun.games.aworld.Util.*;

public class ResourceReader {
    public record FlattenedResources(int[] resourcesBin, List<MemEntry> memList) {
        @SuppressWarnings("NumericCastToByte")
        public byte[] resourcesByteBin() {
            var bytes = new byte[resourcesBin.length];
            for (int i = 0; i < resourcesBin.length; i++) {
                bytes[i] = (byte) resourcesBin[i];
            }
            return bytes;
        }
    }
    public static final int MEMENTRY_STATE_END_OF_MEMLIST = 0xFF;


    /**
     * For each part (into cinematic, level 1, level 2 etc)
     * this structure describes the location in the mem list
     * of the palette, code, video1, and video 2 data
     */
    public static int[][] MEM_LIST_PARTS = {

//MEMLIST_PART_PALETTE   MEMLIST_PART_CODE   MEMLIST_PART_VIDEO1   MEMLIST_PART_VIDEO2
            {0x14, 0x15, 0x16, 0x00}, // protection screens
            {0x17, 0x18, 0x19, 0x00}, // introduction cinematic
            {0x1A, 0x1B, 0x1C, 0x11},
            {0x1D, 0x1E, 0x1F, 0x11},
            {0x20, 0x21, 0x22, 0x11},
            {0x23, 0x24, 0x25, 0x00}, // battlechar cinematic
            {0x26, 0x27, 0x28, 0x11},
            {0x29, 0x2A, 0x2B, 0x11},
            {0x7D, 0x7E, 0x7F, 0x00},
            {0x7D, 0x7E, 0x7F, 0x00}  // password screen

    };

    public FlattenedResources readAllResources() {
        var mutableMemList = readMemList();
        int[] memory = new int[2048*1024];
        int offset = 0;
        for (MemEntry memEntry : mutableMemList) {
            readAndUpdateEntry(memEntry, offset, memory);

            offset += memEntry.size;
        }
        memory = Arrays.copyOf(memory, offset);
        return new FlattenedResources(memory, mutableMemList);
    }

    /**
    * This will read the entry defined in memEntry to the Buffer and
     * update the memEntry with the position and size in the buffer.
     */
    private void readAndUpdateEntry(MemEntry me, int offset, int[] memory) {
        Bank bank = new Bank(Objects.requireNonNull(this.getClass().getClassLoader().getResource("aworld")).getPath());
        bank.read(me, offset, 0, memory);
        me.bufPtr = offset;
        me.packedSize = me.size;
        me.bankId = 0;
    }

    public List<MemEntry> readMemList() {
        var memList = new ArrayList<MemEntry>();


        try (InputStream memlistStream = getClass().getClassLoader().getResourceAsStream("aworld/memlist.bin")) {
            assert memlistStream != null;

            int numMemList = 0;

            while (true) {
                MemEntry memEntry = new MemEntry();
                memEntry.index = numMemList;
                memEntry.state = read8(memlistStream);
                memEntry.type = read8(memlistStream);
                memEntry.bufPtr = 0;
                //Read 5 unused bytes
                read16(memlistStream);
                read16(memlistStream);
                read8(memlistStream);

                memEntry.bankId = read8(memlistStream);
                memEntry.bankOffset = read32(memlistStream);
                //2 unused bytes
                read16(memlistStream);

                memEntry.packedSize = read16(memlistStream);

                //2 unused bytes
                read16(memlistStream);
                memEntry.size = read16(memlistStream);


                if ((memEntry.state & 0xFF) == MEMENTRY_STATE_END_OF_MEMLIST) {
                    break;
                }
                memList.add(memEntry);

                numMemList++;

            }



        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return memList;

    }


}
