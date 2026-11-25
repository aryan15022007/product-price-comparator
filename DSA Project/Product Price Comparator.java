import java.util.*;
import java.util.stream.Collectors;

/**
 * Product Price Comparator
 * Compares offers for a product across stores by total cost (price + shipping - discount).
 * Requires Java 16+ (uses records).
 *
 * Compile: javac ProductPriceComparator.java
 * Run:     java ProductPriceComparator
 */
public class ProductPriceComparator {

    // One store's offer for a product
    record Offer(String store, double price, double shipping, double discountPercent) {
        double totalCost() {
            double discounted = price - (price * discountPercent / 100.0);
            return discounted + shipping;
        }
    }

    // A product with offers from multiple stores
    static class Product {
        private final String name;
         private final List<Offer> offers = new ArrayList<>();

        Product(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        void addOffer(String store, double price, double shipping, double discountPercent) {
            if (price < 0 || shipping < 0 || discountPercent < 0 || discountPercent > 100) {
                throw new IllegalArgumentException("Invalid price, shipping or discount value");
            }
            offers.add(new Offer(store, price, shipping, discountPercent));
        }

        List<Offer> getOffersSorted() {
            return offers.stream()
                    .sorted(Comparator.comparingDouble(Offer::totalCost))
                    .collect(Collectors.toList());
        }

        Optional<Offer> getBestOffer() {
            return offers.stream().min(Comparator.comparingDouble(Offer::totalCost));
        }
    }

    // Simple in-memory catalog
    static class Catalog {
        private final Map<String, Product> products = new LinkedHashMap<>();

        void add(Product p) {
            products.put(p.getName().toLowerCase(), p);
        }

        List<Product> search(String keyword) {
            String k = keyword.toLowerCase();
            return products.values().stream()
                    .filter(p -> p.getName().toLowerCase().contains(k))
                    .collect(Collectors.toList());
        }

        Collection<Product> all() {
            return products.values();
        }
    }

    static void printComparison(Product product) {
        System.out.println("\n=== " + product.getName() + " ===");
        System.out.printf("%-4s %-14s %10s %10s %10s %12s%n",
                "#", "Store", "Price", "Discount", "Shipping", "Total");
        System.out.println("-".repeat(65));

        List<Offer> sorted = product.getOffersSorted();
        int rank = 1;
        for (Offer o : sorted) {
            System.out.printf("%-4d %-14s %10.2f %9.0f%% %10.2f %12.2f%n",
                    rank++, o.store(), o.price(), o.discountPercent(), o.shipping(), o.totalCost());
        }

        product.getBestOffer().ifPresent(best -> {
            double worst = sorted.get(sorted.size() - 1).totalCost();
            System.out.printf("%nBest deal: %s at %.2f (saves %.2f vs. most expensive)%n",
                    best.store(), best.totalCost(), worst - best.totalCost());
        });
    }

    static Catalog loadSampleData() {
        Catalog catalog = new Catalog();

        Product phone = new Product("Smartphone X200");
        phone.addOffer("ShopMart", 29999, 0, 5);
        phone.addOffer("QuickBuy", 28499, 149, 0);
        phone.addOffer("MegaStore", 30999, 0, 10);
        catalog.add(phone);

        Product laptop = new Product("Laptop Pro 14");
        laptop.addOffer("ShopMart", 64999, 0, 0);
        laptop.addOffer("QuickBuy", 62999, 299, 2);
        laptop.addOffer("MegaStore", 66500, 0, 8);
        catalog.add(laptop);

        Product earbuds = new Product("Wireless Earbuds");
        earbuds.addOffer("ShopMart", 2499, 49, 10);
        earbuds.addOffer("QuickBuy", 2299, 0, 0);
        earbuds.addOffer("MegaStore", 2799, 0, 15);
        catalog.add(earbuds);

        return catalog;
    }

    public static void main(String[] args) {
        Catalog catalog = loadSampleData();
        Scanner sc = new Scanner(System.in);

        System.out.println("Product Price Comparator");
        System.out.println("Type a product name to search, 'all' to compare everything, or 'exit' to quit.");

        while (true) {
            System.out.print("\nSearch> ");
            if (!sc.hasNextLine()) break;
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            if (input.isEmpty()) continue;

            List<Product> results = input.equalsIgnoreCase("all")
                    ? new ArrayList<>(catalog.all())
                    : catalog.search(input);

            if (results.isEmpty()) {
                System.out.println("No products found for: " + input);
            } else {
                results.forEach(ProductPriceComparator::printComparison);
            }
        }
        sc.close();
    }
}