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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RequestLogFilterTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Test
    void logsRequestToDatabase() throws Exception {
        int before = requestLogRepository.findAll().size();

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        List<RequestLog> logs = requestLogRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        assertFalse(logs.isEmpty());
        assertTrue(logs.size() >= before + 1);

        RequestLog last = logs.get(0);
        assertEquals("GET", last.getMethod());
        assertEquals("/actuator/health", last.getPath());
        assertEquals(200, last.getStatus());
    }
}
