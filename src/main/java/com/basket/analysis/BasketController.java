package com.basket.analysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class BasketController {

    @Autowired
    private WooCommerceService wooService;

    @Autowired
    private AprioriService aprioriService;

    private Map<Set<Integer>, Set<Integer>> rulesCache = null;

    @GetMapping("/suggestions")
    public ResponseEntity<Set<Integer>> getSuggestions(@RequestParam List<Integer> cart) {
        try {
            if (rulesCache == null) {
                List<Set<Integer>> transactions = wooService.getOrderTransactions();
                rulesCache = aprioriService.generateAssociationRules(transactions);
            }

            Set<Integer> cartSet = new HashSet<>(cart);
            Set<Integer> suggestions = aprioriService.suggestProducts(cartSet, rulesCache);
            return ResponseEntity.ok(suggestions);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/rules")
    public ResponseEntity<Map<Set<Integer>, Set<Integer>>> getRules() {
        return ResponseEntity.ok(rulesCache != null ? rulesCache : Map.of());
    }
}