package com.nova.product;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductSeeder implements CommandLineRunner {
    private final ProductRepository repo;

    public ProductSeeder(ProductRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        // If repo already has data and product 1 has an accurate image, leave existing catalogue intact.
        if (repo.count() == 0) {
            seedCoreCatalogue();
        } else {
            var first = repo.findById(1L);
            if (first.isPresent() && first.get().getImageUrl().contains("photo-1542291026-7eec264c27ff")) {
                seedCoreCatalogue();
            }
        }
        ensureApparelAndFootwearSizes();
        ensureBookCatalogue();
    }

    private void seedCoreCatalogue() {

        String[] categories = {
            "Electronics", "Fashion", "Footwear", "Home & Living",
            "Beauty", "Sports & Lifestyle", "Books & Stationery", "Accessories"
        };

        String[][] names = {
            {"Wireless Headphones", "Smart Watch", "Mirrorless Camera", "Bluetooth Speaker", "Laptop Sleeve", "Portable SSD", "Mechanical Keyboard", "Wireless Mouse", "Smartphone", "Tablet"},
            {"Everyday Overshirt", "Relaxed Cotton Tee", "Tailored Trousers", "Minimal Leather Belt", "Structured Tote", "Classic Denim", "Linen Shirt", "Wool Blend Coat", "Everyday Hoodie", "Canvas Backpack"},
            {"Urban Runner Sneakers", "Minimal Leather Sneakers", "Trail Running Shoes", "Retro Court Sneakers", "Everyday Loafers", "Studio Training Shoes", "Leather Chelsea Boots", "Canvas High Tops", "Lightweight Slides", "City Walking Shoes"},
            {"Sculptural Table Lamp", "Ceramic Pour-Over Set", "Lounge Accent Chair", "Linen Cushion Cover", "Walnut Desk Organizer", "Stoneware Vase", "Soft Knit Throw", "Minimal Wall Clock", "Oak Side Table", "Glass Carafe"},
            {"Daily Hydration Serum", "Botanical Face Cleanser", "Nourishing Hair Oil", "Mineral Sunscreen", "Overnight Recovery Cream", "Gentle Body Wash", "Satin Lip Tint", "Daily Grooming Kit", "Citrus Hand Cream", "Scalp Care Tonic"},
            {"Insulated Water Bottle", "Performance Yoga Mat", "Training Duffel", "Running Belt", "Recovery Foam Roller", "Lightweight Daypack", "Cycling Gloves", "Resistance Band Set", "Travel Organizer", "Outdoor Cap"},
            {"Ideas Notebook", "Hardcover Journal", "Design Thinking Book", "Weekly Planner", "Fine Tip Pen Set", "Desk Notepad", "Art Print Collection", "Reading Light", "Travel Sketchbook", "Document Folio"},
            {"Minimalist Backpack", "Polarized Sunglasses", "Everyday Crossbody", "Leather Card Holder", "Classic Analog Watch", "Travel Wallet", "Compact Umbrella", "Sculpted Pendant", "Tech Organizer", "Weekend Duffle"}
        };

        String[][] imgs = {
            {"photo-1505740420928-5e560c06d30e", "photo-1523275335684-37898b6baf30", "photo-1526170375885-4d8ecf77b99f", "photo-1545454675-3531b543be5d", "photo-1544816155-12df9643f363", "photo-1597872200969-2b65d56bd16b", "photo-1587829741301-dc798b83add3", "photo-1527864550417-7fd91fc51a46", "photo-1511707171634-5f897ff02aa9", "photo-1544244015-0df4b3ffc6b0"},
            {"photo-1591047139829-d91aecb6caea", "photo-1521572163474-6864f9cf17ab", "photo-1624378439575-d8705ad7ae80", "photo-1624222247344-550fb60583dc", "photo-1548036328-c9fa89d128fa", "photo-1541099649105-f69ad21f3246", "photo-1602810318383-e386cc2a3ccf", "photo-1539533018447-63fcce667883", "photo-1556905055-8f358a7a47b2", "photo-1553062407-98eeb64c6a62"},
            {"photo-1542291026-7eec264c27ff", "photo-1560769629-975ec94e6a86", "photo-1584735935682-2f2b69dff9d2", "photo-1595950653106-6c9ebd614d3a", "photo-1533867617858-e7b97e060509", "photo-1608231387042-66d1773070a5", "photo-1638247025967-b4e38f787b76", "photo-1607522370275-f14206abe5d3", "photo-1603808033192-082d6919d3e1", "photo-1491553895911-0055eca6402d"},
            {"photo-1507473885765-e6ed057f782c", "photo-1514432324607-a09d9b4aefdd", "photo-1580481077195-c228c3b7454f", "photo-1584100936595-c0654b55a2e2", "photo-1544716278-ca5e3f4abd8c", "photo-1578749556568-bc2c40e68b61", "photo-1580301762395-21ce84d00bc6", "photo-1563861826100-9cb868fdbe1c", "photo-1533090161767-e6ffed986c88", "photo-1513558161293-cdaf765ed2fd"},
            {"photo-1620916566398-39f1143ab7be", "photo-1556228720-195a672e8a03", "photo-1608248597359-00201083981d", "photo-1598440947619-2c35fc9aa908", "photo-1522337360788-8b13dee7a37e", "photo-1585232351009-aa87416fca90", "photo-1586495777744-4413f21062fa", "photo-1621607512214-68297480165e", "photo-1617897903246-719242758050", "photo-1601049541289-9b1b7bbbfe19"},
            {"photo-1602143407151-7111542de6e8", "photo-1601925260368-ae2f83cf8b7f", "photo-1553062407-98eeb64c6a62", "photo-1517838277536-f5f99be501cd", "photo-1518611012118-696072aa579a", "photo-1546938576-6e6a64f317cc", "photo-1559348349-86f1f65817fe", "photo-1598289431512-b97b0917affc", "photo-1522335789203-aabd1fc54bc9", "photo-1588850561407-ed78c282e89b"},
            {"photo-1531346878377-a5be20888e57", "photo-1544716278-ca5e3f4abd8c", "photo-1512820790803-83ca734da794", "photo-1506784983877-45594efa4cbe", "photo-1583485088034-697b5bc54ccd", "photo-1586075010923-2dd4570fb338", "photo-1579783900882-c0d3dad7b119", "photo-1513506003901-1e6a229e2d15", "photo-1544717305-2782549b5136", "photo-1553062407-98eeb64c6a62"},
            {"photo-1546938576-6e6a64f317cc", "photo-1511499767150-a48a237f0083", "photo-1548036328-c9fa89d128fa", "photo-1627123424574-724758594e93", "photo-1524805444758-089113d48a6d", "photo-1563903530908-afdd155d057a", "photo-1556909114-f6e7ad7d3136", "photo-1599643478518-a784e5dc4c8f", "photo-1522335789203-aabd1fc54bc9", "photo-1553062407-98eeb64c6a62"}
        };

        int[][] basePrices = {
            {6999, 12999, 34999, 3499, 1799, 5999, 4299, 1899, 28999, 22999},
            {3299, 999, 2999, 1499, 4499, 2799, 2199, 8999, 2699, 3499},
            {4999, 4499, 5999, 3999, 5499, 4299, 7999, 2499, 1499, 4799},
            {3999, 1899, 16999, 899, 1999, 1499, 2499, 2799, 5999, 1199},
            {1499, 999, 1599, 1199, 2199, 899, 799, 2999, 599, 1399},
            {1399, 2299, 3299, 899, 1499, 2799, 1299, 799, 1699, 899},
            {799, 1199, 1499, 1299, 699, 499, 2299, 1599, 999, 1899},
            {4299, 2999, 3499, 1199, 6999, 1999, 1299, 3299, 1799, 5499}
        };

        String[] brands = {"Aster & Co.", "Northline", "Forma Studio", "Monarch", "NOVA Select", "Atelier Nine", "Fieldwork", "Lumen"};
        String[] colors = {"Obsidian", "Ivory", "Stone", "Olive", "Midnight", "Sand", "Silver", "Cobalt"};
        String[] suffix = {"", " Mk II", " Edition", " Studio", " Essential", " Pro", " Classic", " Limited"};
        double[] mults = {1.0, 1.15, 1.25, 1.10, 0.85, 1.40, 1.05, 1.50};

        List<Product> batch = new ArrayList<>(1000);
        long id = 1;

        for (int c = 0; c < categories.length; c++) {
            for (int n = 0; n < 125; n++) {
                int itemIdx = n % 10;
                String base = names[c][itemIdx];
                int sIdx = n % 8;
                String name = base + suffix[sIdx];
                double m = mults[sIdx];
                int price = (int) Math.round((basePrices[c][itemIdx] * m) / 100.0) * 100 - 1;
                if (price < 499) price = 499;

                String image = "https://images.unsplash.com/" + imgs[c][itemIdx] + "?auto=format&fit=crop&w=900&q=85";
                String brand = brands[(int) ((id + n) % brands.length)];
                String color = colors[(int) ((id + n) % colors.length)];
                double rating = 4.0 + ((id * 13) % 10) / 10.0;
                int reviewCount = (int) (14 + (id * 37) % 850);
                boolean featured = n < 3;
                String slug = categories[c].toLowerCase().replace(" ", "-").replace("&", "and") + "-" + id;
                String description = "A considered " + base.toLowerCase() + " designed for everyday use. Thoughtful materials, refined details and versatile performance make it an easy addition to your daily routine.";
                String tags = categories[c].toLowerCase() + "," + base.toLowerCase() + "," + color.toLowerCase() + "," + brand.toLowerCase();

                Product product = new Product(id, name, slug, categories[c], brand, BigDecimal.valueOf(price), rating, reviewCount, description, image, color, featured, tags);
                product.setAvailableSizes(ProductSizes.sizesFor(categories[c], base, n));
                batch.add(product);
                id++;
            }
        }
        repo.saveAll(batch);
        System.out.println("NOVA catalogue synced: " + batch.size() + " products");
    }

    private void ensureApparelAndFootwearSizes() {
        List<Product> toUpdate = new ArrayList<>();
        for (Product product : repo.findByCategoryIn(List.of("Fashion", "Footwear"))) {
            if (ProductSizes.requiresSize(product.getAvailableSizes())) continue;
            String sizes = ProductSizes.sizesFor(product.getCategory(), product.getName(), product.getId().intValue());
            if (sizes == null) continue;
            product.setAvailableSizes(sizes);
            toUpdate.add(product);
        }
        if (!toUpdate.isEmpty()) {
            repo.saveAll(toUpdate);
            System.out.println("NOVA size options synced: " + toUpdate.size() + " products");
        }
    }

    private void ensureBookCatalogue() {
        List<Product> added = new ArrayList<>();
        long nextId = Math.max(repo.findMaxId() + 1, 1001);
        for (BookCatalogue.Entry entry : BookCatalogue.entries()) {
            if (repo.findBySlug(entry.slug()).isPresent()) continue;
            long id = entry.preferredId();
            if (repo.existsById(id) || id < 1001) {
                id = nextId++;
            } else {
                nextId = Math.max(nextId, id + 1);
            }
            String image = "https://images.unsplash.com/" + entry.unsplashId() + "?auto=format&fit=crop&w=900&q=85";
            String tags = entry.category().toLowerCase() + "," + entry.genre().toLowerCase() + "," + entry.author().toLowerCase()
                    + "," + entry.title().toLowerCase() + ",representative-cover";
            String description = entry.description()
                    + " Cover photography is representative catalogue art, not an official publisher jacket. Author, title and subject are taken from the published work; ISBNs and editions are omitted.";
            added.add(new Product(id, entry.title(), entry.slug(), entry.category(), entry.author(),
                    BigDecimal.valueOf(entry.priceInr()), 4.6, 48 + (int) (id % 200), description, image,
                    "Ivory", entry.featured(), tags));
        }
        if (!added.isEmpty()) {
            repo.saveAll(added);
            System.out.println("NOVA book catalogue synced: " + added.size() + " titles");
        }
    }
}
