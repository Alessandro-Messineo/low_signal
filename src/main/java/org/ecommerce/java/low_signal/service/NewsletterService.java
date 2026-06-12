package org.ecommerce.java.low_signal.service;

import java.util.Optional;

import org.ecommerce.java.low_signal.model.Newsletter;
import org.ecommerce.java.low_signal.repository.NewsletterRepository;
import org.springframework.stereotype.Service;

@Service
public class NewsletterService {

    private final  NewsletterRepository newsletterRepository;

    public NewsletterService(NewsletterRepository newsletterRepository) {
        this.newsletterRepository = newsletterRepository;
    }

    public Newsletter subscribe(String email) {

        if (newsletterRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already subscribed: " + email);
        }

        Newsletter newsletter = new Newsletter();
        newsletter.setEmail(email);

        return newsletterRepository.save(newsletter);
    }

    public Optional<Newsletter> findByEmail(String email) {
        return newsletterRepository.findByEmail(email);
    }

}
