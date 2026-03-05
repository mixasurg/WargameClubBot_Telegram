package com.wargameclub.clubapi.lab.service;

/**
 * Полный набор настроек эмуляции для покупки билета.
 */
public record PurchaseScenarioOptions(
        ServiceFaultSettings payment,
        ServiceFaultSettings notification
) {
}
