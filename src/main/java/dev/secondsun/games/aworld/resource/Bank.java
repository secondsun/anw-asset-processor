package dev.secondsun.games.aworld.resource;

import dev.secondsun.games.aworld.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static dev.secondsun.games.aworld.Util.*;

public class Bank {
    UnpackContext _unpCtx = new UnpackContext();
    String dataDir;
    private int _startBuf;
    private long _iBuf;
    private long _oBuf;

    public Bank(String dataDir) {
        this.dataDir = dataDir;
    }

    //uint8_t *_iBuf, *_oBuf, *_startBuf;
    //loadDestination is the return value
    public boolean read(MemEntry me, int loadDestination, int memListIndex, int[] memory) {

        boolean ret = false;
        String bankName = String.format("bank%02x", me.bankId);

        File f = new File(dataDir, bankName);

        if (me.type == 5) {
            debug("0", "cinematic loaded at %d", loadDestination);
        }
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(f))) {

            stream.skipNBytes(me.bankOffset);
            // Depending if the resource is packed or not we
            // can read directly or unpack it.
            boolean print = false;
            if (me.type == 5) {
                print = true;
                debug("video", "printing type 5");
            }
            if (me.packedSize == me.size) {
                debug("read", "uncompressed me %s", me.toString());
                byte[] m2 = new byte[me.packedSize];
                stream.read(m2, 0, me.packedSize);

                for (int i =0; i < me.packedSize; i++) {

                    memory[loadDestination+i]=m2[i] & 0xFF;
                    if (print) {
                       // debug("video", "m2[%d]:%d", i, m2[i] & 0xFF);
                    }
                }
                ret = true;
            } else {
                debug("read", "compressed me %s", me.toString());
                byte[] m2 = new byte[me.packedSize];
                stream.read(m2, 0, me.packedSize);
                for (int i =0; i < me.packedSize; i++) {
                    memory[loadDestination+i]=m2[i] & 0xFF;
                }
                this._startBuf = loadDestination;
                this._iBuf = loadDestination + me.packedSize - 4;
                ret = unpack(memory);
                if (print) {
                    for (int i =0; i < me.size; i++) {
                        //debug("video", "memory[%d]:%d", loadDestination+i, memory[loadDestination+i] & 0xFF);
                    }
                }
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private boolean unpack(int[] memory) {

        _unpCtx.size = 0;
        _unpCtx.datasize = Util.read32((int) _iBuf, memory);
        _iBuf -= 4;
        this._oBuf = _startBuf + _unpCtx.datasize - 1;
        _unpCtx.crc = Util.read32((int) _iBuf, memory);
        _iBuf -= 4;
        _unpCtx.chk = Util.read32((int) _iBuf, memory);

        _iBuf -= 4;
        _unpCtx.crc ^= _unpCtx.chk;
        do {
            if (!nextChunk(memory)) {
                _unpCtx.size = 1;
                if (!nextChunk(memory)) {
                    decUnk1(3, 0, memory);
                } else {
                    decUnk2(8, memory);
                }
            } else {
                int c = getCode(2, memory) & 0xFFFF;
                if (c == 3) {
                    decUnk1(8, 8, memory);
                } else {
                    if (c < 2) {
                        _unpCtx.size = c + 2;
                        decUnk2(c + 9, memory);
                    } else {
                        _unpCtx.size = getCode(8, memory) & 0xFFFF;
                        decUnk2(12, memory);
                    }
                }
            }
        } while (_unpCtx.datasize > 0);
        return (_unpCtx.crc == 0);
    }

    private void decUnk2(int numChunks, int[] memory) {
        int i = getCode(numChunks, memory);
        int count = _unpCtx.size + 1;

        _unpCtx.datasize -= count;
        while (count-- != 0) {
            assert(_oBuf >= _iBuf && _oBuf >= _startBuf);
            memory[(int) this._oBuf] = memory[(int) (_oBuf + i)];
		--_oBuf;
        }
    }

    private void decUnk1(int numChunks, int addCount, int[] memory) {
        int count = getCode(numChunks, memory) + addCount + 1;

        _unpCtx.datasize -= count;
        while (count-- != 0) {
            assert (_oBuf >= _iBuf && _oBuf >= _startBuf);
            memory[(int) _oBuf] = getCode(8, memory)&0xFF;
            --_oBuf;
        }

    }

    private int getCode(int numChunks, int[] memory) {

        int c = 0;
        while (numChunks-- != 0) {
            c <<= 1;
            if (nextChunk(memory)) {
                c |= 1;
            }
        }
        return c;
    }

    private boolean nextChunk(int[] memory) {
        boolean CF = rcr(false);
        if (_unpCtx.chk == 0) {
            assert (_iBuf >= _startBuf);
            _unpCtx.chk = (Util.read32((int) _iBuf, memory));
            _iBuf -= 4;
            _unpCtx.crc ^= _unpCtx.chk;
            CF = rcr(true);
        }
        return CF;
    }

    private boolean rcr(boolean CF) {
        boolean rCF = (_unpCtx.chk & 1) != 0;
        _unpCtx.chk = ((int)_unpCtx.chk) >>> 1;
        if (CF) _unpCtx.chk |= 0x0000000080000000;
        _unpCtx.chk &= 0x00000000FFFFFFFF;
        return rCF;
    }

}
