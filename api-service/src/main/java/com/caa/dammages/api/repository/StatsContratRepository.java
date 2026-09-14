package com.caa.dammages.api.repository;

import com.caa.dammages.api.model.SinistreCritique;
import com.caa.dammages.api.model.StatsContrat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsContratRepository extends MongoRepository<StatsContrat, String> {

    StatsContrat findByContratId(String contratId);

    List<StatsContrat> findByContratIdOrderByWindowEndDesc(String contratId);
}
