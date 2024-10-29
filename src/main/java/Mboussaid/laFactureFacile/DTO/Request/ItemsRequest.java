package Mboussaid.laFactureFacile.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ItemsRequest {

    private Integer priceHT;
    private Integer priceTTC;
    private Integer tax;
    private Integer quantity;
}
