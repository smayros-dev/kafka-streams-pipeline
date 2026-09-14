package com.caa.dammages.api;

import com.caa.dammages.api.controller.StatsContratController;
import com.caa.dammages.api.model.SinistreCritique;
import com.caa.dammages.api.model.StatsContrat;
import com.caa.dammages.api.repository.SinistreCritiqueRepository;
import com.caa.dammages.api.repository.StatsContratRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsContratController.class)
class StatsContratControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsContratRepository statsRepository;

    @MockBean
    private SinistreCritiqueRepository sinistresRepository;

    @Test
    void shouldReturnStatsWhenContratExists() throws Exception {
        StatsContrat stats = new StatsContrat("CTR-001", 25000.0, 3, 1773219700000L, 1773220000000L);
        when(statsRepository.findFirstByContratIdOrderByWindowEndDesc("CTR-001")).thenReturn(stats);

        mockMvc.perform(get("/api/v1/stats/contrat/CTR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contratId").value("CTR-001"))
                .andExpect(jsonPath("$.totalMontant").value(25000.0))
                .andExpect(jsonPath("$.nbSinistres").value(3));
    }

    @Test
    void shouldReturn404WhenContratNotFound() throws Exception {
        when(statsRepository.findFirstByContratIdOrderByWindowEndDesc("UNKNOWN")).thenReturn(null);

        mockMvc.perform(get("/api/v1/stats/contrat/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnSinistresCritiques() throws Exception {
        SinistreCritique s1 = new SinistreCritique("sin-001", "CTR-001", 15000.0, "COLLISION", 1773220000000L, 10000.0);
        when(sinistresRepository.findAllByOrderByDateDeclarationDesc()).thenReturn(Arrays.asList(s1));

        mockMvc.perform(get("/api/v1/stats/critiques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sinistreId").value("sin-001"));
    }
}
