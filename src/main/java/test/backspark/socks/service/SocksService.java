package test.backspark.socks.service;

import org.springframework.web.multipart.MultipartFile;
import test.backspark.socks.model.dto.SocksDto;

import java.util.List;

public interface SocksService {
    SocksDto income(SocksDto socksDto);
    SocksDto outcome(SocksDto socksDto);
    SocksDto update(Long id,SocksDto socksDto);
    List<SocksDto> batchIncome(MultipartFile file);
    Integer getSocksAmountByFilter(
        String color,
        String operator,
        Integer cottonPart,
        Integer minCottonPart,
        Integer maxCottonPart,
        String sortBy,
        String sortDirection);
}
