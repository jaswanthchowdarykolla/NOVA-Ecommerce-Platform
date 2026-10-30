package com.nova.service;

import com.nova.assistant.AssistantDtos;
import com.nova.product.Product;
import com.nova.product.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AssistantService {
    private static final Logger log = LoggerFactory.getLogger(AssistantService.class);

    private static final Pattern BUDGET = Pattern.compile(
            "(?:under|below|less than|budget of|within)\\s*₹?\\s*([0-9,]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final List<String> STOP_WORDS = List.of(
            "under", "below", "less", "than", "with", "for", "the", "and", "show",
            "find", "want", "need", "budget", "please", "search", "give", "tell",
            "recommend", "suggest", "products", "product", "items", "item", "things",
            "thing", "anything", "what", "have", "you", "within", "good", "best", "some"
    );

    private final ProductRepository products;
    private final String apiKey;
    private final String model;
    private final RestClient client;

    public AssistantService(ProductRepository products,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model:nvidia/nemotron-3-ultra-550b-a55b:free}") String model,
            @Value("${app.ai.base-url:https://openrouter.ai/api/v1}") String baseUrl) {
        this.products = products;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank() ? "nvidia/nemotron-3-ultra-550b-a55b:free" : model.trim();
        String url = baseUrl == null || baseUrl.isBlank() ? "https://openrouter.ai/api/v1" : baseUrl.trim();
        this.client = RestClient.builder().baseUrl(url).build();
    }

    public AssistantDtos.Reply ask(String message) {
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }
        List<Product> matches = match(message);
        if (!apiKey.isBlank()) {
            try {
                String systemPrompt = "You are NOVA, a friendly and concise shopping assistant for the NOVA e-commerce store. "
                        + "Help the customer discover products from our catalogue. "
                        + "Recommend only products from this list with their exact name, category, and INR price. "
                        + "Keep your response concise, polite, and helpful (under 3-4 sentences). "
                        + "Catalogue matches for this query:\n" + summarize(matches);

                Map<?, ?> body = client.post().uri("/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("HTTP-Referer", "http://localhost:5173")
                        .header("X-Title", "NOVA E-Commerce")
                        .body(Map.of("model", model,
                                "messages", List.of(
                                        Map.of("role", "system", "content", systemPrompt),
                                        Map.of("role", "user", "content", message)),
                                "temperature", 0.3))
                        .retrieve().body(Map.class);

                String content = extractContent(body);
                if (content != null && !content.isBlank()) {
                    return new AssistantDtos.Reply(content, matches, "ai");
                }
            } catch (Exception e) {
                log.warn("AI chat completion with model {} failed: {}", model, e.getMessage());
                // Continue with catalogue-only recommendations if the provider is unavailable.
            }
        }
        String reply = matches.isEmpty()
                ? "I couldn't find a close catalogue match. Try a product type, brand, colour, or a budget such as ‘shoes under 3000’."
                : "I found " + matches.size() + " catalogue match" + (matches.size() == 1 ? "" : "es")
                        + " for your request. These are ranked from the current catalogue; click a product below to view its details.";
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
        String[] rawTerms = lower.split("[^a-z0-9]+|₹");
        List<String> meaningfulTerms = new ArrayList<>();
        for (String term : rawTerms) {
            if (term.length() > 2 && !STOP_WORDS.contains(term) && !term.matches("\\d+")) {
                meaningfulTerms.add(term);
            }
        }

        return products.findAll().stream()
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> {
                    if (meaningfulTerms.isEmpty()) {
                        // Budget-only or generic query like "products under 3000"
                        return true;
                    }
                    String haystack = (p.getName() + " " + p.getCategory() + " " + p.getBrand() + " "
                            + p.getDescription() + " " + (p.getTags() == null ? "" : p.getTags()) + " "
                            + (p.getColor() == null ? "" : p.getColor())).toLowerCase();
                    for (String term : meaningfulTerms) {
                        if (haystack.contains(term)) return true;
                        // Handle simple plural: shoes -> shoe, sneakers -> sneaker, etc.
                        if (term.endsWith("s") && term.length() > 3 && haystack.contains(term.substring(0, term.length() - 1))) {
                            return true;
                        }
                    }
                    return false;
                })
                .sorted(Comparator.comparing(Product::getFeatured, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Product::getRating, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(4).toList();
    }

    private String summarize(List<Product> matches) {
        if (matches == null || matches.isEmpty()) return "None";
        return matches.stream()
                .map(p -> "- " + p.getName() + " (" + p.getCategory() + ", " + p.getBrand() + ") - INR " + p.getPrice())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("None");
    }
}
