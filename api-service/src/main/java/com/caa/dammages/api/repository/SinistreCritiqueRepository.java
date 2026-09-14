package com.caa.dammages.api.repository;

import com.caa.dammages.api.model.SinistreCritique;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SinistreCritiqueRepository extends MongoRepository<SinistreCritique, String> {

    List<SinistreCritique> findByContratId(String contratId);

    List<SinistreCritique> findByMontantSinistreGreaterThan(double montant);

    List<SinistreCritique> findAllByOrderByDateDeclarationDesc();
}
