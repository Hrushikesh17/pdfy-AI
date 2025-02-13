package com.pdf.pdfReaderUsingHuggingFace.service;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    
    private static final int MAX_RETRIES = 3; // Maximum number of retries
    private static final int RETRY_DELAY_SECONDS = 10; // Delay between retries

    // Summarizes the provided text using Hugging Face API.
    public String summarizeText(String text) {

        if (apiKey == null || apiKey.isEmpty()) {
            return "Error: Missing Hugging Face API key.";
        }

        HttpHeaders headers = createHeaders();

        List<String> chunks = splitTextIntoChunks(text, 1500); 
        StringBuilder finalSummary = new StringBuilder();

        for (String chunk : chunks) {
            try {
                String summary = getSummaryWithRetry(chunk, headers);
                if (summary != null) {
                    finalSummary.append(summary).append("\n");
                }
            } catch (Exception e) {
                finalSummary.append("Error summarizing chunk: ").append(e.getMessage()).append("\n");
            }
        }

        return finalSummary.toString().trim();
    }

    // Processes a text chunk and gets a summary from the Hugging Face API.
    private String getSummaryWithRetry(String textChunk, HttpHeaders headers) throws Exception {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return getSummary(textChunk, headers);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Model too busy")) {
                    attempt++;
                    if (attempt < MAX_RETRIES) {
                        System.out.println("Model too busy, retrying... (attempt " + attempt + ")");
                        TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                    } else {
                        throw new Exception("Failed after " + MAX_RETRIES + " attempts: " + e.getMessage());
                    }
                } else {
                    throw e;
                }
            }
        }
        return null;
    }

    // Processes a text chunk and gets a summary from the Hugging Face API.
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

    // Splits long text into smaller chunks to fit Hugging Face API constraints.
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

    // Answers a question based on the provided context using Hugging Face API.
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
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.getBody());

                    // Extract only the answer
                    return rootNode.get("answer").asText();
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing API response: " + e.getMessage());
                }
            } else {
                return "Error: " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
        
    // Generates a creative summary for the provided text using Hugging Face API.
    public String creativeSummarizeText(String text) {
        // Ensure API key is available
        if (apiKey == null || apiKey.isEmpty()) {
            return "Error: Missing Hugging Face API key.";
        }

        HttpHeaders headers = createHeaders();

        List<String> chunks = splitTextIntoChunks(text, 1000); // Reduce chunk size for better API handling
        StringBuilder finalSummary = new StringBuilder();

        for (String chunk : chunks) {
            try {
                String summary = getCreativeSummaryWithRetry(chunk, headers);
                if (summary != null) {
                    finalSummary.append(summary).append("\n");
                }
            } catch (Exception e) {
                finalSummary.append("Error summarizing chunk: ").append(e.getMessage()).append("\n");
            }
        }

        return finalSummary.toString().trim();
    }

    // Processes a text chunk and gets a creative summary from the Hugging Face API.
    private String getCreativeSummaryWithRetry(String textChunk, HttpHeaders headers) throws Exception {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return getCreativeSummary(textChunk, headers);
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Model too busy") || e.getMessage().contains("unknown error")) {
                    attempt++;
                    if (attempt < MAX_RETRIES) {
                        System.out.println("Model too busy, retrying... (attempt " + attempt + ")");
                        TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
                    } else {
                        throw new Exception("Failed after " + MAX_RETRIES + " attempts: " + e.getMessage());
                    }
                } else {
                    throw e;
                }
            }
        }
        return null;
    }

    // Processes a text chunk and gets a creative summary from the Hugging Face API.
    private String getCreativeSummary(String textChunk, HttpHeaders headers) {
        Map<String, Object> body = new HashMap<>();
        body.put("inputs", textChunk);
        body.put("parameters", Map.of(
                "max_length", 250,  // Increase max length for more detailed summary
                "min_length", 100,  // Increase min length for more detailed summary
                "do_sample", true,  // Enable sampling for creative output
                "temperature", 1.0  // Increase temperature for more creativity
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(SUMMARIZATION_URL, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                // Extract the creative summary text from JSON array
                if (rootNode.isArray() && rootNode.size() > 0) {
                    return rootNode.get(0).get("summary_text").asText();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error parsing API response: " + e.getMessage());
            }
        }
        throw new RuntimeException("API error: " + response.getStatusCode());
    }
    
    
    // Generates a creative answer to a question based on the provided context using Hugging Face API.
    public String creativeAnswerQuestion(String context, String question) {
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("context", context);
        body.put("question", question);
        body.put("max_length", 150);  // Increase max length for more detailed answer
        body.put("min_length", 50);   // Increase min length for more detailed answer
        body.put("do_sample", true);  // Enable sampling for creative output
        body.put("temperature", 2.0); // Increase temperature for more creativity

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(QA_URL, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(response.getBody());

                    // Extract the creative answer
                    if (rootNode.has("answer")) {
                        return rootNode.get("answer").asText();
                    } else if (rootNode.has("generated_text")) {
                        return rootNode.get("generated_text").asText();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing API response: " + e.getMessage());
                }
            } else {
                return "Error: " + response.getStatusCode();
            }
            return "Error: Unable to generate creative answer.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    // Creates HTTP headers with API key.
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
