package com.nova.service;

import com.nova.assistant.AssistantDtos;
import com.nova.product.Product;
import com.nova.product.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AssistantService {
    private static final Pattern BUDGET = Pattern.compile("(?:under|below|less than|budget of)\\s*₹?\\s*([0-9,]+)", Pattern.CASE_INSENSITIVE);
    private static final List<String> STOP_WORDS = List.of("under", "below", "less", "than", "with", "for", "the", "and", "show", "find", "want", "need", "budget", "please");

    private final ProductRepository products;
    private final String apiKey;
    private final String model;
    private final RestClient client = RestClient.builder().baseUrl("https://api.openai.com/v1").build();

    public AssistantService(ProductRepository products,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model:gpt-4o-mini}") String model) {
        this.products = products;
        this.apiKey = apiKey == null ? "" : apiKey;
        this.model = model;
    }

    public AssistantDtos.Reply ask(String message) {
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }
        List<Product> matches = match(message);
        if (!apiKey.isBlank()) {
            try {
                Map<?, ?> body = client.post().uri("/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .body(Map.of("model", model,
                                "messages", List.of(
                                        Map.of("role", "system", "content",
                                                "You are NOVA, a concise shopping assistant. Recommend only products in this catalogue. Never invent prices, stock, delivery promises, or product details. Catalogue matches: " + summarize(matches)),
                                        Map.of("role", "user", "content", message)),
                                "temperature", 0.3))
                        .retrieve().body(Map.class);
                String content = extractContent(body);
                if (content != null && !content.isBlank()) {
                    return new AssistantDtos.Reply(content, matches, "ai");
                }
            } catch (Exception ignored) {
                // Continue with catalogue-only recommendations if the provider is unavailable.
            }
        }
        String reply = matches.isEmpty()
                ? "I couldn't find a close catalogue match. Try a product type, brand, colour, or a budget such as ‘running shoes under 5000’."
                : "I found " + matches.size() + " catalogue match" + (matches.size() == 1 ? "" : "es")
                        + " for your request. These are ranked from the current catalogue; open a product to see its details.";
        return new AssistantDtos.Reply(reply, matches, "catalogue");
    }

    private String extractContent(Map<?, ?> body) {
        if (body == null || !(body.get("choices") instanceof List<?> choices) || choices.isEmpty()) return null;
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> choice) || !(choice.get("message") instanceof Map<?, ?> message)) return null;
        Object content = message.get("content");
        return content == null ? null : String.valueOf(content);
    }

    private List<Product> match(String query) {
        Matcher matcher = BUDGET.matcher(query);
        BigDecimal maxPrice = matcher.find() ? new BigDecimal(matcher.group(1).replace(",", "")) : null;
        String lower = query.toLowerCase();
        String[] terms = lower.split("[^a-z0-9]+|₹");
        return products.findAll().stream()
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> {
                    String haystack = (p.getName() + " " + p.getCategory() + " " + p.getBrand() + " "
                            + p.getDescription() + " " + p.getTags() + " " + p.getColor()).toLowerCase();
                    for (String term : terms) {
                        if (term.length() > 2 && !STOP_WORDS.contains(term) && haystack.contains(term)) return true;
                    }
                    return false;
                })
                .sorted(Comparator.comparing(Product::getFeatured, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Product::getRating, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4).toList();
    }

    private String summarize(List<Product> matches) {
        return matches.stream().map(p -> p.getName() + " | " + p.getCategory() + " | INR " + p.getPrice()).toList().toString();
    }
}
