package com.eurovoting.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "vote",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_vote_voter_contestant",
                        columnNames = {"from_country_id", "to_contestant_id", "voter_role", "jury_member_id"}
                )
        }
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "from_country_id", nullable = false)
    private Country fromCountry;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "to_contestant_id", nullable = false)
    private Contestant toContestant;

    @Enumerated(EnumType.STRING)
    @Column(name = "voter_role", nullable = false)
    private VoterRole voterRole;

    @Column(nullable = false)
    private int points;

    // ⚠️ WICHTIG: DB-Unique-Constraint greift bei NULL nicht (SQL: NULL != NULL)
    // Duplikat-Prüfung für AUDIENCE läuft ausschließlich im VoteService! (Person 1)
    @Column(name = "jury_member_id")
    private Integer juryMemberId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Country getFromCountry() { return fromCountry; }
    public void setFromCountry(Country fromCountry) { this.fromCountry = fromCountry; }
    public Contestant getToContestant() { return toContestant; }
    public void setToContestant(Contestant toContestant) { this.toContestant = toContestant; }
    public VoterRole getVoterRole() { return voterRole; }
    public void setVoterRole(VoterRole voterRole) { this.voterRole = voterRole; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public Integer getJuryMemberId() { return juryMemberId; }
    public void setJuryMemberId(Integer juryMemberId) { this.juryMemberId = juryMemberId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}