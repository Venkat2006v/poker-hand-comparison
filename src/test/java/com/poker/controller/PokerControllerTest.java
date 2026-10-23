package com.poker.controller;

import com.poker.exception.ApiExceptionHandler;
import com.poker.service.HandEvaluator;
import com.poker.service.PokerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PokerControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PokerController(new PokerService(new HandEvaluator())))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsTheWinnerAndEvaluations() throws Exception {
        mockMvc.perform(post("/api/poker/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hands":[
                                  {"player":"A","cards":["5H","6D","7S","8C","9H"]},
                                  {"player":"B","cards":["KC","KD","KH","2S","3C"]}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winners[0]").value("A"))
                .andExpect(jsonPath("$.results[0].category").value("Straight"));
    }

    @Test
    void acceptsMixedHandSizesAndPicksBestFiveFromSeven() throws Exception {
        mockMvc.perform(post("/api/poker/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hands":[
                                  {"player":"A","cards":["2C","3D","4H","9S","KC"]},
                                  {"player":"B","cards":["2S","3H","4D","9C","KH"]},
                                  {"player":"C","cards":["2D","3S","4C","9H","KD","5C","6D"]}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winners.length()").value(1))
                .andExpect(jsonPath("$.winners[0]").value("C"))
                .andExpect(jsonPath("$.results[2].category").value("Straight"))
                .andExpect(jsonPath("$.results[2].bestFive.length()").value(5));
    }

    @Test
    void returnsMultipleWinnersOnATie() throws Exception {
        mockMvc.perform(post("/api/poker/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hands":[
                                  {"player":"A","cards":["2C","3D","4H","9S","KC"]},
                                  {"player":"B","cards":["2S","3H","4D","9C","KH"]}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winners.length()").value(2))
                .andExpect(jsonPath("$.winners[0]").value("A"))
                .andExpect(jsonPath("$.winners[1]").value("B"));
    }

    @Test
    void rejectsMissingHandsField() throws Exception {
        mockMvc.perform(post("/api/poker/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Request must contain a hands array"));
    }

    @Test
    void rejectsWrongNumberOfHands() throws Exception {
        mockMvc.perform(post("/api/poker/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hands":[{"player":"A","cards":["2C","3D","4H","9S","KC"]}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Provide between 2 and 4 hands"));
    }

    @Test
    void reportsInvalidCardAsBadRequest() throws Exception {
        mockMvc.perform(post("/api/poker/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hands":[
                                  {"player":"A","cards":["1X","6D","7S","8C","9H"]},
                                  {"player":"B","cards":["KC","KD","KH","2S","3C"]}
                                ]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void rejectsInvalidJson() throws Exception {
        mockMvc.perform(post("/api/poker/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
