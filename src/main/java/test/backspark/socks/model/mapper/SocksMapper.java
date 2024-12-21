package test.backspark.socks.model.mapper;

import org.springframework.stereotype.Component;
import test.backspark.socks.model.dto.SocksDto;
import test.backspark.socks.model.entity.Socks;

@Component
public class SocksMapper {

    public SocksDto mapToDto(Socks socks) {
        SocksDto socksDto = new SocksDto();
        socksDto.setId(socks.getId());
        socksDto.setColor(socks.getColor().toLowerCase());
        socksDto.setCottonPart(socks.getCottonPart());
        socksDto.setQuantity(socks.getQuantity());
        return socksDto;
    }
}
