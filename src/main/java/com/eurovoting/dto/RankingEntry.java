package com.eurovoting.dto;

public class RankingEntry {
    private int rank;
    private Long contestantId;
    private String artistName;
    private String songTitle;
    private String countryName;
    private String countryCode;
    private int totalPoints;
    private int juryPoints;
    private int audiencePoints;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Long getContestantId() {
        return contestantId;
    }

    public void setContestantId(Long contestantId) {
        this.contestantId = contestantId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getJuryPoints() {
        return juryPoints;
    }

    public void setJuryPoints(int juryPoints) {
        this.juryPoints = juryPoints;
    }

    public int getAudiencePoints() {
        return audiencePoints;
    }

    public void setAudiencePoints(int audiencePoints) {
        this.audiencePoints = audiencePoints;
    }
}