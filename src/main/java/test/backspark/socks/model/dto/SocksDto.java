package test.backspark.socks.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocksDto {
    @PositiveOrZero(message = "Неверно указан идентификатор")
    private Long id;

    @NotNull(message = "Неверно указан цвет")
    private String color;

    @NotNull(message = "Неверно указан бренд")
    private String brand;

    @NotNull(message = "Неверно указан артикул")
    private String article;

    @Max(value = 100, message = "Процент содержания хлопка не должен превышать 100")
    @PositiveOrZero(message = "Неверно указан процент содержания хлопка")
    private Integer cottonPart;

    @Positive(message = "Неверно указанно количество")
    private Integer quantity;

}
