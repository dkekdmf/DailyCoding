package com.example.zero.healthcare.journal.controller;

import com.example.zero.healthcare.Entity.User;
import com.example.zero.healthcare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JournalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("ctrl-" + UUID.randomUUID() + "@test.com", "token", "tester"));
        userId = user.getId();
    }

    @Test
    @DisplayName("유효한 요청으로 일지 생성 시 201과 journalId를 반환한다")
    void createJournal_validRequest_returns201() throws Exception {
        String body = """
                {
                  "userId": %d,
                  "folderId": null,
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": [
                    {"bodyPart": "SHOULDER", "side": "LEFT", "painLevel": 7}
                  ]
                }""".formatted(userId);

        mockMvc.perform(post("/api/journals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.journalId").exists());
    }

    @Test
    @DisplayName("painRecords가 없어도 일지 생성이 성공한다")
    void createJournal_emptyPainRecords_returns201() throws Exception {
        String body = """
                {
                  "userId": %d,
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""".formatted(userId);

        mockMvc.perform(post("/api/journals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("preCondition 값이 범위 초과이면 400을 반환한다")
    void createJournal_outOfRangeCondition_returns400() throws Exception {
        String body = """
                {
                  "userId": %d,
                  "preCondition": {
                    "jointMusclePain": 11, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""".formatted(userId);

        mockMvc.perform(post("/api/journals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("중복 bodyPart+side가 있으면 400을 반환한다")
    void createJournal_duplicatePainRecord_returns400() throws Exception {
        String body = """
                {
                  "userId": %d,
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": [
                    {"bodyPart": "KNEE", "side": "LEFT", "painLevel": 5},
                    {"bodyPart": "KNEE", "side": "LEFT", "painLevel": 3}
                  ]
                }""".formatted(userId);

        mockMvc.perform(post("/api/journals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("존재하지 않는 userId이면 404를 반환한다")
    void createJournal_unknownUser_returns404() throws Exception {
        String body = """
                {
                  "userId": 99999,
                  "preCondition": {
                    "jointMusclePain": 5, "sleepHours": 7, "sleepQuality": 6,
                    "previousFatigue": 4, "overallCondition": 8
                  },
                  "painRecords": []
                }""";

        mockMvc.perform(post("/api/journals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}
