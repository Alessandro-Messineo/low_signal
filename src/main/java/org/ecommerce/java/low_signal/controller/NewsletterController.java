package org.ecommerce.java.low_signal.controller;

import java.util.Map;

import org.ecommerce.java.low_signal.model.Newsletter;
import org.ecommerce.java.low_signal.service.NewsletterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true") 
@RequestMapping("/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> subscribe(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        try {
            Newsletter newsletter = newsletterService.subscribe(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully subscribed!",
                    "email", newsletter.getEmail()
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()));
        }
    }

}
