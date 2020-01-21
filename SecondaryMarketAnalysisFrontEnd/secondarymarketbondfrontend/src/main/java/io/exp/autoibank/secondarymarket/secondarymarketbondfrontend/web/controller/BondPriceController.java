package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.controller;



import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services.BondPriceService;
import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.model.BondPriceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bondprice")
@CrossOrigin
public class BondPriceController {
    @Autowired
    private BondPriceService bondPriceService;

    @GetMapping("/{id}")
    public ResponseEntity<BondPriceDto> getBondPrice(@PathVariable("id")String identifier){
        BondPriceDto bondPriceDto = bondPriceService.getBondPrice(identifier);
        return new ResponseEntity<>(bondPriceDto, HttpStatus.OK);
    }

}
