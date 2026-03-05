package com.wargameclub.clubapi.lab;

import com.wargameclub.clubapi.lab.dto.LabTicketPurchaseRequest;
import com.wargameclub.clubapi.lab.dto.LabTicketPurchaseResponse;
import com.wargameclub.clubapi.lab.service.LabTicketPurchaseService;
import com.wargameclub.clubapi.lab.service.ScenarioOverrides;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API лабораторного сценария покупки билета.
 */
@RestController
@RequestMapping("/api/lab/tickets")
public class LabTicketController {

    private final LabTicketPurchaseService ticketPurchaseService;

    public LabTicketController(LabTicketPurchaseService ticketPurchaseService) {
        this.ticketPurchaseService = ticketPurchaseService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<LabTicketPurchaseResponse> purchase(
            @Valid @RequestBody LabTicketPurchaseRequest request,
            @RequestParam(name = "paymentMode", required = false) FaultMode paymentMode,
            @RequestParam(name = "paymentDelayMs", required = false) Long paymentDelayMs,
            @RequestParam(name = "paymentErrorProbability", required = false) Double paymentErrorProbability,
            @RequestParam(name = "paymentFailAttempts", required = false) Integer paymentFailAttempts,
            @RequestParam(name = "notificationMode", required = false) FaultMode notificationMode,
            @RequestParam(name = "notificationDelayMs", required = false) Long notificationDelayMs,
            @RequestParam(name = "notificationErrorProbability", required = false) Double notificationErrorProbability,
            @RequestParam(name = "notificationFailAttempts", required = false) Integer notificationFailAttempts
    ) {
        return processRequest(
                request,
                paymentMode,
                paymentDelayMs,
                paymentErrorProbability,
                paymentFailAttempts,
                notificationMode,
                notificationDelayMs,
                notificationErrorProbability,
                notificationFailAttempts
        );
    }

    @PostMapping("/purchase/limited")
    public ResponseEntity<LabTicketPurchaseResponse> purchaseLimited(
            @Valid @RequestBody LabTicketPurchaseRequest request,
            @RequestParam(name = "paymentMode", required = false) FaultMode paymentMode,
            @RequestParam(name = "paymentDelayMs", required = false) Long paymentDelayMs,
            @RequestParam(name = "paymentErrorProbability", required = false) Double paymentErrorProbability,
            @RequestParam(name = "paymentFailAttempts", required = false) Integer paymentFailAttempts,
            @RequestParam(name = "notificationMode", required = false) FaultMode notificationMode,
            @RequestParam(name = "notificationDelayMs", required = false) Long notificationDelayMs,
            @RequestParam(name = "notificationErrorProbability", required = false) Double notificationErrorProbability,
            @RequestParam(name = "notificationFailAttempts", required = false) Integer notificationFailAttempts
    ) {
        return processRequest(
                request,
                paymentMode,
                paymentDelayMs,
                paymentErrorProbability,
                paymentFailAttempts,
                notificationMode,
                notificationDelayMs,
                notificationErrorProbability,
                notificationFailAttempts
        );
    }

    private ResponseEntity<LabTicketPurchaseResponse> processRequest(
            LabTicketPurchaseRequest request,
            FaultMode paymentMode,
            Long paymentDelayMs,
            Double paymentErrorProbability,
            Integer paymentFailAttempts,
            FaultMode notificationMode,
            Long notificationDelayMs,
            Double notificationErrorProbability,
            Integer notificationFailAttempts
    ) {
        ScenarioOverrides overrides = new ScenarioOverrides(
                paymentMode,
                paymentDelayMs,
                paymentErrorProbability,
                paymentFailAttempts,
                notificationMode,
                notificationDelayMs,
                notificationErrorProbability,
                notificationFailAttempts
        );

        LabTicketPurchaseResponse response = ticketPurchaseService.purchase(request, overrides);
        HttpStatus status = response.ticketBooked() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
}
