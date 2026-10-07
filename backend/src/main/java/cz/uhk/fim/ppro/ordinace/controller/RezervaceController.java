package cz.uhk.fim.ppro.ordinace.controller;

import cz.uhk.fim.ppro.ordinace.dto.RezervaceDto;
import cz.uhk.fim.ppro.ordinace.service.RezervaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rezervace")
@Tag(name = "Rezervace", description = "Vytváření, správa a stornování rezervací termínů")
public class RezervaceController {

    private final RezervaceService rezervaceService;

    public RezervaceController(RezervaceService rezervaceService) {
        this.rezervaceService = rezervaceService;
    }

    @PostMapping
    @Operation(summary = "Vytvoření nové rezervace termínu (s kontrolou 15m mřížky, ordinační doby a nekoliznosti)")
    public ResponseEntity<RezervaceDto.Response> vytvorRezervaci(@Valid @RequestBody RezervaceDto.Request req) {
        RezervaceDto.Response nova = rezervaceService.vytvorRezervaci(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(nova);
    }

    @GetMapping
    @Operation(summary = "Získání aktivních rezervací pro daného lékaře v časovém intervalu")
    public ResponseEntity<List<RezervaceDto.Response>> ziskejRezervaceLekare(
            @RequestParam Long lekarId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime od,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime doCas
    ) {
        return ResponseEntity.ok(rezervaceService.ziskejRezervaceLekareVObdobi(lekarId, od, doCas));
    }

    @GetMapping("/pacient/{pacientId}")
    @Operation(summary = "Získání historie rezervací daného pacienta (včetně zrušených)")
    public ResponseEntity<List<RezervaceDto.Response>> ziskejHistoriiPacienta(@PathVariable Long pacientId) {
        return ResponseEntity.ok(rezervaceService.ziskejHistoriiPacienta(pacientId));
    }

    @GetMapping("/fronta-preobjednani")
    @Operation(summary = "Fronta pacientů k přeobjednání (např. po zaznamenání nemoci/výluky lékaře)")
    public ResponseEntity<List<RezervaceDto.Response>> ziskejFrontuKPreobjednani() {
        return ResponseEntity.ok(rezervaceService.ziskejFrontuKPreobjednani());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail rezervace podle ID")
    public ResponseEntity<RezervaceDto.Response> ziskejPodleId(@PathVariable Long id) {
        return ResponseEntity.ok(rezervaceService.ziskejPodleId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Stornování rezervace (uvolní slot v mřížce, zachová auditní záznam)")
    public ResponseEntity<RezervaceDto.Response> stornujRezervaci(@PathVariable Long id) {
        return ResponseEntity.ok(rezervaceService.stornujRezervaci(id));
    }

    public record PreobjednaniRequest(
            Long novyLekarId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime novyCasOd,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime novyCasDo
    ) {}

    @PostMapping("/{id}/preobjednat")
    @Operation(summary = "Přeobjednání pacienta na náhradní termín / lékaře")
    public ResponseEntity<RezervaceDto.Response> preobjednej(
            @PathVariable Long id,
            @RequestBody PreobjednaniRequest req
    ) {
        return ResponseEntity.ok(rezervaceService.preobjednejRezervaci(id, req.novyLekarId(), req.novyCasOd(), req.novyCasDo()));
    }
}
