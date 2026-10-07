package cz.uhk.fim.ppro.ordinace.controller;

import cz.uhk.fim.ppro.ordinace.dto.VykazDto;
import cz.uhk.fim.ppro.ordinace.service.VykazService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vykazy")
@Tag(name = "Výkazy pro pojišťovny", description = "Měsíční agregace výkonů a součtu bodů pro pojišťovny")
public class VykazController {

    private final VykazService vykazService;

    public VykazController(VykazService vykazService) {
        this.vykazService = vykazService;
    }

    @GetMapping("/mesicni")
    @Operation(summary = "Měsíční výkaz provedených výkonů a přiznaných bodů")
    public ResponseEntity<VykazDto.MesicniPrehled> ziskejMesicniVykaz(
            @RequestParam int rok,
            @RequestParam int mesic,
            @RequestParam(required = false) Long lekarId
    ) {
        return ResponseEntity.ok(vykazService.generujMesicniVykaz(rok, mesic, lekarId));
    }
}
