package com.basket.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class BasketController {

    @Autowired
    private WooCommerceService wooService;

    @Autowired
    private AprioriService aprioriService;

    private List<AprioriService.Rule> rulesCache = null;
    
    private static final Logger log = LoggerFactory.getLogger(BasketController.class);

    @GetMapping("/suggestions")
    public ResponseEntity<Set<Integer>> getSuggestions(@RequestParam List<Integer> cart) {
        try {
            if (rulesCache == null) {
                List<Set<Integer>> transactions = wooService.getOrderTransactions();
                rulesCache = aprioriService.apriori(transactions);
            }

            Set<Integer> cartSet = new HashSet<>(cart);
            Set<Integer> suggestions = new HashSet<>();

            for (AprioriService.Rule rule : rulesCache) {
                if (cartSet.containsAll(rule.antecedent)) {
                    suggestions.addAll(rule.consequent);
                }
            }

            suggestions.removeAll(cart);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/suggestions/full")
    public ResponseEntity<JsonNode> getDetailedSuggestions(@RequestParam List<Integer> cart) {
        try {
            if (rulesCache == null) {
                List<Set<Integer>> transactions = wooService.getOrderTransactions();
                rulesCache = aprioriService.apriori(transactions);
            }

            Set<Integer> cartSet = new HashSet<>(cart);
            Set<Integer> suggestions = new HashSet<>();

            for (AprioriService.Rule rule : rulesCache) {
                if (cartSet.containsAll(rule.antecedent)) {
                    suggestions.addAll(rule.consequent);
                }
            }

            suggestions.removeAll(cart);

            ArrayNode array = JsonNodeFactory.instance.arrayNode();
            for (Integer id : suggestions) {
                try {
                    JsonNode product = wooService.getProductDetails(id);
                    array.add(product);                      
                } catch (IOException ex) {                    
                    log.warn("Product {} skipped: {}", id, ex.getMessage());
                    // non faccio fallire l’intera risposta
                }
            }

            return ResponseEntity.ok(array);
        } catch (Exception e) {
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/rules")
    public ResponseEntity<List<AprioriService.Rule>> getRules() {
        return ResponseEntity.ok(rulesCache != null ? rulesCache : List.of());
    }
}

