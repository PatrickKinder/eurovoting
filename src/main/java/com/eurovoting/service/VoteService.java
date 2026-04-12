package com.eurovoting.service;

import com.eurovoting.dto.VoteRequest;
import com.eurovoting.dto.VoteResponse;
import com.eurovoting.exception.NotFoundException;
import com.eurovoting.exception.VotingException;
import com.eurovoting.model.Contestant;
import com.eurovoting.model.Country;
import com.eurovoting.model.Vote;
import com.eurovoting.model.VoterRole;
import com.eurovoting.repository.ContestantRepository;
import com.eurovoting.repository.CountryRepository;
import com.eurovoting.repository.VoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class VoteService {

    private static final Set<Integer> VALID_POINTS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 10, 12);

    private final VoteRepository voteRepository;
    private final CountryRepository countryRepository;
    private final ContestantRepository contestantRepository;

    public VoteService(VoteRepository voteRepository,
                       CountryRepository countryRepository,
                       ContestantRepository contestantRepository) {
        this.voteRepository = voteRepository;
        this.countryRepository = countryRepository;
        this.contestantRepository = contestantRepository;
    }

    @Transactional
    public VoteResponse castVote(VoteRequest request) {
        Country fromCountry = countryRepository.findByOurCountryTrue()
                .orElseThrow(() -> new VotingException(
                        "Our country is not configured.", HttpStatus.INTERNAL_SERVER_ERROR));

        Contestant toContestant = contestantRepository.findById(request.getToContestantId())
                .orElseThrow(() -> new NotFoundException(
                        "Contestant not found with id: " + request.getToContestantId()));

        validatePoints(request.getPoints());
        validateNoSelfVoting(fromCountry, toContestant);
        validateJuryMemberId(request);
        validateNoDuplicatePoints(request, fromCountry);
        validateNoDuplicateContestant(request, fromCountry, toContestant);

        Vote vote = new Vote();
        vote.setFromCountry(fromCountry);
        vote.setToContestant(toContestant);
        vote.setVoterRole(request.getVoterRole());
        vote.setPoints(request.getPoints());
        vote.setJuryMemberId(request.getJuryMemberId());

        Vote saved = voteRepository.save(vote);
        return toResponse(saved);
    }

    private void validatePoints(int points) {
        if (!VALID_POINTS.contains(points)) {
            throw new VotingException(
                    "Invalid points: " + points + ". Allowed: " + VALID_POINTS,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void validateNoSelfVoting(Country fromCountry, Contestant toContestant) {
        if (fromCountry.getId().equals(toContestant.getCountry().getId())) {
            throw new VotingException("A country cannot vote for its own contestant.");
        }
    }

    private void validateJuryMemberId(VoteRequest request) {
        if (request.getVoterRole() == VoterRole.JURY) {
            Integer id = request.getJuryMemberId();
            if (id == null || id < 1 || id > 3) {
                throw new VotingException(
                        "JURY votes require juryMemberId between 1 and 3.",
                        HttpStatus.BAD_REQUEST
                );
            }
        } else {
            if (request.getJuryMemberId() != null) {
                throw new VotingException(
                        "AUDIENCE votes must not include a juryMemberId.",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    // DB-Unique-Constraint greift bei NULL nicht (SQL: NULL != NULL), daher Service-Prüfung.
    private void validateNoDuplicatePoints(VoteRequest request, Country fromCountry) {
        boolean exists = voteRepository.existsByFromCountryAndVoterRoleAndJuryMemberIdAndPoints(
                fromCountry,
                request.getVoterRole(),
                request.getJuryMemberId(),
                request.getPoints()
        );
        if (exists) {
            throw new VotingException(
                    "Point value " + request.getPoints() + " already assigned by this voter."
            );
        }
    }

    // DB-Unique-Constraint greift bei NULL nicht (SQL: NULL != NULL), daher Service-Prüfung.
    private void validateNoDuplicateContestant(VoteRequest request,
                                               Country fromCountry,
                                               Contestant toContestant) {
        boolean exists = voteRepository.existsByFromCountryAndVoterRoleAndJuryMemberIdAndToContestant(
                fromCountry,
                request.getVoterRole(),
                request.getJuryMemberId(),
                toContestant
        );
        if (exists) {
            throw new VotingException(
                    "Contestant '" + toContestant.getArtistName() + "' already rated by this voter."
            );
        }
    }

    private VoteResponse toResponse(Vote vote) {
        VoteResponse response = new VoteResponse();
        response.setId(vote.getId());
        response.setFromCountryName(vote.getFromCountry().getName());
        response.setToArtistName(vote.getToContestant().getArtistName());
        response.setToSongTitle(vote.getToContestant().getSongTitle());
        response.setToCountryName(vote.getToContestant().getCountry().getName());
        response.setVoterRole(vote.getVoterRole());
        response.setPoints(vote.getPoints());
        response.setJuryMemberId(vote.getJuryMemberId());
        response.setCreatedAt(vote.getCreatedAt());
        return response;
    }
}
