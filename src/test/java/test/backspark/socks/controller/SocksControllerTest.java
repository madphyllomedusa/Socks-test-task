package test.backspark.socks.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.BDDMockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import test.backspark.socks.model.dto.SocksDto;
import test.backspark.socks.service.SocksService;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SocksController.class)
@ExtendWith(MockitoExtension.class)
class SocksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SocksService socksService;

    private static final String BRAND = "nike";
    private static final String ARTICLE = "art123";

    @Nested
    @DisplayName("POST /api/socks/income")
    class IncomeTests {

        @ParameterizedTest(name = "income with color={0}, cottonPart={1}, quantity={2}")
        @CsvSource({
                "red,30,10",
                "blue,50,5"
        })
        void testIncome(String color, Integer cottonPart, Integer quantity) throws Exception {
            SocksDto outputDto = new SocksDto(1L, color.toLowerCase(), BRAND, ARTICLE, cottonPart, quantity + 20);
            BDDMockito.given(socksService.income(any(SocksDto.class))).willReturn(outputDto);

            mockMvc.perform(post("/api/socks/income")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "color": "%s",
                              "brand": "%s",
                              "article": "%s",
                              "cottonPart": %d,
                              "quantity": %d
                            }
                            """.formatted(color, BRAND, ARTICLE, cottonPart, quantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.color").value(color.toLowerCase()))
                    .andExpect(jsonPath("$.brand").value(BRAND))
                    .andExpect(jsonPath("$.article").value(ARTICLE))
                    .andExpect(jsonPath("$.cottonPart").value(cottonPart))
                    .andExpect(jsonPath("$.quantity").value(quantity + 20));
        }
    }

    @Nested
    @DisplayName("POST /api/socks/outcome")
    class OutcomeTests {

        @ParameterizedTest(name = "outcome with color={0}, cottonPart={1}, quantity={2}")
        @CsvSource({
                "red,30,10",
                "green,0,1"
        })
        void testOutcome(String color, Integer cottonPart, Integer quantity) throws Exception {
            SocksDto outputDto = new SocksDto(2L, color.toLowerCase(), BRAND, ARTICLE, cottonPart, 100 - quantity);
            BDDMockito.given(socksService.outcome(any(SocksDto.class))).willReturn(outputDto);

            mockMvc.perform(post("/api/socks/outcome")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "color": "%s",
                              "brand": "%s",
                              "article": "%s",
                              "cottonPart": %d,
                              "quantity": %d
                            }
                            """.formatted(color, BRAND, ARTICLE, cottonPart, quantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2L))
                    .andExpect(jsonPath("$.color").value(color.toLowerCase()))
                    .andExpect(jsonPath("$.brand").value(BRAND))
                    .andExpect(jsonPath("$.article").value(ARTICLE))
                    .andExpect(jsonPath("$.cottonPart").value(cottonPart))
                    .andExpect(jsonPath("$.quantity").value(100 - quantity));
        }
    }

    @Nested
    @DisplayName("PUT /api/socks/{id}")
    class UpdateTests {
        @ParameterizedTest(name = "update id={0}, color={1}, cottonPart={2}, quantity={3}")
        @CsvSource({
                "1,red,30,100",
                "2,blue,50,10"
        })
        void testUpdate(Long id, String color, Integer cottonPart, Integer quantity) throws Exception {
            SocksDto outputDto = new SocksDto(id, color.toLowerCase(), BRAND, ARTICLE, cottonPart, quantity);
            BDDMockito.given(socksService.update(any(Long.class), any(SocksDto.class))).willReturn(outputDto);

            mockMvc.perform(put("/api/socks/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "color": "%s",
                              "brand": "%s",
                              "article": "%s",
                              "cottonPart": %d,
                              "quantity": %d
                            }
                            """.formatted(color, BRAND, ARTICLE, cottonPart, quantity)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.color").value(color.toLowerCase()))
                    .andExpect(jsonPath("$.brand").value(BRAND))
                    .andExpect(jsonPath("$.article").value(ARTICLE))
                    .andExpect(jsonPath("$.cottonPart").value(cottonPart))
                    .andExpect(jsonPath("$.quantity").value(quantity));
        }
    }

    @Nested
    @DisplayName("POST /api/socks/batch")
    class BatchIncomeTests {
        static Stream<Arguments> batchArguments() {
            return Stream.of(
                Arguments.of("valid.csv", "color,brand,article,cottonPart,quantity\nred,nike,art123,30,10\nblue,nike,art999,50,20"),
                Arguments.of("partial.csv", "color,brand,article,cottonPart,quantity\nred,nike,art124,30,10\n,,,\ngreen,nike,art125,20,5")
            );
        }

        @ParameterizedTest(name = "batchIncome with file {0}")
        @MethodSource("batchArguments")
        void testBatchIncome(String fileName, String content) throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", fileName,
                    "text/csv", content.getBytes());

            List<SocksDto> mockResult = List.of(
                    new SocksDto(10L, "red", "nike", "art123", 30, 30),
                    new SocksDto(11L, "blue", "nike", "art999", 50, 40)
            );

            BDDMockito.given(socksService.batchIncome(any())).willReturn(mockResult);

            mockMvc.perform(multipart("/api/socks/batch")
                    .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(mockResult.size()));
        }
    }

    @Nested
    @DisplayName("GET /api/socks")
    class GetSocksTests {
        @ParameterizedTest(name = "getSocks with color={0}, operator={1}, cottonPart={2}")
        @CsvSource({
                "red,eq,30",
                "blue,moreThan,50",
                "green,lessThan,10"
        })
        void testGetSocksAmountByFilter(String color, String operator, Integer cottonPart) throws Exception {
            BDDMockito.given(socksService.getSocksAmountByFilter(
                    color, operator, cottonPart, null, null, null, null
            )).willReturn(35);

            mockMvc.perform(get("/api/socks")
                    .param("color", color)
                    .param("operator", operator)
                    .param("cottonPart", cottonPart.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("35"));
        }

        @ParameterizedTest(name = "getSocks empty with color={0}")
        @ValueSource(strings = {"red","blue"})
        void testGetSocksEmpty(String color) throws Exception {
            BDDMockito.given(socksService.getSocksAmountByFilter(
                    any(), any(), any(),
                    any(), any(),
                    any(), any()
            )).willReturn(0);

            mockMvc.perform(get("/api/socks")
                    .param("color", color))
                    .andExpect(status().isOk())
                    .andExpect(content().string("0"));
        }
    }
}
