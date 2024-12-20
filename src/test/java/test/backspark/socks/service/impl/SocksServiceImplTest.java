package test.backspark.socks.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import test.backspark.socks.exeptionhandler.InvalidFileFormatException;
import test.backspark.socks.exeptionhandler.NotEnoughSocksException;
import test.backspark.socks.exeptionhandler.SocksNotFoundException;
import test.backspark.socks.model.dto.SocksDto;
import test.backspark.socks.model.entity.Socks;
import test.backspark.socks.model.mapper.SocksMapper;
import test.backspark.socks.repositrory.SocksRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocksServiceImplTest {
    @Mock
    private SocksRepository socksRepository;

    @Mock
    private SocksMapper socksMapper;

    @InjectMocks
    private SocksServiceImpl socksService;

    @BeforeEach
    void setUp() {
        Mockito.reset(socksRepository, socksMapper);
    }

    @Nested
    @DisplayName("Method income")
    class IncomeTests {
        @ParameterizedTest(name = "income: {0}, cottonPart={1}, quantity={2}")
        @CsvSource({
                "red, 30, 10",
                "blue, 50, 5",
                "green, 0, 1",
                "yellow, 100, 100"
        })
        void testIncomeWithDifferentParams(String color, Integer cottonPart, Integer quantity) {
            SocksDto inputDto = new SocksDto(null, color, cottonPart, quantity);
            Socks existingSocks = new Socks();
            existingSocks.setColor(color.toLowerCase());
            existingSocks.setCottonPart(cottonPart);
            existingSocks.setQuantity(20);

            when(socksRepository.findByColorAndCottonPart(color.toLowerCase(), cottonPart))
                    .thenReturn(Optional.of(existingSocks));

            Socks updatedSocks = new Socks();
            updatedSocks.setColor(color.toLowerCase());
            updatedSocks.setCottonPart(cottonPart);
            updatedSocks.setQuantity(20 + quantity);

            when(socksRepository.save(any(Socks.class))).thenReturn(updatedSocks);

            SocksDto expectedDto = new SocksDto(1L, color.toLowerCase(), cottonPart, 20 + quantity);
            when(socksMapper.mapToDto(any(Socks.class))).thenReturn(expectedDto);

            SocksDto resultDto = socksService.income(inputDto);

            assertEquals(color.toLowerCase(), resultDto.getColor());
            assertEquals(cottonPart, resultDto.getCottonPart());
            assertEquals(20 + quantity, resultDto.getQuantity());

            verify(socksRepository).findByColorAndCottonPart(color.toLowerCase(), cottonPart);
            verify(socksRepository).save(any(Socks.class));
            verify(socksMapper).mapToDto(any(Socks.class));
        }

        @ParameterizedTest(name = "income new socks: {0}, cottonPart={1}, quantity={2}")
        @CsvSource({
                "black, 10, 5",
                "white, 99, 50"
        })
        void testIncomeWhenSocksNotFound(String color, Integer cottonPart, Integer quantity) {
            SocksDto inputDto = new SocksDto(null, color, cottonPart, quantity);

            when(socksRepository.findByColorAndCottonPart(color.toLowerCase(), cottonPart))
                    .thenReturn(Optional.empty());

            when(socksRepository.save(any(Socks.class))).thenAnswer(invocation -> {
                Socks arg = invocation.getArgument(0, Socks.class);
                arg.setId(2L);
                return arg;
            });

            SocksDto expectedDto = new SocksDto(2L, color.toLowerCase(), cottonPart, quantity);
            when(socksMapper.mapToDto(any(Socks.class))).thenReturn(expectedDto);

            SocksDto resultDto = socksService.income(inputDto);

            assertEquals(color.toLowerCase(), resultDto.getColor());
            assertEquals(cottonPart, resultDto.getCottonPart());
            assertEquals(quantity, resultDto.getQuantity());

            verify(socksRepository).findByColorAndCottonPart(color.toLowerCase(), cottonPart);
            verify(socksRepository).save(any(Socks.class));
            verify(socksMapper).mapToDto(any(Socks.class));
        }
    }

    @Nested
    @DisplayName("Method outcome")
    class OutcomeTests {
        @ParameterizedTest(name = "outcome: {0}, cottonPart={1}, starting={2}, outcome={3}")
        @CsvSource({
                "red, 30, 20, 10",
                "blue, 50, 10, 5",
                "green, 0, 1, 1"
        })
        void testOutcome(String color, Integer cottonPart, Integer startQuantity, Integer outcomeQty) {
            SocksDto inputDto = new SocksDto(null, color, cottonPart, outcomeQty);
            Socks existing = new Socks();
            existing.setColor(color.toLowerCase());
            existing.setCottonPart(cottonPart);
            existing.setQuantity(startQuantity);

            when(socksRepository.findByColorAndCottonPart(color.toLowerCase(), cottonPart))
                    .thenReturn(Optional.of(existing));

            Socks afterOutcome = new Socks();
            afterOutcome.setColor(color.toLowerCase());
            afterOutcome.setCottonPart(cottonPart);
            afterOutcome.setQuantity(startQuantity - outcomeQty);

            when(socksRepository.save(any(Socks.class))).thenReturn(afterOutcome);

            SocksDto expectedDto = new SocksDto(3L, color.toLowerCase(), cottonPart, startQuantity - outcomeQty);
            when(socksMapper.mapToDto(any(Socks.class))).thenReturn(expectedDto);

            SocksDto result = socksService.outcome(inputDto);

            assertEquals(startQuantity - outcomeQty, result.getQuantity());

            verify(socksRepository).findByColorAndCottonPart(color.toLowerCase(), cottonPart);
            verify(socksRepository).save(any(Socks.class));
            verify(socksMapper).mapToDto(any(Socks.class));
        }

        @ParameterizedTest(name = "outcome not enough: {0}, cottonPart={1}, starting={2}, outcome={3}")
        @CsvSource({
                "red, 30, 5, 10",
                "blue, 50, 0, 1"
        })
        void testOutcomeNotEnough(String color, Integer cottonPart, Integer startQuantity, Integer outcomeQty) {
            SocksDto inputDto = new SocksDto(null, color, cottonPart, outcomeQty);
            Socks existing = new Socks();
            existing.setColor(color.toLowerCase());
            existing.setCottonPart(cottonPart);
            existing.setQuantity(startQuantity);

            when(socksRepository.findByColorAndCottonPart(color.toLowerCase(), cottonPart))
                    .thenReturn(Optional.of(existing));

            assertThrows(NotEnoughSocksException.class, () -> socksService.outcome(inputDto));

            verify(socksRepository).findByColorAndCottonPart(color.toLowerCase(), cottonPart);
            verify(socksRepository, never()).save(any(Socks.class));
            verify(socksMapper, never()).mapToDto(any(Socks.class));
        }

        @ParameterizedTest(name = "outcome not found: {0}, cottonPart={1}, outcome={2}")
        @CsvSource({
                "purple, 33, 10",
                "pink, 10, 1"
        })
        void testOutcomeSocksNotFound(String color, Integer cottonPart, Integer outcomeQty) {
            SocksDto inputDto = new SocksDto(null, color, cottonPart, outcomeQty);

            when(socksRepository.findByColorAndCottonPart(color.toLowerCase(), cottonPart))
                    .thenReturn(Optional.empty());

            assertThrows(SocksNotFoundException.class, () -> socksService.outcome(inputDto));

            verify(socksRepository).findByColorAndCottonPart(color.toLowerCase(), cottonPart);
            verify(socksRepository, never()).save(any(Socks.class));
            verify(socksMapper, never()).mapToDto(any(Socks.class));
        }
    }

    @Nested
    @DisplayName("Method update")
    class UpdateTests {
        @ParameterizedTest(name = "update: id={0}, color={1}, cottonPart={2}, quantity={3}")
        @CsvSource({
                "1, red, 30, 100",
                "2, blue, 50, 10"
        })
        void testUpdate(Long id, String color, Integer cottonPart, Integer quantity) {
            SocksDto inputDto = new SocksDto(null, color, cottonPart, quantity);
            Socks existing = new Socks();
            existing.setId(id);
            existing.setColor("oldColor");
            existing.setCottonPart(10);
            existing.setQuantity(5);

            when(socksRepository.findById(id)).thenReturn(Optional.of(existing));

            Socks afterUpdate = new Socks();
            afterUpdate.setId(id);
            afterUpdate.setColor(color.toLowerCase());
            afterUpdate.setCottonPart(cottonPart);
            afterUpdate.setQuantity(quantity);

            when(socksRepository.save(any(Socks.class))).thenReturn(afterUpdate);

            SocksDto expectedDto = new SocksDto(id, color.toLowerCase(), cottonPart, quantity);
            when(socksMapper.mapToDto(any(Socks.class))).thenReturn(expectedDto);

            SocksDto result = socksService.update(id, inputDto);

            assertEquals(color.toLowerCase(), result.getColor());
            assertEquals(cottonPart, result.getCottonPart());
            assertEquals(quantity, result.getQuantity());

            verify(socksRepository).findById(id);
            verify(socksRepository).save(any(Socks.class));
            verify(socksMapper).mapToDto(any(Socks.class));
        }

        @ParameterizedTest(name = "update not found: id={0}, color={1}, cottonPart={2}, quantity={3}")
        @CsvSource({
                "10, black, 40, 50",
                "11, pink, 20, 20"
        })
        void testUpdateNotFound(Long id, String color, Integer cottonPart, Integer quantity) {
            SocksDto inputDto = new SocksDto(null, color, cottonPart, quantity);

            when(socksRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(SocksNotFoundException.class, () -> socksService.update(id, inputDto));

            verify(socksRepository).findById(id);
            verify(socksRepository, never()).save(any(Socks.class));
            verify(socksMapper, never()).mapToDto(any(Socks.class));
        }
    }

    @Nested
    @DisplayName("Method batchIncome")
    class BatchIncomeTests {
        static Stream<Arguments> batchIncomeArguments() {
            return Stream.of(
                    Arguments.of("valid.csv", "color,cottonPart,quantity\nred,30,10\nblue,50,20", 2),
                    Arguments.of("partial.csv", "color,cottonPart,quantity\nred,30,10\n,,\ngreen,20,5", 2) // одна строка некорректная, но две валидны
            );
        }

        @ParameterizedTest(name = "batchIncome with file {0}")
        @MethodSource("batchIncomeArguments")
        void testBatchIncome(String fileName, String fileContent, int expectedCount) throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", fileName,
                    "text/csv", fileContent.getBytes());

            when(socksRepository.save(any(Socks.class))).thenAnswer(invocation -> {
                Socks arg = invocation.getArgument(0, Socks.class);
                if (arg.getId() == null) {
                    arg.setId(new Random().nextLong());
                }
                return arg;
            });

            when(socksMapper.mapToDto(any(Socks.class))).thenAnswer(inv -> {
                Socks s = inv.getArgument(0, Socks.class);
                return new SocksDto(s.getId(), s.getColor(), s.getCottonPart(), s.getQuantity());
            });

            List<SocksDto> result = socksService.batchIncome(file);
            assertEquals(expectedCount, result.size());

            verify(socksRepository, atLeast(expectedCount)).save(any(Socks.class));
        }

        @ParameterizedTest(name = "batchIncome invalid file: {0}")
        @ValueSource(strings = {"", "not_csv.txt"})
        void testBatchIncomeInvalidFile(String fileName) {
            MockMultipartFile file = new MockMultipartFile("file", fileName,
                    "text/plain", new byte[0]);

            assertThrows(InvalidFileFormatException.class, () -> socksService.batchIncome(file));
            verifyNoInteractions(socksRepository);
        }
    }

    @Nested
    @DisplayName("Method getSocksAmountByFilter")
    class GetSocksAmountByFilterTests {
        @ParameterizedTest(name = "getSocksAmountByFilter: color={0}, operator={1}, cottonPart={2}, min={3}, max={4}, sortBy={5}, sortDir={6}")
        @CsvSource({
                "red, eq, 30, , , , ",
                "blue, moreThan, 50, , , quantity, asc",
                "green, lessThan, 10, , , cottonPart, desc",
                "yellow, , , 20, 40, , "
        })
        void testGetSocksAmountByFilter(String color, String operator, Integer cottonPart, Integer minCottonPart, Integer maxCottonPart, String sortBy, String sortDirection) {
            List<Socks> socksList = new ArrayList<>();
            Socks s1 = new Socks();
            s1.setQuantity(10);
            Socks s2 = new Socks();
            s2.setQuantity(5);
            Socks s3 = new Socks();
            s3.setQuantity(7);
            socksList.add(s1);
            socksList.add(s2);
            socksList.add(s3);

            when(socksRepository.findAll(
                    ArgumentMatchers.<Specification<Socks>>any(),
                    ArgumentMatchers.<Sort>any()
            )).thenReturn(socksList);
            Integer result = socksService.getSocksAmountByFilter(color, operator, cottonPart, minCottonPart, maxCottonPart, sortBy, sortDirection);

            assertEquals(10 + 5 + 7, result);
            verify(socksRepository).findAll(any(Specification.class), any(Sort.class));
        }

        @ParameterizedTest(name = "getSocksAmountByFilter with empty result: color={0}")
        @ValueSource(strings = {"red", "blue"})
        void testGetSocksAmountByFilterEmpty(String color) {
            when(socksRepository.findAll(
                    ArgumentMatchers.<Specification<Socks>>any(),
                    ArgumentMatchers.<Sort>any())).thenReturn(Collections.emptyList());

            Integer result = socksService.getSocksAmountByFilter(color, "eq", 50, null, null, "quantity", "desc");
            assertEquals(0, result);
            verify(socksRepository).findAll(any(Specification.class), any(Sort.class));
        }
    }
}
