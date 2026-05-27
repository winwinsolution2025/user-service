package com.example.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class UpdateMeRequest {
    private String name;
    private String gender;
    private String nickname;
    private String avatar;
    @JsonProperty("avatar_id")
    private Integer avatarId;
    @JsonProperty("avatar_frame_id")
    private Integer avatarFrameId;
    private LocalDate birthdate;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Integer avatarId) {
        this.avatarId = avatarId;
    }

    public Integer getAvatarFrameId() {
        return avatarFrameId;
    }

    public void setAvatarFrameId(Integer avatarFrameId) {
        this.avatarFrameId = avatarFrameId;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }
} 