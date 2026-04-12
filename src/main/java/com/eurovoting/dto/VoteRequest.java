package com.eurovoting.dto;

import com.eurovoting.model.VoterRole;
import jakarta.validation.constraints.NotNull;

public class VoteRequest {

    @NotNull(message = "toContestantId is required")
    private Long toContestantId;

    @NotNull(message = "voterRole is required")
    private VoterRole voterRole;

    @NotNull(message = "points is required")
    private Integer points;

    private Integer juryMemberId;

    public Long getToContestantId() {
        return toContestantId;
    }

    public void setToContestantId(Long toContestantId) {
        this.toContestantId = toContestantId;
    }

    public VoterRole getVoterRole() {
        return voterRole;
    }

    public void setVoterRole(VoterRole voterRole) {
        this.voterRole = voterRole;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getJuryMemberId() {
        return juryMemberId;
    }

    public void setJuryMemberId(Integer juryMemberId) {
        this.juryMemberId = juryMemberId;
    }
}
