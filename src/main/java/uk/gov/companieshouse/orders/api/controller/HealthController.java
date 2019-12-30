package uk.gov.companieshouse.orders.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("${uk.gov.companieshouse.orders.api.health}")
    public ResponseEntity<Void> getHealthCheck (){
        return ResponseEntity.status(200).build();
    }
}
