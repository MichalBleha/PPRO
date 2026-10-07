package cz.uhk.fim.ppro.ordinace.controller;

import cz.uhk.fim.ppro.ordinace.dto.LekarDto;
import cz.uhk.fim.ppro.ordinace.service.LekarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lekari")
@Tag(name = "Lékaři", description = "Přehled lékařů a jejich ordinačních hodin")
public class LekarController {

    private final LekarService lekarService;

    public LekarController(LekarService lekarService) {
        this.lekarService = lekarService;
    }

    @GetMapping
    @Operation(summary = "Seznam všech aktivních lékařů ordinace")
    public ResponseEntity<List<LekarDto.Response>> ziskejAktivniLekare() {
        return ResponseEntity.ok(lekarService.ziskejAktivniLekare());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail lékaře včetně rozvrhu ordinačních hodin")
    public ResponseEntity<LekarDto.Response> ziskejPodleId(@PathVariable Long id) {
        return ResponseEntity.ok(lekarService.ziskejPodleId(id));
    }
}
