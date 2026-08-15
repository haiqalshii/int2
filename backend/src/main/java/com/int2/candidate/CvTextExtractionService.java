package com.int2.candidate;

import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class CvTextExtractionService {
    public String extract(byte[] pdf) {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document).replace('\u0000', ' ').trim();
            if (text.isBlank()) {
                throw new InvalidCvException("Unable to extract text from this CV. Please upload a PDF with selectable text.");
            }
            return text;
        } catch (InvalidCvException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new InvalidCvException("Unable to extract text from this CV.", exception);
        }
    }
}

