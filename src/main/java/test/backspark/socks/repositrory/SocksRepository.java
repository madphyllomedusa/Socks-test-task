package test.backspark.socks.repositrory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import test.backspark.socks.model.entity.Socks;

import java.util.Optional;

@Repository
public interface SocksRepository extends JpaRepository<Socks, Long>, JpaSpecificationExecutor<Socks> {

    Optional<Socks> findByColorAndCottonPart(String color, Integer cottonPart);
}
