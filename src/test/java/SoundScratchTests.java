import dev.secondsun.games.aworld.ResourceReader;
import dev.secondsun.games.aworld.resource.audio.AudioChannel;
import dev.secondsun.games.aworld.resource.audio.AudioSample;
import dev.secondsun.games.aworld.resource.MemEntry;
import dev.secondsun.games.aworld.resource.audio.Mixer;
import dev.secondsun.games.aworld.resource.audio.Paula;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static dev.secondsun.games.aworld.resource.Resource.RT_MUSIC;
import static dev.secondsun.games.aworld.resource.Resource.RT_SOUND;
import static dev.secondsun.games.aworld.resource.audio.Paula.getFrequency;
import static dev.secondsun.games.aworld.resource.audio.Paula.getVolume;

public class SoundScratchTests {

    public static final int SAMPLE_RATE = 44100;

    @Test
    void scratchMusic() {
        var resources = new ResourceReader().readAllResources();
        var data = resources.resourcesByteBin();
        var memEntryList = resources.memList();

        var sounds = memEntryList.stream().filter(it -> it.type == RT_SOUND && it.size != 0).toList();
        var music = memEntryList.stream().filter(it -> it.type == RT_MUSIC).toList();

    }

    @Test
    void scratchSound() {
        var resources = new ResourceReader().readAllResources();
        var data = resources.resourcesByteBin();
        var memEntryList = resources.memList();

        var sounds = memEntryList.stream().filter(it -> it.type == RT_SOUND && it.size != 0).toList();
        var music = memEntryList.stream().filter(it -> it.type == RT_MUSIC).toList();

        for (MemEntry sound : sounds) {
            System.out.println("Playing sound" + sound.index + " memory " + sound.bufPtr +  " size " + sound.size);
            try {

                var sample = playSound(sound, data, (byte) 0x2F, (byte) 0x20);
                System.out.println("Sample loop len " + sample.loop_len);
                //Mixer::playChannel
                var channel = playChannel(sample);
                var buffer = new short[SAMPLE_RATE ];
                var length = SAMPLE_RATE ;
                new Mixer().mixOneChannel(channel, buffer, length, data);

                //playAudio(buffer);
            } catch (Exception e) {
                System.out.println("Failed to play sound " + sound.index);
                e.printStackTrace();
            }
        }
    }

    public static void playAudio(short[] buffer) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            byte[] byteBuffer = new byte[buffer.length * 4];
            ByteBuffer.wrap(byteBuffer).asShortBuffer().put(buffer);
            line.write(byteBuffer, 0, byteBuffer.length);

            line.drain();
            line.stop();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private AudioChannel playChannel(AudioSample sample) {
        var channel = new AudioChannel();
        channel.active    = 1;
        channel.volume    = sample.volume;
        channel.sample_id = sample.sample_id;
        channel.data_ptr  = sample.data_ptr;
        channel.data_len = (int) sample.data_len;
        channel.data_pos = 0;
        channel.data_inc = (sample.frequency << 8) / SAMPLE_RATE;
        channel.loop_pos =  sample.loop_pos;
        channel.loop_len =  sample.loop_len;
        return channel;
    }

    private AudioSample playSound(MemEntry sound, byte[] data, byte volume, byte pitch) {
        ByteBuffer buf = ByteBuffer.wrap(data, (int) sound.bankOffset, sound.size);
        //buf.order(ByteOrder.BIG_ENDIAN);
        int dataLen = buf.getInt();
        int loopStart = buf.getInt();
        short unused1 = buf.getShort();
        short unused2 = buf.getShort();
        int dataPtr = buf.get() & 0xFF;

        AudioSample sample = new AudioSample();
        sample.sample_id = (short) sound.index;
        sample.frequency = getFrequency(pitch);
        sample.volume = getVolume(volume);
        sample.data_ptr = dataPtr;
        sample.data_len = dataLen;
        sample.loop_pos = 0;
        sample.loop_len = 0;
        sample.unused1   = unused1;
        sample.unused2   = unused2;
        if(dataLen != 0) {
            sample.data_len += dataLen;
            sample.loop_pos += dataLen;
            sample.loop_len += dataLen;
        }
        return sample;
    }


}
