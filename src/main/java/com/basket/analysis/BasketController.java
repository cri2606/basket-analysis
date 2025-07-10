package com.basket.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    private List<AprioriService.Rule> rulesCache = null;

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
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
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
                JsonNode product = wooService.getProductDetails(id);
                array.add(product);
            }

            return ResponseEntity.ok(array);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/rules")
    public ResponseEntity<List<AprioriService.Rule>> getRules() {
        return ResponseEntity.ok(rulesCache != null ? rulesCache : List.of());
    }
}

