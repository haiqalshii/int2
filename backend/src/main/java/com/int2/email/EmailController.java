package com.int2.email;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailGenerationService generationService;
    public EmailController(EmailGenerationService generationService) { this.generationService = generationService; }

    @PostMapping("/generate")
    public EmailResponse generate(@Valid @RequestBody EmailGenerationRequest request) {
        return generationService.generate(request.companyId());
    }
}

