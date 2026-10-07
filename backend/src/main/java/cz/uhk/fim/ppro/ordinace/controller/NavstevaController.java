package cz.uhk.fim.ppro.ordinace.controller;

import cz.uhk.fim.ppro.ordinace.dto.NavstevaDto;
import cz.uhk.fim.ppro.ordinace.service.NavstevaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/navstevy")
@Tag(name = "Návštěvy a Výkony", description = "Evidence realizovaných návštěv a přiřazování vykázaných výkonů")
public class NavstevaController {

    private final NavstevaService navstevaService;

    public NavstevaController(NavstevaService navstevaService) {
        this.navstevaService = navstevaService;
    }

    @PostMapping
    @Operation(summary = "Zaevidování uskutečněné návštěvy s výkony z číselníku (dokončí rezervaci)")
    public ResponseEntity<NavstevaDto.Response> zaevidujNavstevu(@Valid @RequestBody NavstevaDto.Request req) {
        NavstevaDto.Response res = navstevaService.zaevidujNavstevu(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/pacient/{pacientId}")
    @Operation(summary = "Historie návštěv konkrétního pacienta včetně vykázaných výkonů a zprávy lékaře")
    public ResponseEntity<List<NavstevaDto.Response>> ziskejNavstevyPacienta(@PathVariable Long pacientId) {
        return ResponseEntity.ok(navstevaService.ziskejNavstevyPacienta(pacientId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail návštěvy podle ID")
    public ResponseEntity<NavstevaDto.Response> ziskejPodleId(@PathVariable Long id) {
        return ResponseEntity.ok(navstevaService.ziskejPodleId(id));
    }
}
