package com.ProDoc.service;

import com.ProDoc.model.FileContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"; // Correct this URL

    public String getChatCompletion(String prompt) throws Exception {
        // Prepare the request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("messages", List.of(
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
        ));
        payload.put("model", "llama3-8b-8192");

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Create the HTTP entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Call the Groq API
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                GROQ_API_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Extract the response content
        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                if (message != null) {
                    String responseContent = (String) message.get("content");

                    // Extract content between <start> and <end> markers
                    String startMarker = "<start>";
                    String endMarker = "<end>";
                    int startIndex = responseContent.indexOf(startMarker) + startMarker.length();
                    int endIndex = responseContent.indexOf(endMarker);

                    if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                        return responseContent.substring(startIndex, endIndex).trim();
                    } else {
                        return responseContent;  // Fallback to full response if markers are not found
                    }
                }
            }
            throw new Exception("Invalid response structure from Groq API");
        } else {
            throw new Exception("No response from Groq API");
        }
    }

    public String constructPrompt(FileContent file) {
        return "I have a program file in my project. I want you to create a proper description about what is happening in this code. I will be providing you with the file path and the file content. Provide the detailed documentation that you will be provided to be enclosed between the markers \"<start>\" and \"</end>\"."
               + " File path is: " + file.getPath()
               + " and file content is: " + file.getContent();
    }
}
