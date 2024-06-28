package com.ProDoc.controller;

import com.ProDoc.model.FileContent;
import com.ProDoc.service.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class CodeController {

    @Autowired
    private GroqService groqService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> chat(@RequestBody FileContent file) {
        if (file == null || file.getPath() == null || file.getContent() == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid file provided");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        String prompt = groqService.constructPrompt(file);

        try {
            String description = groqService.getChatCompletion(prompt);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("response", description);
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
