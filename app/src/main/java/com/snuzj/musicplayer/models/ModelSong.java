package com.snuzj.musicplayer.models;

public class ModelSong {
    String uid, id, title,album, artist, categoryId, duration, imageUrl, audioUrl;
    long timestamp;

    public ModelSong() {

    }

    public ModelSong(String uid, String id, String title, String artist, String categoryId, String duration, String imageUrl, String audioUrl, long timestamp, String album) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.categoryId = categoryId;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
        this.timestamp = timestamp;
        this.album = album;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
