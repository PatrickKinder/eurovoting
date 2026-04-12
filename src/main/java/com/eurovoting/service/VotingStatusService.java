package com.eurovoting.service;

import com.eurovoting.dto.VotingStatusResponse;
import com.eurovoting.exception.VotingException;
import com.eurovoting.model.VoterRole;
import com.eurovoting.repository.CountryRepository;
import com.eurovoting.repository.VoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VotingStatusService {

    private static final int REQUIRED_VOTES_PER_SET = 10;

    private final VoteRepository voteRepository;
    private final CountryRepository countryRepository;

    public VotingStatusService(VoteRepository voteRepository,
                               CountryRepository countryRepository) {
        this.voteRepository = voteRepository;
        this.countryRepository = countryRepository;
    }

    @Transactional(readOnly = true)
    public VotingStatusResponse getStatusForOurCountry() {
        var ourCountry = countryRepository.findByOurCountryTrue()
                .orElseThrow(() -> new VotingException(
                        "Our country not configured.", HttpStatus.INTERNAL_SERVER_ERROR));

        long m1 = voteRepository.countVotesByVoter(ourCountry, VoterRole.JURY, 1);
        long m2 = voteRepository.countVotesByVoter(ourCountry, VoterRole.JURY, 2);
        long m3 = voteRepository.countVotesByVoter(ourCountry, VoterRole.JURY, 3);
        long audience = voteRepository.countVotesByVoter(ourCountry, VoterRole.AUDIENCE, null);

        VotingStatusResponse response = new VotingStatusResponse();
        response.setOurCountryId(ourCountry.getId());
        response.setOurCountryName(ourCountry.getName());
        response.setTotalRequiredPerSet(REQUIRED_VOTES_PER_SET);
        response.setJuryMember1VoteCount(m1);
        response.setJuryMember2VoteCount(m2);
        response.setJuryMember3VoteCount(m3);
        response.setAudienceVoteCount(audience);
        response.setJuryMember1Complete(m1 >= REQUIRED_VOTES_PER_SET);
        response.setJuryMember2Complete(m2 >= REQUIRED_VOTES_PER_SET);
        response.setJuryMember3Complete(m3 >= REQUIRED_VOTES_PER_SET);
        response.setAudienceComplete(audience >= REQUIRED_VOTES_PER_SET);
        return response;
    }
}
