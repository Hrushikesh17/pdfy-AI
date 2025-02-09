package com.pdf.pdfReaderUsingHuggingFace.service;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PdfService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Hugging Face Summarization Model URL
    private static final String SUMMARIZATION_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";

    // Hugging Face Question-Answering Model URL
    private static final String QA_URL = "https://api-inference.huggingface.co/models/deepset/roberta-base-squad2";

    /**
     * Summarizes the provided text using Hugging Face API.
     *
     * @param text The input text to summarize.
     * @return The summarized text.
     */
    public String summarizeText(String text) {
        // Ensure API key is available
        if (apiKey == null || apiKey.isEmpty()) {
            return "Error: Missing Hugging Face API key.";
        }

        HttpHeaders headers = createHeaders();

        List<String> chunks = splitTextIntoChunks(text, 1500); // Reduce chunk size for better API handling
        StringBuilder finalSummary = new StringBuilder();

        for (String chunk : chunks) {
            try {
                String summary = getSummary(chunk, headers);
                if (summary != null) {
                    finalSummary.append(summary).append("\n");
                }
            } catch (Exception e) {
                finalSummary.append("Error summarizing chunk: ").append(e.getMessage()).append("\n");
            }
        }

        return finalSummary.toString().trim();
    }

    /**
     * Processes a text chunk and gets a summary from the Hugging Face API.
     */
    private String getSummary(String textChunk, HttpHeaders headers) {
        Map<String, Object> body = new HashMap<>();
        body.put("inputs", textChunk);
        body.put("parameters", Map.of(
                "max_length", 200,
                "min_length", 50,
                "do_sample", false
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(SUMMARIZATION_URL, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                // Extract the summary text from JSON array
                if (rootNode.isArray() && rootNode.size() > 0) {
                    return rootNode.get(0).get("summary_text").asText();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error parsing API response: " + e.getMessage());
            }
        }
        throw new RuntimeException("API error: " + response.getStatusCode());
    }

    /**
     * Splits long text into smaller chunks to fit Hugging Face API constraints.
     */
    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }

    /**
     * Answers a question based on the provided context using Hugging Face API.
     *
     * @param context  The text to extract an answer from.
     * @param question The question to be answered.
     * @return The answer or an error message.
     */
    public String answerQuestion(String context, String question) {
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("context", context);
        body.put("question", question);
        body.put("max_length", 100);  // Request a longer answer if supported

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(QA_URL, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                // Extract only the answer
                return rootNode.get("answer").asText();
            } else {
                return "Error: " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Creates HTTP headers with API key.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}