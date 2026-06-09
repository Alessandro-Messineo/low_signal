package org.ecommerce.java.low_signal.service;

import org.ecommerce.java.low_signal.model.Collection;
import org.ecommerce.java.low_signal.repository.CollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    public List<Collection> getAllCollections() {
        return collectionRepository.findAll();
    }

    public Collection createCollection(Collection collection) {
        return collectionRepository.save(collection);
    }

    public Optional<Collection> updateCollection(Integer id, Collection collection) {
        if (!collectionRepository.existsById(id)) {
            return Optional.empty();
        }

        collection.setId(id);
        return Optional.of(collectionRepository.save(collection));
    }

}