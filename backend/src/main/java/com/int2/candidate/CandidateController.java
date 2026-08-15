package com.int2.candidate;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidate")
public class CandidateController {
    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) { this.candidateService = candidateService; }

    @GetMapping
    public CandidateResponse candidate() { return candidateService.getCandidate(); }

    @PostMapping(value = "/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CandidateResponse uploadCv(@RequestPart("file") MultipartFile file) {
        return candidateService.uploadCv(file);
    }
}

