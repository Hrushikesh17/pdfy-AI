package com.pdf.pdfReaderUsingHuggingFace.util;

import java.io.File;
import java.io.IOException;


import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.text.PDFTextStripper;

public class PdfReader {
    public static String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String extractedText = pdfStripper.getText(document);

            // Replace problematic characters with alternatives
            extractedText = extractedText.replace("negationslash", "¬")
                    .replace("star", "*");

            return extractedText;
        }
    }
}