package test.backspark.socks.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import test.backspark.socks.model.dto.SocksDto;
import test.backspark.socks.service.SocksService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/socks")
@RequiredArgsConstructor
public class SocksController {

    private final SocksService socksService;

    @PostMapping("/income")
    public ResponseEntity<SocksDto> income(@RequestBody @Valid SocksDto socksDto) {
        SocksDto result = socksService.income(socksDto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/outcome")
    public ResponseEntity<SocksDto> outcome(@RequestBody @Valid SocksDto socksDto) {
        SocksDto result = socksService.outcome(socksDto);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocksDto> update(@PathVariable Long id, @RequestBody @Valid SocksDto socksDto) {
        SocksDto result = socksService.update(id, socksDto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<SocksDto>> batchIncome(@RequestParam("file") MultipartFile file) {
        List<SocksDto> result = socksService.batchIncome(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Integer> getSocks(
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) Integer cottonPart,
            @RequestParam(required = false) Integer minCottonPart,
            @RequestParam(required = false) Integer maxCottonPart,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        Integer count = socksService.getSocksAmountByFilter(color, operator, cottonPart, minCottonPart, maxCottonPart, sortBy, sortDirection);
        return ResponseEntity.ok(count);
    }
}
