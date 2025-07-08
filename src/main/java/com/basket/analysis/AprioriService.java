package com.basket.analysis;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AprioriService {

    public Map<Set<Integer>, Set<Integer>> generateAssociationRules(List<Set<Integer>> transactions) {
        Map<Set<Integer>, Set<Integer>> rules = new HashMap<>();
        Map<Set<Integer>, Integer> itemsets = new HashMap<>();

        for (Set<Integer> transaction : transactions) {
            List<Integer> items = new ArrayList<>(transaction);
            int n = items.size();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Set<Integer> pair = new HashSet<>(Arrays.asList(items.get(i), items.get(j)));
                    itemsets.put(pair, itemsets.getOrDefault(pair, 0) + 1);
                }
            }
        }

        for (Set<Integer> base : itemsets.keySet()) {
            for (Set<Integer> transaction : transactions) {
                if (transaction.containsAll(base)) {
                    for (Integer item : transaction) {
                        if (!base.contains(item)) {
                            rules.computeIfAbsent(base, k -> new HashSet<>()).add(item);
                        }
                    }
                }
            }
        }

        return rules;
    }

    public Set<Integer> suggestProducts(Set<Integer> cart, Map<Set<Integer>, Set<Integer>> rules) {
        Set<Integer> suggestions = new HashSet<>();
        for (Map.Entry<Set<Integer>, Set<Integer>> rule : rules.entrySet()) {
            if (cart.containsAll(rule.getKey())) {
                suggestions.addAll(rule.getValue());
            }
        }
        suggestions.removeAll(cart);
        return suggestions;
    }
}