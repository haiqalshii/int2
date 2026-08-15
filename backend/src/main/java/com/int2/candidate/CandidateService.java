package com.int2.candidate;

import com.int2.common.ResourceNotFoundException;
import java.io.IOException;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CandidateService {
    static final long MAX_CV_BYTES = 10L * 1024 * 1024;
    private final CandidateRepository repository;
    private final CvTextExtractionService textExtractionService;
    private final CvStorageService storageService;

    public CandidateService(CandidateRepository repository, CvTextExtractionService textExtractionService, CvStorageService storageService) {
        this.repository = repository;
        this.textExtractionService = textExtractionService;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public CandidateResponse getCandidate() {
        return repository.findFirstByOrderByUpdatedAtDesc().map(CandidateResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No candidate profile exists yet."));
    }

    @Transactional
    public CandidateResponse uploadCv(MultipartFile file) {
        byte[] bytes = validateAndRead(file);
        String text = textExtractionService.extract(bytes);
        String path = storageService.upload(bytes);
        CandidateProfile profile = repository.findFirstByOrderByUpdatedAtDesc()
                .orElseGet(() -> new CandidateProfile(null, null));
        profile.setCvFilePath(path);
        profile.setCvText(text);
        return CandidateResponse.from(repository.saveAndFlush(profile));
    }

    private byte[] validateAndRead(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new InvalidCvException("Please select a non-empty PDF CV.");
        if (file.getSize() > MAX_CV_BYTES) throw new CvTooLargeException("CV files must not exceed 10 MB.");
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()) || !filename.endsWith(".pdf")) {
            throw new InvalidCvException("Only PDF CV files are accepted.");
        }
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F' || bytes[4] != '-') {
                throw new InvalidCvException("The uploaded file is not a valid PDF.");
            }
            return bytes;
        } catch (IOException exception) {
            throw new InvalidCvException("Unable to read the uploaded CV.", exception);
        }
    }
}

