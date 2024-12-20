package test.backspark.socks.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import test.backspark.socks.exeptionhandler.InvalidFileFormatException;
import test.backspark.socks.exeptionhandler.NotEnoughSocksException;
import test.backspark.socks.exeptionhandler.SocksNotFoundException;
import test.backspark.socks.model.dto.SocksDto;
import test.backspark.socks.model.entity.Socks;
import test.backspark.socks.model.mapper.SocksMapper;
import test.backspark.socks.repositrory.SocksRepository;
import test.backspark.socks.service.SocksService;
import test.backspark.socks.specification.SocksSpecification;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SocksServiceImpl implements SocksService {
    private static final Logger logger = LoggerFactory.getLogger(SocksServiceImpl.class);
    private final SocksRepository socksRepository;
    private final SocksMapper socksMapper;

    @Override
    @Transactional
    public SocksDto income(SocksDto socksDto) {
        logger.info("Socks income {}", socksDto);

        validateStingAndIntegerValues(socksDto);

        Socks socks = socksRepository.findByColorAndCottonPart(socksDto.getColor(), socksDto.getCottonPart())
                .orElseGet(() -> {
                    Socks newSocks = new Socks();
                    newSocks.setColor(socksDto.getColor().toLowerCase());
                    newSocks.setCottonPart(socksDto.getCottonPart());
                    newSocks.setQuantity(0);
                    return newSocks;
                });

        socks.setQuantity(socks.getQuantity() + socksDto.getQuantity());
        socksRepository.save(socks);

        logger.info("Socks income successfully");
        return socksMapper.mapToDto(socks);
    }

    @Override
    @Transactional
    public SocksDto outcome(SocksDto socksDto) {
        logger.info("Socks outcome {}", socksDto);
        validateStingAndIntegerValues(socksDto);
        Socks socks = socksRepository.findByColorAndCottonPart(socksDto.getColor(), socksDto.getCottonPart())
                .orElseThrow(() -> new SocksNotFoundException("Носки не найдены"));

        if (socks.getQuantity() < socksDto.getQuantity()) {
            logger.error("Socks not enough");
            throw new NotEnoughSocksException("Носков не хватает на складе");
        }

        socks.setQuantity(socks.getQuantity() - socksDto.getQuantity());
        logger.info("Socks outcome successfully");
        return socksMapper.mapToDto(socksRepository.save(socks));
    }

    @Override
    @Transactional
    public SocksDto update(Long id, SocksDto socksDto) {
        logger.info("Update socks with id {}", id);
        validateStingAndIntegerValues(socksDto);
        Socks socks = socksRepository.findById(id)
                .orElseThrow(() -> new SocksNotFoundException("Носки не найдены"));
        socks.setColor(socksDto.getColor().toLowerCase());
        socks.setCottonPart(socksDto.getCottonPart());
        socks.setQuantity(socksDto.getQuantity());
        socksRepository.save(socks);
        logger.info("Socks update successfully");
        return socksMapper.mapToDto(socks);
    }

    @Override
    @Transactional
    public List<SocksDto> batchIncome(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        logger.info("Starting to process CSV file: {}", originalFilename);

        if (originalFilename == null || file.isEmpty() || !originalFilename.endsWith(".csv")) {
            throw new InvalidFileFormatException("Файл должен быть CSV и не пустой.");
        }

        List<Socks> processedEntities = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                throw new InvalidFileFormatException("Файл пустой или не содержит заголовок.");
            }

            String line;
            int rowNum = 1;

            while ((line = reader.readLine()) != null) {
                rowNum++;
                try {
                    SocksDto dto = parseCSVLineToDto(line, rowNum);
                    validateSocks(dto.getColor(), dto.getCottonPart(), dto.getQuantity(), rowNum);

                    Socks socks = socksRepository.findByColorAndCottonPart(dto.getColor().toLowerCase(), dto.getCottonPart())
                            .map(existingSocks -> {
                                existingSocks.setQuantity(existingSocks.getQuantity() + dto.getQuantity());
                                return existingSocks;
                            })
                            .orElseGet(() -> {
                                Socks newSocks = new Socks();
                                newSocks.setColor(dto.getColor().trim());
                                newSocks.setCottonPart(dto.getCottonPart());
                                newSocks.setQuantity(dto.getQuantity());
                                return newSocks;
                            });

                    socksRepository.save(socks);
                    processedEntities.add(socks);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid data at row {}: {}", rowNum, e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Error reading CSV file: {}", originalFilename, e);
            throw new InvalidFileFormatException("Ошибка при чтении файла: " + e.getMessage());
        }

        List<SocksDto> processedSocks = processedEntities.stream()
                .map(socksMapper::mapToDto)
                .toList();

        logger.info("Finished processing CSV file: {}. Total valid records: {}", originalFilename, processedSocks.size());
        return processedSocks;
    }


    private SocksDto parseCSVLineToDto(String line, int rowNum) {
        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Некорректный формат: ожидается 3 столбца, строка " + rowNum);
        }

        String color = parts[0].trim().toLowerCase();
        int cottonPart;
        int quantity;

        try {
            cottonPart = Integer.parseInt(parts[1].trim());
            quantity = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Ошибка преобразования числового значения в строке " + rowNum + ": " + e.getMessage(), e);
        }

        return new SocksDto(null, color, cottonPart, quantity);
    }

    private void validateSocks(String color, int cottonPart, int quantity, int rowNum) {
        if (color == null || color.isEmpty()) {
            throw new IllegalArgumentException("Пустой цвет в строке " + rowNum);
        }
        if (cottonPart < 0 || cottonPart > 100) {
            throw new IllegalArgumentException("Процент хлопка должен быть в диапазоне [0, 100] в строке " + rowNum);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0 в строке " + rowNum);
        }
    }


    @Override
    public Integer getSocksAmountByFilter(
            String color,
            String operator,
            Integer cottonPart,
            Integer minCottonPart,
            Integer maxCottonPart,
            String sortBy,
            String sortDirection) {

        Specification<Socks> spec = Specification
                .where(SocksSpecification.hasColor(color != null ? color.trim().toLowerCase() : null))
                .and(SocksSpecification.hasCottonPart(operator, cottonPart))
                .and(SocksSpecification.cottonPartBetween(minCottonPart, maxCottonPart));

        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            sort = "desc".equalsIgnoreCase(sortDirection)
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
        }

        return socksRepository.findAll(spec, sort).stream()
                .mapToInt(Socks::getQuantity)
                .sum();
    }

    private void validateStingAndIntegerValues(SocksDto socksDto) {
        if (socksDto.getColor() == null || socksDto.getColor().isEmpty()) {
            socksDto.setColor("");
        }
        if (socksDto.getCottonPart() == null) {
            socksDto.setCottonPart(0);
        }
        if (socksDto.getQuantity() == null) {
            socksDto.setQuantity(0);
        }
    }

}
