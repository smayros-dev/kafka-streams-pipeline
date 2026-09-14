package com.caa.dammages.api.acceptance;

import com.caa.dammages.api.model.SinistreCritique;
import com.caa.dammages.api.model.StatsContrat;
import com.caa.dammages.api.repository.SinistreCritiqueRepository;
import com.caa.dammages.api.repository.StatsContratRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class ApiAcceptanceTest {

    @Container
    static MongoDBContainer mongoDB = new MongoDBContainer("mongo:6.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDB::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "test_db");
    }

    @Autowired
    private StatsContratRepository statsRepository;

    @Autowired
    private SinistreCritiqueRepository sinistresRepository;

    @BeforeAll
    static void setUp() {
        mongoDB.start();
    }

    @AfterAll
    static void tearDown() {
        mongoDB.stop();
    }

    @Test
    void shouldSaveAndRetrieveStatsContrat() {
        StatsContrat stats = new StatsContrat("CTR-001", 25000.0, 3, 1773219700000L, 1773220000000L);
        statsRepository.save(stats);

        StatsContrat retrieved = statsRepository.findByContratId("CTR-001");

        assertNotNull(retrieved);
        assertEquals("CTR-001", retrieved.getContratId());
        assertEquals(25000.0, retrieved.getTotalMontant());
        assertEquals(3, retrieved.getNbSinistres());
    }

    @Test
    void shouldSaveAndRetrieveSinistresCritiques() {
        SinistreCritique s1 = new SinistreCritique("sin-001", "CTR-001", 15000.0, "COLLISION", 1773220000000L, 10000.0);
        SinistreCritique s2 = new SinistreCritique("sin-002", "CTR-002", 12000.0, "VOL", 1773220100000L, 10000.0);
        sinistresRepository.saveAll(Arrays.asList(s1, s2));

        List<SinistreCritique> all = sinistresRepository.findAllByOrderByDateDeclarationDesc();

        assertFalse(all.isEmpty());
        assertTrue(all.size() >= 2);
    }

    @Test
    void shouldReturnEmptyWhenContratNotFound() {
        StatsContrat retrieved = statsRepository.findByContratId("UNKNOWN");

        assertNull(retrieved);
    }

    @Test
    void shouldSaveMultipleStatsForSameContrat() {
        StatsContrat stats1 = new StatsContrat("CTR-003", 10000.0, 2, 1773219700000L, 1773220000000L);
        StatsContrat stats2 = new StatsContrat("CTR-003", 15000.0, 3, 1773220000000L, 1773220300000L);
        statsRepository.save(stats1);
        statsRepository.save(stats2);

        List<StatsContrat> all = statsRepository.findAll();

        assertFalse(all.isEmpty());
    }

    @Test
    void shouldDeleteAllData() {
        statsRepository.deleteAll();
        sinistresRepository.deleteAll();

        List<StatsContrat> stats = statsRepository.findAll();
        List<SinistreCritique> sinistres = sinistresRepository.findAllByOrderByDateDeclarationDesc();

        assertTrue(stats.isEmpty());
        assertTrue(sinistres.isEmpty());
    }
}
