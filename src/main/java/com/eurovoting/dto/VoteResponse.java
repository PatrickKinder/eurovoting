package com.eurovoting.dto;

import com.eurovoting.model.VoterRole;

import java.time.LocalDateTime;

public class VoteResponse {
    private Long id;
    private String fromCountryName;
    private String toArtistName;
    private String toSongTitle;
    private String toCountryName;
    private VoterRole voterRole;
    private int points;
    private Integer juryMemberId;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromCountryName() {
        return fromCountryName;
    }

    public void setFromCountryName(String fromCountryName) {
        this.fromCountryName = fromCountryName;
    }

    public String getToArtistName() {
        return toArtistName;
    }

    public void setToArtistName(String toArtistName) {
        this.toArtistName = toArtistName;
    }

    public String getToSongTitle() {
        return toSongTitle;
    }

    public void setToSongTitle(String toSongTitle) {
        this.toSongTitle = toSongTitle;
    }

    public String getToCountryName() {
        return toCountryName;
    }

    public void setToCountryName(String toCountryName) {
        this.toCountryName = toCountryName;
    }

    public VoterRole getVoterRole() {
        return voterRole;
    }

    public void setVoterRole(VoterRole voterRole) {
        this.voterRole = voterRole;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Integer getJuryMemberId() {
        return juryMemberId;
    }

    public void setJuryMemberId(Integer juryMemberId) {
        this.juryMemberId = juryMemberId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
