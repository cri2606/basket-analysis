package com.basket.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WooCommerceService {

    @Value("${woocommerce.api.url}")
    private String apiUrl;

    @Value("${woocommerce.api.key}")
    private String apiKey;

    @Value("${woocommerce.api.secret}")
    private String apiSecret;

    public List<Set<Integer>> getOrderTransactions() throws IOException {
        String credentials = apiKey + ":" + apiSecret;
        String base64Creds = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl + "?per_page=100").openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + base64Creds);
        connection.setRequestProperty("Accept", "application/json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.lines().collect(Collectors.joining());
        reader.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode orders = mapper.readTree(response);

        List<Set<Integer>> transactions = new ArrayList<>();

        for (JsonNode order : orders) {
            Set<Integer> products = new HashSet<>();
            for (JsonNode item : order.get("line_items")) {
                products.add(item.get("product_id").asInt());
            }
            if (!products.isEmpty()) {
                transactions.add(products);
            }
        }

        return transactions;
    }
}
