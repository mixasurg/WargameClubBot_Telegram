package com.wargameclub.clubapi;

import java.util.List;
import com.wargameclub.clubapi.entity.RequestLog;
import com.wargameclub.clubapi.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты фильтра логирования ошибочных HTTP-запросов.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RequestLogFilterTest {

    /**
     * MockMvc для выполнения HTTP-запросов.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Репозиторий логов запросов.
     */
    @Autowired
    private RequestLogRepository requestLogRepository;

    /**
     * Проверяет, что успешный HTTP-запрос не записывается в базу логов.
     */
    @Test
    void doesNotLogSuccessfulRequest() throws Exception {
        int before = requestLogRepository.findAll().size();

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        int after = requestLogRepository.findAll().size();
        assertEquals(before, after);
    }

    /**
     * Проверяет, что ошибочный HTTP-запрос записывается в базу логов.
     */
    @Test
    void logsErrorRequestToDatabase() throws Exception {
        int before = requestLogRepository.findAll().size();

        mockMvc.perform(get("/actuator/does-not-exist"))
                .andExpect(status().isInternalServerError());

        List<RequestLog> logs = requestLogRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        assertFalse(logs.isEmpty());
        assertEquals(before + 1, logs.size());

        RequestLog last = logs.get(0);
        assertEquals("GET", last.getMethod());
        assertEquals("/actuator/does-not-exist", last.getPath());
        assertEquals(500, last.getStatus());
    }
}
