package test.backspark.socks.specification;

import org.springframework.data.jpa.domain.Specification;
import test.backspark.socks.model.entity.Socks;

public class SocksSpecification {

    private SocksSpecification() {
        throw new UnsupportedOperationException("Этот класс нельзя инициализировать");
    }

    public static Specification<Socks> hasColor(String color) {
        return (root, query, criteriaBuilder) ->
                color == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("color"), color);
    }

    public static Specification<Socks> hasCottonPart(String operator, Integer cottonPart) {
        return (root, query, criteriaBuilder) -> {
            if (operator == null || cottonPart == null) {
                return criteriaBuilder.conjunction();
            }
            return switch (operator) {
                case "moreThan" -> criteriaBuilder.greaterThan(root.get("cottonPart"), cottonPart);
                case "lessThan" -> criteriaBuilder.lessThan(root.get("cottonPart"), cottonPart);
                case "equal" -> criteriaBuilder.equal(root.get("cottonPart"), cottonPart);
                default -> throw new IllegalArgumentException("Invalid operator: " + operator);
            };
        };
    }

    public static Specification<Socks> cottonPartBetween(Integer minCottonPart, Integer maxCottonPart) {
        return (root, query, cb) -> {
            if (minCottonPart == null && maxCottonPart == null) {
                return cb.conjunction();
            } else if (minCottonPart != null && maxCottonPart != null) {
                return cb.between(root.get("cottonPart"), minCottonPart, maxCottonPart);
            } else if (minCottonPart != null) {
                return cb.greaterThanOrEqualTo(root.get("cottonPart"), minCottonPart);
            } else {
                return cb.lessThanOrEqualTo(root.get("cottonPart"), maxCottonPart);
            }
        };
    }

}
