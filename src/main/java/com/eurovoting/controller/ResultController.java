package com.eurovoting.controller;

import com.eurovoting.dto.RankingEntry;
import com.eurovoting.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "*")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    public ResponseEntity<List<RankingEntry>> getResults() {
        return ResponseEntity.ok(resultService.calculateRanking());
    }
}