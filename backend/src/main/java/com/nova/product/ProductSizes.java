package com.nova.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSizes {
    public static final List<String> CLOTHING = List.of("XS", "S", "M", "L", "XL", "XXL", "XXXL");
    public static final List<String> FOOTWEAR_UK = List.of("UK 5", "UK 6", "UK 7", "UK 8", "UK 9", "UK 10", "UK 11", "UK 12");

    private static final String[] FOOTWEAR_SETS = {
            "UK 5,UK 6,UK 7,UK 8,UK 9,UK 10,UK 11,UK 12",
            "UK 6,UK 7,UK 8,UK 9,UK 10,UK 11",
            "UK 7,UK 8,UK 9,UK 10,UK 11,UK 12",
            "UK 5,UK 6,UK 7,UK 8,UK 9,UK 10"
    };
    private static final String[] CLOTHING_SETS = {
            "XS,S,M,L,XL,XXL,XXXL",
            "S,M,L,XL,XXL",
            "XS,S,M,L,XL,XXL",
            "M,L,XL,XXL,XXXL"
    };

    private ProductSizes() { }

    public static boolean isApparelName(String name) {
        if (name == null) return false;
        String n = name.toLowerCase(Locale.ROOT);
        return !(n.contains("belt") || n.contains("tote") || n.contains("backpack"));
    }

    public static String sizesFor(String category, String baseName, int variantIndex) {
        if ("Footwear".equals(category)) {
            return FOOTWEAR_SETS[Math.floorMod(variantIndex, FOOTWEAR_SETS.length)];
        }
        if ("Fashion".equals(category) && isApparelName(baseName)) {
            return CLOTHING_SETS[Math.floorMod(variantIndex, CLOTHING_SETS.length)];
        }
        return null;
    }

    public static List<String> parse(String raw) {
        List<String> sizes = new ArrayList<>();
        if (raw == null || raw.isBlank()) return sizes;
        for (String part : raw.split(",")) {
            String size = part.trim();
            if (!size.isEmpty() && !sizes.contains(size)) sizes.add(size);
        }
        return sizes;
    }

    public static boolean requiresSize(String raw) {
        return !parse(raw).isEmpty();
    }

    public static boolean isAllowed(String rawAvailable, String selected) {
        if (selected == null || selected.isBlank()) return false;
        String wanted = selected.trim();
        for (String size : parse(rawAvailable)) {
            if (size.equalsIgnoreCase(wanted)) return true;
        }
        return false;
    }
}
