package com.eurovoting.controller;

import com.eurovoting.dto.ContestantResponse;
import com.eurovoting.repository.ContestantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contestants")
@CrossOrigin(origins = "*")
public class ContestantController {

    private final ContestantRepository contestantRepository;

    public ContestantController(ContestantRepository contestantRepository) {
        this.contestantRepository = contestantRepository;
    }

    @GetMapping
    public ResponseEntity<List<ContestantResponse>> getAllContestants() {
        List<ContestantResponse> response = contestantRepository.findAll().stream()
                .map(c -> new ContestantResponse(
                        c.getId(),
                        c.getArtistName(),
                        c.getSongTitle(),
                        c.getCountry().getName(),
                        c.getCountry().getCode()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }
}