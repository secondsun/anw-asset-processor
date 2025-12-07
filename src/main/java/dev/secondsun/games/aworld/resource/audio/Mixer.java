package dev.secondsun.games.aworld.resource.audio;

public class Mixer {


    public short add_clamp(short a, int b) {
        int sum = a + b;
        if (sum > 32767) return 32767;
        if (sum < -32768) return -32768;
        return (short) sum;
    }


    public void mixOneChannel(AudioChannel channel, short[] buffer, int length, byte[] memory) {
        int bufIndex = 0;
        var volume = channel.volume;
        var data_ptr = channel.data_ptr;
        var data_len = channel.data_len;
        var current_pos = channel.data_pos;
        var next_pos = channel.data_pos;
        var data_inc = channel.data_inc;
        var loop_pos = channel.loop_pos;
        var loop_len = channel.loop_len;
        var loop_end = loop_pos + loop_len;

        for (int count = length; count != 0; --count) {
            current_pos = next_pos;
            next_pos += data_inc;
            int p1 = current_pos >> 8;
            int p2 = p1 + 1;

            if (loop_len == 0) {
                if (p2 >= data_len) {
                    channel.active = 0;
                    next_pos = 0;
                    break;
                }
            } else {
                if (p2 >= loop_end) {
                    p2 = loop_pos;
                    next_pos = (loop_pos << 8);
                }
            }

            final int ic = (current_pos & 0xff);
            final int i1 = (0xff - ic);
            final int i2 = ic;
            final int s1 =  memory[data_ptr+ p1];
            final int s2 =  memory[data_ptr+p2];
            final int s3 = ((s1 * i1) + (s2 * i2)) >> 8;

            // Replaced floating point logic with integer math for 16-bit PCM.
            // s3 (approx +/- 127) * volume (max 63) = approx +/- 8000.
            // This fits perfectly into short (+/- 32767) allowing headroom for ~4 channels.
            int sample = s3 * volume;
            buffer[bufIndex] = add_clamp(buffer[bufIndex], sample);
            ++bufIndex;
        }
        channel.data_pos = next_pos;
    }
}
