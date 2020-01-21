package io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.web.controller;

import io.exp.autoibank.secondarymarket.secondarymarketbondfrontend.services.BondSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bondsecurity")
@CrossOrigin
public class BondSecurityController {
    @Autowired
    private BondSecurityService bondSecurityService;

    @GetMapping("/{filter}")
    public ResponseEntity<String[]> getBondSecurity(@PathVariable("filter")String filter){
        String[] securities= bondSecurityService.getSecurities(filter);
        return new ResponseEntity<>(securities, HttpStatus.OK);
    }
}
