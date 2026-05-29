package org.ecommerce.java.low_signal.service;

import org.ecommerce.java.low_signal.model.Collection;
import org.ecommerce.java.low_signal.repository.CollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    public List<Collection> getAllCollections() {
        return collectionRepository.findAll();
    }

}