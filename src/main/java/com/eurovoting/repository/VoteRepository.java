package com.eurovoting.repository;

import com.eurovoting.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByFromCountryAndVoterRoleAndJuryMemberIdAndPoints(
            Country fromCountry,
            VoterRole voterRole,
            Integer juryMemberId,
            int points
    );

    boolean existsByFromCountryAndVoterRoleAndJuryMemberIdAndToContestant(
            Country fromCountry,
            VoterRole voterRole,
            Integer juryMemberId,
            Contestant toContestant
    );

    @Query("""
        SELECT COALESCE(SUM(v.points), 0)
        FROM Vote v
        WHERE v.toContestant = :contestant
          AND v.voterRole = :role
    """)
    int sumPointsByContestantAndRole(
            @Param("contestant") Contestant contestant,
            @Param("role") VoterRole role
    );

    @Query("""
        SELECT COUNT(v)
        FROM Vote v
        WHERE v.fromCountry = :country
          AND v.voterRole = :role
          AND v.juryMemberId = :memberId
    """)
    long countVotesByVoter(
            @Param("country") Country country,
            @Param("role") VoterRole role,
            @Param("memberId") Integer memberId
    );
}