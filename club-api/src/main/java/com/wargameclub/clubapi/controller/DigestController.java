package com.wargameclub.clubapi.controller;

import com.wargameclub.clubapi.dto.WeekDigestDto;
import com.wargameclub.clubapi.service.DigestService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/digest")
public class DigestController {
    private final DigestService digestService;

    public DigestController(DigestService digestService) {
        this.digestService = digestService;
    }

    @GetMapping("/week")
    public WeekDigestDto week(@RequestParam(name = "offset", defaultValue = "0") @Min(0) @Max(1) int offset) {
        return digestService.getWeekDigest(offset);
    }
}

