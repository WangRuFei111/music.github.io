package com.example.musicplayer.bean;

public class MusicPlayerBean {

    private String id; // 歌曲id
    private String song; // 歌曲名称
    private String singer; // 歌手名称
    private String album; // 专辑名称
    private String duration; // 歌曲时长
    private String pach; // 歌曲路径

    public MusicPlayerBean() {    // 空参构造
    }

    public MusicPlayerBean(String id, String song, String singer, String album, String duration,
                           String pach) {
        this.id = id;
        this.song = song;
        this.singer = singer;
        this.album = album;
        this.duration = duration;
        this.pach = pach;
    }     // 实参构造

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPach() {
        return pach;
    }

    public void setPach(String pach) {
        this.pach = pach;
    }
}
