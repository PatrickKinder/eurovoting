package com.eurovoting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contestant")
public class Contestant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(name = "song_title", nullable = false)
    private String songTitle;

    @OneToOne
    @JoinColumn(name = "country_id", nullable = false, unique = true)
    private Country country;

    public Long getId() { return id; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getSongTitle() { return songTitle; }
    public void setSongTitle(String songTitle) { this.songTitle = songTitle; }
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
}