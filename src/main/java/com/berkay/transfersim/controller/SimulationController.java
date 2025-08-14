package com.berkay.transfersim.controller;

import com.berkay.transfersim.service.SimulationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sim")
@CrossOrigin
public class SimulationController {
    private final SimulationService sim;

    public SimulationController(SimulationService sim) {
        this.sim = sim;
    }

    @PostMapping("/tick")
    public String tick() {
        return sim.tick();
    }
}
