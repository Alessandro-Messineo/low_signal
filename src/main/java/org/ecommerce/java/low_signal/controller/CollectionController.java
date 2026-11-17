package org.ecommerce.java.low_signal.controller;

import org.ecommerce.java.low_signal.model.Collection;
import org.ecommerce.java.low_signal.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
// /api/collections e' il path canonico: con il proxy same-origin di Vercel
// "/collections" collide con la rotta SPA /collections (pagina). Il vecchio
// path resta temporaneamente per non rompere il frontend gia' deployato:
// rimuoverlo dopo il deploy del frontend.
@RequestMapping({ "/api/collections", "/collections" })
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ResponseEntity<List<Collection>> getAllCollections() {
        List<Collection> collections = collectionService.getAllCollections();
        return ResponseEntity.ok(collections);
    }

    @PostMapping
    public ResponseEntity<Collection> createCollection(@RequestBody Collection collection) {
        Collection newCollection = collectionService.createCollection(collection);
        return ResponseEntity.ok(newCollection);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Collection> updateCollection(@PathVariable Integer id, @RequestBody Collection collection) {
        Optional<Collection> updated = collectionService.updateCollection(id, collection);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}