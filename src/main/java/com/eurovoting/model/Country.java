package com.eurovoting.model;

import jakarta.persistence.*;

@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(name = "our_country", nullable = false)
    private boolean ourCountry = false;

    @Column(nullable = false)
    private boolean simulated = false;

    @OneToOne(mappedBy = "country", fetch = FetchType.LAZY)
    private Contestant contestant;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public boolean isOurCountry() { return ourCountry; }
    public void setOurCountry(boolean ourCountry) { this.ourCountry = ourCountry; }
    public boolean isSimulated() { return simulated; }
    public void setSimulated(boolean simulated) { this.simulated = simulated; }
    public Contestant getContestant() { return contestant; }
}