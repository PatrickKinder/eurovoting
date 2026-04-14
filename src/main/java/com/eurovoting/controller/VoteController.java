package com.eurovoting.controller;

import com.eurovoting.dto.VoteRequest;
import com.eurovoting.dto.VoteResponse;
import com.eurovoting.dto.VotingStatusResponse;
import com.eurovoting.service.VoteService;
import com.eurovoting.service.VotingStatusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/votes")
@CrossOrigin(origins = "*")
public class VoteController {

    private final VoteService voteService;
    private final VotingStatusService votingStatusService;

    public VoteController(VoteService voteService,
                          VotingStatusService votingStatusService) {
        this.voteService = voteService;
        this.votingStatusService = votingStatusService;
    }

    @PostMapping
    public ResponseEntity<VoteResponse> castVote(@Valid @RequestBody VoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voteService.castVote(request));
    }

    @GetMapping("/status")
    public ResponseEntity<VotingStatusResponse> getStatus() {
        return ResponseEntity.ok(votingStatusService.getStatusForOurCountry());
    }
}
