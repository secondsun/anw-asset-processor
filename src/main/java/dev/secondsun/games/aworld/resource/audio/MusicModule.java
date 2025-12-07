package dev.secondsun.games.aworld.resource.audio;

public class MusicModule {

    public short music_id = (short) 0xffff;
    public short music_ticks = 0;
    public int data_ptr = 0;
    public long data_pos = 0;
    public byte seq_index = 0;
    public byte seq_count = 0;
    public byte[] seq_table = new byte[0x80];
    public AudioSample[] samples = new AudioSample[15];

    public MusicModule() {
        for(int i = 0; i < samples.length; i++) {
            samples[i] = new AudioSample();
        }
    }
}
