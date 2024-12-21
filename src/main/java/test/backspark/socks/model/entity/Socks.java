package test.backspark.socks.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "socks")
@AllArgsConstructor
@NoArgsConstructor
public class Socks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;

    private String article;

    private String color;

    private Integer cottonPart;

    private Integer quantity;

    @CreationTimestamp
    private OffsetDateTime createdTime;

    @UpdateTimestamp
    private OffsetDateTime updatedTime;

}
