package cz.uhk.fim.ppro.ordinace.controller;

import cz.uhk.fim.ppro.ordinace.dto.SlotDto;
import cz.uhk.fim.ppro.ordinace.service.OrdinacniDobaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sloty")
@Tag(name = "Sloty a Diář", description = "Generování 15minutové mřížky volných a obsazených slotů")
public class SlotController {

    private final OrdinacniDobaService ordinacniDobaService;

    public SlotController(OrdinacniDobaService ordinacniDobaService) {
        this.ordinacniDobaService = ordinacniDobaService;
    }

    @GetMapping
    @Operation(summary = "Získání 15minutové mřížky slotů pro lékaře a konkrétní datum")
    public ResponseEntity<List<SlotDto>> ziskejSlotyProDen(
            @RequestParam Long lekarId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datum
    ) {
        return ResponseEntity.ok(ordinacniDobaService.vygenerujSlotyProDen(lekarId, datum));
    }
}
