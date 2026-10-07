package cz.uhk.fim.ppro.ordinace.controller;

import cz.uhk.fim.ppro.ordinace.dto.PacientDto;
import cz.uhk.fim.ppro.ordinace.service.PacientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacienti")
@Tag(name = "Pacienti", description = "Správa kartotéky a vyhledávání pacientů")
public class PacientController {

    private final PacientService pacientService;

    public PacientController(PacientService pacientService) {
        this.pacientService = pacientService;
    }

    @GetMapping
    @Operation(summary = "Vyhledání pacientů podle příjmení, telefonu nebo čísla pojištěnce")
    public ResponseEntity<List<PacientDto.Response>> vyhledejPacienty(
            @RequestParam(required = false) String dotaz
    ) {
        return ResponseEntity.ok(pacientService.vyhledejPacienty(dotaz));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Získání detailu pacienta podle ID")
    public ResponseEntity<PacientDto.Response> ziskejPodleId(@PathVariable Long id) {
        return ResponseEntity.ok(pacientService.ziskejPodleId(id));
    }

    @PostMapping
    @Operation(summary = "Registrace nového pacienta do kartotéky")
    public ResponseEntity<PacientDto.Response> vytvorPacienta(@Valid @RequestBody PacientDto.Request req) {
        PacientDto.Response novy = pacientService.vytvorPacienta(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(novy);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizace údajů existujícího pacienta")
    public ResponseEntity<PacientDto.Response> upravPacienta(
            @PathVariable Long id,
            @Valid @RequestBody PacientDto.Request req
    ) {
        return ResponseEntity.ok(pacientService.upravPacienta(id, req));
    }
}
