package dev.secondsun.games.aworld.resource.audio;

public class AudioChannel {
    public byte channel_id = (byte) 0xff;
    public byte active = 0;
    public byte volume = 0;
    public short sample_id = (short) 0xffff;
    public int data_ptr = 0;
    public int data_len = 0;
    public int data_pos = 0;
    public int data_inc = 0;
    public int loop_pos = 0;
    public int loop_len = 0;
}
