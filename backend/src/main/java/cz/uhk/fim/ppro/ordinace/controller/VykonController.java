package cz.uhk.fim.ppro.ordinace.controller;

import cz.uhk.fim.ppro.ordinace.dto.VykonDto;
import cz.uhk.fim.ppro.ordinace.service.VykonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vykony")
@Tag(name = "Číselník výkonů", description = "Správa kódů a bodového ohodnocení lékařských výkonů")
public class VykonController {

    private final VykonService vykonService;

    public VykonController(VykonService vykonService) {
        this.vykonService = vykonService;
    }

    @GetMapping
    @Operation(summary = "Seznam všech dostupných lékařských výkonů")
    public ResponseEntity<List<VykonDto.Response>> ziskejVsechnyVykony() {
        return ResponseEntity.ok(vykonService.ziskejVsechnyVykony());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail výkonu podle ID")
    public ResponseEntity<VykonDto.Response> ziskejPodleId(@PathVariable Long id) {
        return ResponseEntity.ok(vykonService.ziskejPodleId(id));
    }

    @PostMapping
    @Operation(summary = "Přidání nového výkonu do číselníku")
    public ResponseEntity<VykonDto.Response> vytvorVykon(@Valid @RequestBody VykonDto.Request req) {
        VykonDto.Response novy = vykonService.vytvorVykon(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(novy);
    }
}
