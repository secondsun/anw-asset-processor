import dev.secondsun.games.aworld.ResourceReader;
import dev.secondsun.games.aworld.resource.MemEntry;
import dev.secondsun.games.aworld.resource.audio.*;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static dev.secondsun.games.aworld.resource.Resource.RT_MUSIC;
import static dev.secondsun.games.aworld.resource.Resource.RT_SOUND;

public class MusicScratchTests {
    public static final int SAMPLE_RATE = 44100;

    @Test
    void scratchMusic() {
        var resources = new ResourceReader().readAllResources();
        var memory = resources.resourcesByteBin();
        var memEntryList = resources.memList();
        var output = new HashMap<AudioChannel, ByteBuffer>();
        var sounds = memEntryList.stream().filter(it -> it.type == RT_SOUND && it.size != 0).toList();
        var musics = memEntryList.stream().filter(it -> it.type == RT_MUSIC).toList();
        var channels = new ArrayList<AudioChannel>();
        MemEntry music = musics.get(0);
        var musicData = wrapArray(memory, music.bufPtr, music.size);
        var _module = load_module(musicData, music, (byte) 0, (short) 15700);
        load_samples(musicData, music, _module, memEntryList, memory);
        load_seq_table(musicData, music, _module);

        //music#183
        while (_module.seq_index < _module.seq_count) {
            var sequence = _module.seq_table[_module.seq_index];
            //var moduleData = musicData.slice((int) (_module.data_ptr + _module.data_pos + sequence * 1024), (int) (music.size - (_module.data_ptr + _module.data_pos + sequence * 1024)));
            // Below is probably wrong.
            var moduleData = wrapArray(musicData.array(), (int) (_module.data_ptr + _module.data_pos + sequence * 1024), musicData.limit() - (int) (_module.data_ptr + _module.data_pos + sequence * 1024));
            for (byte channel = 0; channel < 4; ++channel) {
                var temp = processPattern(channel, moduleData, _module);
                if (temp != null) {
                    channels.add(temp);
                }
            }
            //MIXER.MIXALLCHANNELS
            var buffer = new short[SAMPLE_RATE * 10];
            var length = SAMPLE_RATE * 10;

            for (var channel : channels) {
                new Mixer().mixOneChannel(channel, buffer, length, memory);
            }

            //SoundScratchTests.playAudio(buffer);

            _module.data_pos += moduleData.position() - musicData.position();
            if (_module.data_pos >= 1024) {
                _module.data_pos = 0;
                var seq_index = _module.seq_index + 1;
                var seq_count = _module.seq_count;
                if (seq_index >= seq_count) {
                    System.out.printf("music is over [music_id: 0x%02x]\n", _module.music_id);
                    break;
                } else {
                    _module.seq_index = (byte) seq_index;
                }
            }


//          playPattern(data, module);
        }

    }

    private AudioChannel processPattern(byte index, ByteBuffer data, MusicModule _module) {

        MusicPattern pattern = new MusicPattern();
        pattern.word1 = data.getShort();
        pattern.word2 = data.getShort();
        if (pattern.word1 == 0x0000) {
            return null;
        } else if (pattern.word1 == 0xfffd) {
            return null;
        } else if (pattern.word1 == 0xfffe) {
            return null;
        } else {
            short period_value = (short) ((pattern.word1 & 0x0fff) >> 0);
            byte sample_index = (byte) ((pattern.word2 & 0xf000) >> 12);
            byte effect_index = (byte) ((pattern.word2 & 0x0f00) >> 8);
            byte effect_value = (byte) ((pattern.word2 & 0x00ff) >> 0);
            if (sample_index != 0) {
                var sample = (_module.samples[sample_index - 1]);
                short volume = sample.volume;
                switch (effect_index) {
                    case 0x0: // no effect
                        break;
                    case 0x5: // volume up
                        volume += effect_value;
                        break;
                    case 0x6: // volume down
                        volume -= effect_value;
                        break;
                    default:
                        System.out.printf("unsupported effect $%x%n\n", effect_index);
                        break;
                }
                sample.frequency = (short) getFrequency(period_value);
                sample.volume = getVolume( volume);

                var channel = new AudioChannel();
                channel.active = 1;
                channel.volume = sample.volume;
                channel.sample_id = sample.sample_id;
                channel.data_ptr = sample.data_ptr;
                channel.data_len = (sample.data_len);
                channel.data_pos = (0);
                channel.data_inc = ((sample.frequency) << 8) / SAMPLE_RATE;
                channel.loop_pos = (sample.loop_pos);
                channel.loop_len = (sample.loop_len);
                return channel;
            }
            return null;
        }
    }

