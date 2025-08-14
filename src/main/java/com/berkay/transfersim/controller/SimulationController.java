package com.berkay.transfersim.controller;

import com.berkay.transfersim.service.SimulationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/simulation")
@CrossOrigin
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /** Handmatig een tick draaien: POST /simulation/tick */
    @PostMapping("/tick")
    public String runTick() {
        return simulationService.tick();
    }
}
