import dev.secondsun.games.aworld.ResourceReader;
import dev.secondsun.games.aworld.resource.MemEntry;
import dev.secondsun.games.aworld.resource.audio.*;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static dev.secondsun.games.aworld.resource.Resource.RT_MUSIC;
import static dev.secondsun.games.aworld.resource.Resource.RT_SOUND;
import static dev.secondsun.games.aworld.resource.audio.Paula.getFrequency;
import static dev.secondsun.games.aworld.resource.audio.Paula.getVolume;

public class MusicScratchTests {
    public static final int SAMPLE_RATE = 44100;

    @Test
    void scratchMusic() {
        var resources = new ResourceReader().readAllResources();
        var memory = resources.resourcesByteBin();
        var memEntryList = resources.memList();

        var sounds = memEntryList.stream().filter(it -> it.type == RT_SOUND && it.size != 0).toList();
        var musics = memEntryList.stream().filter(it -> it.type == RT_MUSIC).toList();
        var channels = new ArrayList<AudioChannel>();
        for (MemEntry music : musics) {
            var data = ByteBuffer.wrap(memory, music.bufPtr, music.size);
            var _module = load_module(data, music, (byte) 0, (short) 0);
            load_samples(data, music, _module,memEntryList, memory);
            load_seq_table(data, music, _module);

            //music#183
            while(_module.seq_index < _module.seq_count) {
                var sequence = _module.seq_table[_module.seq_index++];
                var data2 = ByteBuffer.wrap(data.array(), (int)(_module.data_ptr + _module.data_pos + sequence * 1024),data.limit());
                for(byte channel = 0; channel < 4; ++channel) {
                    var temp = processPattern(channel, data2, _module);
                    if (temp != null) {
                        channels.add(temp);
                    }
                }
                //MIXER.MIXALLCHANNELS


                _module.data_pos += data2.position() - data.position();
                if(_module.data_pos >= 1024) {
                    _module.data_pos = 0;
                    var seq_index = _module.seq_index + 1;
                    var seq_count = _module.seq_count;
                    if(seq_index >= seq_count) {
                        System.out.printf("music is over [music_id: 0x%02x]\n", _module.music_id);
                        break;
                    }
                    else {
                        _module.seq_index = (byte) seq_index;
                    }
                }
            }

//          playPattern(data, module);
        }

    }

    private AudioChannel processPattern(byte index, ByteBuffer data, MusicModule _module) {

        MusicPattern pattern = new MusicPattern();
        pattern.word1 = data.getShort();
        pattern.word2 = data.getShort();
        if(pattern.word1 == 0x0000) {
            return null;
        }
        else if(pattern.word1 == 0xfffd) {
            return null;
        }
        else if(pattern.word1 == 0xfffe) {
            return null;
        } else {
        short period_value = (short) ((pattern.word1 & 0x0fff) >>  0);
        byte  sample_index = (byte) ((pattern.word2 & 0xf000) >> 12);
        byte  effect_index = (byte) ((pattern.word2 & 0x0f00) >>  8);
        byte  effect_value = (byte) ((pattern.word2 & 0x00ff) >>  0);
            if(sample_index != 0) {
                var sample = (_module.samples[sample_index - 1]);
                short volume = sample.volume;
                switch(effect_index) {
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
                sample.frequency = getFrequency((byte) period_value);
                sample.volume    = getVolume((byte) volume);

                var channel = new AudioChannel();
                channel.active    = 1;
                channel.volume    = sample.volume;
                channel.sample_id = sample.sample_id;
                channel.data_ptr  = sample.data_ptr;
                channel.data_len  = (sample.data_len);
                channel.data_pos  = (0);
                channel.data_inc  = ((sample.frequency) << 8) / SAMPLE_RATE;
                channel.loop_pos  = (sample.loop_pos);
                channel.loop_len  = (sample.loop_len);
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
        data.position(data.position() + 0x40);
        for( int sequence = 0; sequence < _module.seq_table.length; sequence++) {
            _module.seq_table[sequence] = data.get();
        }
    }

    private void load_samples(ByteBuffer data, MemEntry music, MusicModule _module, List<MemEntry> memEntryList, byte[] memory) {
        data.position(data.position() + 0x02);
        for (AudioSample sample : _module.samples) {
            if (sample == null) continue;

            short sample_id = data.getShort();
            short volume = data.getShort();

            if (sample_id != 0) {
                System.out.println("load sample [sound_id: " + String.format("0x%02x", sample_id) + ", volume: " + volume + "]");

                var resource = memEntryList.get(sample_id);
                if(resource == null) {
                    Logger.getAnonymousLogger().log(java.util.logging.Level.INFO, String.format("resource not found [sound_id: 0x%02x]", sample_id));
                }
                else if(resource.type != RT_SOUND) {
                    Logger.getAnonymousLogger().log(java.util.logging.Level.INFO, String.format("resource not invalid [sound_id: 0x%02x]", sample_id));

                }
                var sampleData = ByteBuffer.wrap(memory, resource.bufPtr, resource.size);
                int data_len = (sampleData.getShort() & 0xFFFF) * 2;
                int loop_len = (sampleData.getShort() & 0xFFFF) * 2;
                short unused1 = sampleData.getShort();
                short unused2 = sampleData.getShort();
                byte data_ptr = sampleData.get();

                sample.sample_id = sample_id;
                sample.frequency = getFrequency((byte) 109);
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
            }
        }
    }

    private MusicModule load_module(ByteBuffer data, MemEntry music, byte index, short ticks) {
        var _module             = new MusicModule();
        _module.music_id    = (short) music.index;
        _module.music_ticks = data.position(data.position() + 0x00).getShort();
        _module.data_ptr    = data.position(data.position() + 0xc0).get();
        _module.data_pos    = 0;
        _module.seq_index   = index;
        _module.seq_count   = (byte) data.position(data.position() + 0x3e).getShort();
        if(ticks != 0) {
            _module.music_ticks = ticks;
        }
        return _module;
    }
}
