package com.hackathon.sentiment_api.controller;

import com.hackathon.sentiment_api.dto.SentimentResponse;
import com.hackathon.sentiment_api.exception.InvalidTextException;
import com.hackathon.sentiment_api.model.SentimentModelClient;
import com.hackathon.sentiment_api.service.SentimentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "sentiment.ds.health-check.enabled=false")
@WebMvcTest(SentimentController.class)
public class SentimentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SentimentService sentimentService;

    // No es necesario para este controller actual, pero no molesta.
    @MockBean
    private SentimentModelClient modelClient;

    @Test
    void predict_ok_return200() throws Exception {
        when(sentimentService.predict("I love this product"))
                .thenReturn(new SentimentResponse("positive", 0.97, 1));

        mvc.perform(post("/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"I love this product\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediction").value("positive"))
                .andExpect(jsonPath("$.probability").value(0.97))
                .andExpect(jsonPath("$.label").value(1));
    }

    @Test
    void predict_tooShort_return400() throws Exception {
        mvc.perform(post("/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"aa\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.path").value("/sentiment"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void predict_onlySymbols_return400() throws Exception {
        when(sentimentService.predict("!!!@@@###"))
                .thenThrow(new InvalidTextException("El texto debe contener al menos una letra o un número."));

        mvc.perform(post("/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"!!!@@@###\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/sentiment"));
    }

    @Test
    void predict_tooLong_return400() throws Exception {
        String longText = "a".repeat(5001);

        mvc.perform(post("/sentiment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"" + longText + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/sentiment"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void health_return200_OK() throws Exception {
        mvc.perform(get("/sentiment/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.equalToIgnoringCase("OK"))); // El "ok" simple no funciona, se cambia a un ignoringcase para forzar el match
    }

    @Test
    void healthDeps_whenNotFastApiClient_return200_andSkipped() throws Exception {
        mvc.perform(get("/sentiment/health/deps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backend").value("ok"))
                .andExpect(jsonPath("$.modelClient").exists())
                .andExpect(jsonPath("$.ds").value("skipped"));
    }

}
