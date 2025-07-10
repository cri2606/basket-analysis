package com.basket.analysis;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AprioriService {

    private double minSupport = 0.5;
    private double minConfidence = 0.7;

    public static class Rule {
        public Set<Integer> antecedent;
        public Set<Integer> consequent;
        public double support;
        public double confidence;
        public double lift;

        public Rule(Set<Integer> antecedent, Set<Integer> consequent, double support, double confidence, double lift) {
            this.antecedent = antecedent;
            this.consequent = consequent;
            this.support = support;
            this.confidence = confidence;
            this.lift = lift;
        }

        @Override
        public String toString() {
            return antecedent + " => " + consequent +
                   " [support=" + support +
                   ", confidence=" + confidence +
                   ", lift=" + lift + "]";
        }
    }

    public List<Rule> apriori(List<Set<Integer>> transactions) {
        int transactionCount = transactions.size();
        List<Rule> rules = new ArrayList<>();
        Map<Set<Integer>, Integer> freqItemsets = new HashMap<>();

        // 1. Genera itemset frequenti con supporto ≥ minSupport
        Map<Set<Integer>, Integer> candidates = generateCandidates(transactions, 1);
        while (!candidates.isEmpty()) {
            Map<Set<Integer>, Integer> filtered = filterBySupport(candidates, transactionCount);
            freqItemsets.putAll(filtered);

            Set<Set<Integer>> nextGen = generateNextItemsets(filtered.keySet());
            candidates = countItemsets(nextGen, transactions);
        }

        // 2. Genera regole valide da itemset frequenti
        for (Set<Integer> itemset : freqItemsets.keySet()) {
            if (itemset.size() < 2) continue;

            int itemsetCount = freqItemsets.get(itemset);

            Set<Set<Integer>> subsets = getSubsets(itemset);
            for (Set<Integer> antecedent : subsets) {
                if (antecedent.isEmpty() || antecedent.size() == itemset.size()) continue;

                Set<Integer> consequent = new HashSet<>(itemset);
                consequent.removeAll(antecedent);

                int antecedentCount = freqItemsets.getOrDefault(antecedent, 0);
                int consequentCount = freqItemsets.getOrDefault(consequent, 0);

                if (antecedentCount == 0 || consequentCount == 0) continue;

                double support = (double) itemsetCount / transactionCount;
                double confidence = (double) itemsetCount / antecedentCount;
                double lift = confidence / ((double) consequentCount / transactionCount);

                if (confidence >= minConfidence) {
                    rules.add(new Rule(antecedent, consequent, support, confidence, lift));
                }
            }
        }

        return rules;
    }

    private Map<Set<Integer>, Integer> generateCandidates(List<Set<Integer>> transactions, int size) {
        Map<Set<Integer>, Integer> candidates = new HashMap<>();
        for (Set<Integer> transaction : transactions) {
            if (transaction.size() < size) continue;
            Set<Set<Integer>> subsets = combinations(transaction, size);
            for (Set<Integer> subset : subsets) {
                candidates.put(subset, candidates.getOrDefault(subset, 0) + 1);
            }
        }
        return candidates;
    }

    private Map<Set<Integer>, Integer> filterBySupport(Map<Set<Integer>, Integer> candidates, int totalTransactions) {
        Map<Set<Integer>, Integer> result = new HashMap<>();
        for (Map.Entry<Set<Integer>, Integer> entry : candidates.entrySet()) {
            double support = (double) entry.getValue() / totalTransactions;
            if (support >= minSupport) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private Set<Set<Integer>> generateNextItemsets(Set<Set<Integer>> itemsets) {
        Set<Set<Integer>> result = new HashSet<>();
        List<Set<Integer>> itemsetList = new ArrayList<>(itemsets);

        for (int i = 0; i < itemsetList.size(); i++) {
            for (int j = i + 1; j < itemsetList.size(); j++) {
                Set<Integer> a = itemsetList.get(i);
                Set<Integer> b = itemsetList.get(j);
                Set<Integer> union = new HashSet<>(a);
                union.addAll(b);
                if (union.size() == a.size() + 1) {
                    result.add(union);
                }
            }
        }
        return result;
    }

    private Map<Set<Integer>, Integer> countItemsets(Set<Set<Integer>> candidates, List<Set<Integer>> transactions) {
        Map<Set<Integer>, Integer> counts = new HashMap<>();
        for (Set<Set<Integer>> transactionCandidates : transactions.stream().map(t -> combinations(t, candidates.iterator().next().size())).toList()) {
            for (Set<Integer> candidate : candidates) {
                if (transactionCandidates.contains(candidate)) {
                    counts.put(candidate, counts.getOrDefault(candidate, 0) + 1);
                }
            }
        }
        return counts;
    }

    private Set<Set<Integer>> getSubsets(Set<Integer> set) {
        Set<Set<Integer>> subsets = new HashSet<>();
        List<Integer> list = new ArrayList<>(set);
        int n = list.size();
        for (int i = 1; i < (1 << n); i++) {
            Set<Integer> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    subset.add(list.get(j));
                }
            }
            subsets.add(subset);
        }
        return subsets;
    }

    private Set<Set<Integer>> combinations(Set<Integer> set, int size) {
        List<Integer> list = new ArrayList<>(set);
        Set<Set<Integer>> result = new HashSet<>();
        backtrack(list, 0, size, new LinkedList<>(), result);
        return result;
    }

    private void backtrack(List<Integer> list, int start, int size, LinkedList<Integer> current, Set<Set<Integer>> result) {
        if (current.size() == size) {
            result.add(new HashSet<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            backtrack(list, i + 1, size, current, result);
            current.removeLast();
        }
    }
}
