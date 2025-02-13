package com.pdf.pdfReaderUsingHuggingFace.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pdf.pdfReaderUsingHuggingFace.service.PdfService;
import com.pdf.pdfReaderUsingHuggingFace.util.PdfReader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    // Endpoint to upload PDF and get summary
    @PostMapping(value="/summarize",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public String summarizePdf(@RequestParam("file") MultipartFile file) {
        try {
            // Save the uploaded PDF to a temporary file
            File tempFile = File.createTempFile("uploaded", ".pdf");
            file.transferTo(tempFile);

            // Extract text from the PDF
            String extractedText = PdfReader.extractTextFromPdf(tempFile);

            // Summarize the extracted text
            return pdfService.summarizeText(extractedText);
        } catch (IOException e) {
            return "Error processing the PDF: " + e.getMessage();
        }
    }

 
     // Endpoint to ask a question based on extracted PDF content
    @PostMapping(value="/answer",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public String answerQuestion(@RequestParam("file") MultipartFile file, @RequestParam("question") String question) {
        try {
            // Save the uploaded PDF to a temporary file
            File tempFile = File.createTempFile("uploaded", ".pdf");
            file.transferTo(tempFile);

            // Extract text from the PDF
            String extractedText = PdfReader.extractTextFromPdf(tempFile);

            // Get the answer for the provided question
            return pdfService.answerQuestion(extractedText, question);
        } catch (IOException e) {
            return "Error processing the PDF: " + e.getMessage();
        }
    }
    
    // Endpoint to upload PDF and get summary
    @PostMapping(value="/creativesummarize",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public String creativeSummarizePdf(@RequestParam("file") MultipartFile file) {
        try {
            // Save the uploaded PDF to a temporary file
            File tempFile = File.createTempFile("uploaded", ".pdf");
            file.transferTo(tempFile);

            // Extract text from the PDF
            String extractedText = PdfReader.extractTextFromPdf(tempFile);

            // Summarize the extracted text
            return pdfService.creativeSummarizeText(extractedText);
        } catch (IOException e) {
            return "Error processing the PDF: " + e.getMessage();
        }
    }

 
     // Endpoint to ask a question based on extracted PDF content
    @PostMapping(value="/creativeanswer",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public String creativeAnswerQuestion(@RequestParam("file") MultipartFile file, @RequestParam("question") String question) {
        try {
            // Save the uploaded PDF to a temporary file
            File tempFile = File.createTempFile("uploaded", ".pdf");
            file.transferTo(tempFile);

            // Extract text from the PDF
            String extractedText = PdfReader.extractTextFromPdf(tempFile);

            // Get the answer for the provided question
            return pdfService.creativeAnswerQuestion(extractedText, question);
        } catch (IOException e) {
            return "Error processing the PDF: " + e.getMessage();
        }
    }
}