    private void processPattern(byte index, ByteBuffer data) {
    }

    private AudioChannel playChannel(AudioSample sample) {
        return null;
    }

    private void load_seq_table(ByteBuffer data, MemEntry music, MusicModule _module) {
        data.position( 0x40);
        for (int sequence = 0; sequence < _module.seq_table.length; sequence++) {
            _module.seq_table[sequence] = data.get();
        }

    }

    private void load_samples(ByteBuffer data, MemEntry music, MusicModule _module, List<MemEntry> memEntryList, byte[] memory) {
        data.position( 0x02);
        for (AudioSample sample : _module.samples) {
            if (sample == null) continue;

            short sample_id = data.getShort();
            short volume = data.getShort();

            if (sample_id != 0) {
                System.out.println("load sample [sound_id: " + String.format("0x%02x", sample_id) + ", volume: " + volume + "]");

                var resource = memEntryList.get(sample_id);
                if (resource == null) {
                    Logger.getAnonymousLogger().log(java.util.logging.Level.INFO, String.format("resource not found [sound_id: 0x%02x]", sample_id));
                } else if (resource.type != RT_SOUND) {
                    Logger.getAnonymousLogger().log(java.util.logging.Level.INFO, String.format("resource not invalid [sound_id: 0x%02x]", sample_id));

                }
                var sampleData = wrapArray(memory, resource.bufPtr, resource.size);
                int data_len = (sampleData.getShort() & 0xFFFF) * 2;
                int loop_len = (sampleData.getShort() & 0xFFFF) * 2;
                short unused1 = sampleData.getShort();
                short unused2 = sampleData.getShort();
                byte data_ptr = sampleData.get();

                sample.sample_id = sample_id;
                sample.frequency = (short) getFrequency((short) 109);
                sample.volume = getVolume((byte) volume);
                sample.data_ptr = data_ptr;
                sample.data_len = data_len;
                sample.loop_pos = 0;
                sample.loop_len = 0;
                sample.unused1 = unused1;
                sample.unused2 = unused2;

                if (loop_len != 0) {
                    sample.data_len += loop_len;
                    sample.loop_pos += data_len;
                    sample.loop_len += loop_len;
                }

                System.out.printf(
                        "sample loaded [sample_id: 0x%02x, frequency: %d, volume: %d, data_ptr: 0x%x, data_len: %d, loop_pos: %d, loop_len: %d]%n",
                        sample.sample_id,
                        sample.frequency,
                        sample.volume,
                        sample.data_ptr,
                        sample.data_len,
                        sample.loop_pos,
                        sample.loop_len
                );

            }
        }
    }

    private ByteBuffer wrapArray(byte[] memory, int bufPtr, int size) {
        return ByteBuffer.wrap(memory).slice(bufPtr, size);
    }

    private MusicModule load_module(ByteBuffer data, MemEntry music, byte index, short ticks) {
        System.out.printf("play music [music_id: 0x%02x, index: %d, ticks: %d]\n", music.index, index, ticks);
        var _module = new MusicModule();
        _module.music_id = (short) music.index;
        _module.music_ticks = data.position(  0x00).getShort();
        _module.data_ptr =  0xc0;
        _module.data_pos = 0;
        _module.seq_index = index;
        _module.seq_count = (byte) data.position(  0x3e).getShort();
        if (ticks != 0) {
            _module.music_ticks = ticks;
        }
System.out.printf(            "module loaded [music_id: 0x%02x, ticks: %d, seq_index: %d, seq_count: %d, data_pos: %d, data_ptr: %d]\n",
        _module.music_id,
        _module.music_ticks,
        _module.seq_index,
        _module.seq_count,
        _module.data_pos,
        _module.data_ptr
);
        return _module;
    }


    private static int getFrequency(short period) {
        if(period < 55) {
            return 65535;
        }
        return ((Paula.Carrier / period)&0xFFFF);
    }

    private static  byte getVolume(short volume) {
        if(volume < 0x00) {
            volume = 0x00;
        }
        if(volume > 0x3f) {
            volume = 0x3f;
        }
        return (byte) volume;
    }

}
