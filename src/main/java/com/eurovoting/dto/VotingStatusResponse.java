package com.eurovoting.dto;

public class VotingStatusResponse {
    private Long ourCountryId;
    private String ourCountryName;
    private int totalRequiredPerSet;
    private long juryMember1VoteCount;
    private long juryMember2VoteCount;
    private long juryMember3VoteCount;
    private long audienceVoteCount;
    private boolean juryMember1Complete;
    private boolean juryMember2Complete;
    private boolean juryMember3Complete;
    private boolean audienceComplete;

    public Long getOurCountryId() {
        return ourCountryId;
    }

    public void setOurCountryId(Long ourCountryId) {
        this.ourCountryId = ourCountryId;
    }

    public String getOurCountryName() {
        return ourCountryName;
    }

    public void setOurCountryName(String ourCountryName) {
        this.ourCountryName = ourCountryName;
    }

    public int getTotalRequiredPerSet() {
        return totalRequiredPerSet;
    }

    public void setTotalRequiredPerSet(int totalRequiredPerSet) {
        this.totalRequiredPerSet = totalRequiredPerSet;
    }

    public long getJuryMember1VoteCount() {
        return juryMember1VoteCount;
    }

    public void setJuryMember1VoteCount(long juryMember1VoteCount) {
        this.juryMember1VoteCount = juryMember1VoteCount;
    }

    public long getJuryMember2VoteCount() {
        return juryMember2VoteCount;
    }

    public void setJuryMember2VoteCount(long juryMember2VoteCount) {
        this.juryMember2VoteCount = juryMember2VoteCount;
    }

    public long getJuryMember3VoteCount() {
        return juryMember3VoteCount;
    }

    public void setJuryMember3VoteCount(long juryMember3VoteCount) {
        this.juryMember3VoteCount = juryMember3VoteCount;
    }

    public long getAudienceVoteCount() {
        return audienceVoteCount;
    }

    public void setAudienceVoteCount(long audienceVoteCount) {
        this.audienceVoteCount = audienceVoteCount;
    }

    public boolean isJuryMember1Complete() {
        return juryMember1Complete;
    }

    public void setJuryMember1Complete(boolean juryMember1Complete) {
        this.juryMember1Complete = juryMember1Complete;
    }

    public boolean isJuryMember2Complete() {
        return juryMember2Complete;
    }

    public void setJuryMember2Complete(boolean juryMember2Complete) {
        this.juryMember2Complete = juryMember2Complete;
    }

    public boolean isJuryMember3Complete() {
        return juryMember3Complete;
    }

    public void setJuryMember3Complete(boolean juryMember3Complete) {
        this.juryMember3Complete = juryMember3Complete;
    }

    public boolean isAudienceComplete() {
        return audienceComplete;
    }

    public void setAudienceComplete(boolean audienceComplete) {
        this.audienceComplete = audienceComplete;
    }
}
