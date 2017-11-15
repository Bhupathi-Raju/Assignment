package com.example.zemoso.assignment.activities.model;

import java.io.Serializable;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zemoso on 7/11/17.
 */

public class VideoInfo extends RealmObject implements Serializable {

    @PrimaryKey
    private String id;
    private String videoPath;
    private RealmList<Float> xPosList = new RealmList<>();
    private RealmList<Float> yPosList = new RealmList<>();
    private RealmList<String> commentPaths = new RealmList<>();

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public RealmList<String> getCommentPath() {
        return commentPaths;
    }

    public void setCommentPath(String commentPath) {
        this.commentPaths.add(commentPath);
    }

    public RealmList<Float> getxPosList() {
        return xPosList;
    }

    public void setxPosList(Float xPos) {
        this.xPosList.add(xPos);
    }

    public RealmList<Float> getyPosList() {
        return yPosList;
    }

    public void setyPosList(Float yPos) {
        this.yPosList.add(yPos);
    }
}
