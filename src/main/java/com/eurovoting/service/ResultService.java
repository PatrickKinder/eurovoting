package com.eurovoting.service;

import com.eurovoting.dto.RankingEntry;
import com.eurovoting.model.Contestant;
import com.eurovoting.model.VoterRole;
import com.eurovoting.repository.ContestantRepository;
import com.eurovoting.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ResultService {

    private final VoteRepository voteRepository;
    private final ContestantRepository contestantRepository;

    public ResultService(VoteRepository voteRepository,
                         ContestantRepository contestantRepository) {
        this.voteRepository = voteRepository;
        this.contestantRepository = contestantRepository;
    }

    @Transactional(readOnly = true)
    public List<RankingEntry> calculateRanking() {
        List<RankingEntry> ranking = contestantRepository.findAll().stream()
                .map(this::toRankingEntry)
                .sorted(
                        Comparator.comparingInt(RankingEntry::getTotalPoints).reversed()
                                .thenComparing(e -> e.getCountryName().toLowerCase(Locale.ROOT))
                )
                .collect(Collectors.toList());

        IntStream.range(0, ranking.size())
                .forEach(i -> ranking.get(i).setRank(i + 1));

        return ranking;
    }

    private RankingEntry toRankingEntry(Contestant contestant) {
        int jury = safePoints(voteRepository.sumPointsByContestantAndRole(contestant, VoterRole.JURY));
        int audience = safePoints(voteRepository.sumPointsByContestantAndRole(contestant, VoterRole.AUDIENCE));

        RankingEntry e = new RankingEntry();
        e.setContestantId(contestant.getId());
        e.setArtistName(contestant.getArtistName());
        e.setSongTitle(contestant.getSongTitle());
        e.setCountryName(contestant.getCountry().getName());
        e.setCountryCode(contestant.getCountry().getCode());
        e.setJuryPoints(jury);
        e.setAudiencePoints(audience);
        e.setTotalPoints(jury + audience);

        return e;
    }

    private int safePoints(Integer points) {
        return points != null ? points : 0;
    }
}
