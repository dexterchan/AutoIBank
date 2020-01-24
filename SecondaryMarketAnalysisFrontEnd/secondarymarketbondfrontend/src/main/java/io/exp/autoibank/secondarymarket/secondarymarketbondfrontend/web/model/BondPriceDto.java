package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BondPriceDto {
    private  String identifier;
    private  double bid;
    private  double ask;

}
