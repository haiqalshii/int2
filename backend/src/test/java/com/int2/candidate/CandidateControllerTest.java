package com.int2.candidate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CandidateControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired CandidateRepository repository;
    @MockitoBean CvStorageService storageService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        when(storageService.upload(any(byte[].class))).thenReturn("candidate/cv.pdf");
    }

    @Test
    void uploadsExtractsAndReturnsSafeCandidateMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cv.pdf", "application/pdf", pdfWithText("Java cybersecurity student"));
        mockMvc.perform(multipart("/api/candidate/cv").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvUploaded").value(true))
                .andExpect(jsonPath("$.cvText").doesNotExist())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        mockMvc.perform(get("/api/candidate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvUploaded").value(true))
                .andExpect(jsonPath("$.cvText").doesNotExist());
    }

    @Test
    void rejectsNonPdfAndPdfWithoutSelectableText() throws Exception {
        MockMultipartFile text = new MockMultipartFile("file", "cv.txt", "text/plain", "hello".getBytes());
        mockMvc.perform(multipart("/api/candidate/cv").file(text))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Only PDF CV files are accepted."));

        MockMultipartFile emptyPdf = new MockMultipartFile("file", "cv.pdf", "application/pdf", pdfWithText(null));
        mockMvc.perform(multipart("/api/candidate/cv").file(emptyPdf))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Unable to extract text from this CV. Please upload a PDF with selectable text."));
    }

    @Test
    void reportsMissingCandidate() throws Exception {
        mockMvc.perform(get("/api/candidate"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No candidate profile exists yet."));
    }

    private byte[] pdfWithText(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            if (text != null) {
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(72, 720);
                    content.showText(text);
                    content.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
