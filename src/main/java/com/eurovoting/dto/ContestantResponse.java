package com.eurovoting.dto;

public class ContestantResponse {
    private Long id;
    private String artistName;
    private String songTitle;
    private String countryName;
    private String countryCode;

    public ContestantResponse(Long id, String artistName, String songTitle,
                              String countryName, String countryCode) {
        this.id = id;
        this.artistName = artistName;
        this.songTitle = songTitle;
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public Long getId() { return id; }
    public String getArtistName() { return artistName; }
    public String getSongTitle() { return songTitle; }
    public String getCountryName() { return countryName; }
    public String getCountryCode() { return countryCode; }
}