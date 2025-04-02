package com.yl.deepseekxunfei.page;

import java.util.List;

public class LocationMusccarResult {
    String songName;//歌手名
    String  album;//歌曲名
    String  artist;// 专辑
    List musicId;//歌曲id

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
    public LocationMusccarResult(String songName, String album, String artist) {
        this.songName = songName;
        this.album = album;
        this.artist = artist;
    }

    public List getMusicId() {
        return musicId;
    }

    public void setMusicId(List musicId) {
        this.musicId = musicId;
    }

    public LocationMusccarResult(String songName, String album, String artist, List musicId) {
        this.songName = songName;
        this.album = album;
        this.artist = artist;
        this.musicId = musicId;
    }

    @Override
    public String toString() {
        return "LocationMusccarResult{" +
                "songName='" + songName + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", musicId=" + musicId +
                '}';
    }
}
