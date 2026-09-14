package com.caa.dammages.api.controller;

import com.caa.dammages.api.model.SinistreCritique;
import com.caa.dammages.api.model.StatsContrat;
import com.caa.dammages.api.repository.SinistreCritiqueRepository;
import com.caa.dammages.api.repository.StatsContratRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsContratController {

    private final StatsContratRepository statsRepository;
    private final SinistreCritiqueRepository sinistresRepository;

    public StatsContratController(StatsContratRepository statsRepository,
                                  SinistreCritiqueRepository sinistresRepository) {
        this.statsRepository = statsRepository;
        this.sinistresRepository = sinistresRepository;
    }

    @GetMapping("/contrat/{contratId}")
    public ResponseEntity<StatsContrat> getStatsByContrat(
            @PathVariable String contratId) {
        StatsContrat stats = statsRepository.findByContratId(contratId);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/contrat/{contratId}/historique")
    public ResponseEntity<List<StatsContrat>> getHistoriqueByContrat(
            @PathVariable String contratId) {
        List<StatsContrat> historique =
                statsRepository.findByContratIdOrderByWindowEndDesc(contratId);
        return ResponseEntity.ok(historique);
    }

    @GetMapping("/critiques")
    public ResponseEntity<Page<SinistreCritique>> getSinistresCritiques(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "dateDeclaration"));
        Page<SinistreCritique> critiques =
                sinistresRepository.findAll(pageable);
        return ResponseEntity.ok(critiques);
    }

    @GetMapping("/critiques/all")
    public ResponseEntity<List<SinistreCritique>>
            getSinistresCritiquesAll() {
        List<SinistreCritique> critiques =
                sinistresRepository.findAllByOrderByDateDeclarationDesc();
        return ResponseEntity.ok(critiques);
    }

    @GetMapping("/critiques/contrat/{contratId}")
    public ResponseEntity<List<SinistreCritique>>
            getSinistresCritiquesByContrat(
            @PathVariable String contratId) {
        List<SinistreCritique> critiques =
                sinistresRepository.findByContratId(contratId);
        return ResponseEntity.ok(critiques);
    }

    @GetMapping("/critiques/seuil/{seuil}")
    public ResponseEntity<List<SinistreCritique>>
            getSinistresAboveSeuil(
            @PathVariable double seuil) {
        List<SinistreCritique> critiques =
                sinistresRepository
                        .findByMontantSinistreGreaterThan(seuil);
        return ResponseEntity.ok(critiques);
    }
}
