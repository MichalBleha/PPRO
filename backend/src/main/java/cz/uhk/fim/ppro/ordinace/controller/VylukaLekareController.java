package cz.uhk.fim.ppro.ordinace.controller;

import cz.uhk.fim.ppro.ordinace.dto.VylukaLekareDto;
import cz.uhk.fim.ppro.ordinace.service.OrdinacniDobaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vyluky")
@Tag(name = "Výluky lékařů", description = "Zadávání nepřítomností lékařů a správa přeobjednávání")
public class VylukaLekareController {

    private final OrdinacniDobaService ordinacniDobaService;

    public VylukaLekareController(OrdinacniDobaService ordinacniDobaService) {
        this.ordinacniDobaService = ordinacniDobaService;
    }

    @PostMapping
    @Operation(summary = "Zadání nepřítomnosti/nemoci lékaře (automaticky přesune dotčené rezervace do fronty k přeobjednání)")
    public ResponseEntity<VylukaLekareDto.Response> zaznamenejVyluku(@Valid @RequestBody VylukaLekareDto.Request req) {
        VylukaLekareDto.Response res = ordinacniDobaService.zaznamenejVyluku(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
