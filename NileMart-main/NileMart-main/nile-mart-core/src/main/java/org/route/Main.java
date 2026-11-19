package org.route;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final Map<Integer, ShoppingCart> carts = new LinkedHashMap<>();
    private int nextCartNumber = 1;

    private final ProductService productService;
    private final CheckoutService checkoutService;
    private final AnnotationConfigApplicationContext context;

    private Main(AnnotationConfigApplicationContext context) {
        this.context = context;
        this.productService = context.getBean(ProductService.class);
        this.checkoutService = context.getBean(CheckoutService.class);
    }

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {
            new Main(context).run();
        }
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> showProducts();
                    case 2 -> createCart();
                    case 3 -> addProductToCart();
                    case 4 -> showCart();
                    case 5 -> checkoutCart();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option. Please choose 0 to 5.");
                }
            } catch (StoreException ex) {
                // Each operation handles expected business failures without terminating the app.
                System.out.println("Store error: " + ex.getMessage());
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid input: " + ex.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n========== NILEMART ==========");
        System.out.println("1. Show products");
        System.out.println("2. Create a cart");
        System.out.println("3. Add a product to a cart");
        System.out.println("4. Show a cart");
        System.out.println("5. Check out a cart");
        System.out.println("0. Exit");
        System.out.println("==============================");
    }

    private void showProducts() {
        List<Product> products = productService.findAll();
        System.out.println("\n--- PRODUCT CATALOG ---");
        products.forEach(product -> System.out.printf(
                Locale.US,
                "%s | %-25s | %-15s | %,.2f EGP | Stock: %d | %s%n",
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStockQuantity() == 0 ? "OUT OF STOCK" : "Available"
        ));
    }
    private void createCart() {
        String fullName = readLine("Customer full name: ");
        String phone = readLine("Phone number: ");
        String email = readLine("Email (optional, press Enter to skip): ");

        Customer customer = new Customer(
                fullName,
                phone,
                email.isBlank() ? null : email
        );

        // Each getBean call returns a new ShoppingCart because its scope is prototype.
        ShoppingCart cart = context.getBean(ShoppingCart.class);
        int cartNumber = nextCartNumber++;
        cart.initialize(cartNumber, customer);
        carts.put(cartNumber, cart);

        System.out.println("Cart created successfully. Cart number: " + cartNumber);
    }

    private void addProductToCart() {
        int cartNumber = readInt("Cart number: ");
        ShoppingCart cart = getCart(cartNumber);
        String sku = readLine("Product SKU: ");
        int quantity = readInt("Quantity: ");

        cart.addItem(sku, quantity);
        System.out.println("Product added. Updated cart subtotal: "
                + String.format(Locale.US, "%,.2f", cart.subTotal()) + " EGP");
    }

    private void showCart() {
        int cartNumber = readInt("Cart number: ");
        ShoppingCart cart = getCart(cartNumber);

        System.out.println("\nCart #" + cart.getCartNumber()
                + " — " + cart.getCustomer().fullName());

        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        cart.getLines().forEach((sku, quantity) -> {
            Product product = productService.findBySku(sku);
            BigDecimal linePrice = productService.getPrice(sku, quantity);
            System.out.printf(
                    Locale.US,
                    "%s | %s | Qty: %d | Line price: %,.2f EGP%n",
                    sku,
                    product.getName(),
                    quantity,
                    linePrice
            );
        });

        System.out.printf(
                Locale.US,
                "Subtotal: %,.2f EGP%n",
                cart.subTotal()
        );
    }
    private void checkoutCart() {
        int cartNumber = readInt("Cart number: ");
        ShoppingCart cart = getCart(cartNumber);

        System.out.println("Select governorate:");
        Governorate[] governorates = Governorate.values();
        for (int i = 0; i < governorates.length; i++) {
            System.out.printf("%2d. %s (%s)%n",
                    i + 1,
                    governorates[i].getDisplayName(),
                    governorates[i].getZone());
        }

        int governorateNumber;
        while (true) {
            governorateNumber = readInt("Governorate number: ");
            if (governorateNumber >= 1 && governorateNumber <= governorates.length) {
                break;
            }
            System.out.println("Please choose a valid governorate number from 1 to "
                    + governorates.length + ".");
        }

        Order order = checkoutService.placeOrder (
                cart,
                governorates[governorateNumber - 1]
        );

        System.out.println("\n========== ORDER CONFIRMED ==========");
        System.out.println("Order number: " + order.orderNumber());
        System.out.println("Customer: " + order.customer().fullName());
        System.out.println("Governorate: " + order.governorate());
        order.lines().forEach(line -> System.out.printf(
                Locale.US,
                "%s | %s | Qty: %d | %,.2f EGP%n",
                line.sku(), line.productName(), line.quantity(), line.linePrice()
        ));
        System.out.printf(Locale.US, "Subtotal: %,.2f EGP%n", order.subtotal());
        System.out.printf(Locale.US, "VAT (14%%): %,.2f EGP%n", order.vat());
        System.out.printf(Locale.US, "Shipping: %,.2f EGP%n", order.shippingFee());
        System.out.printf(Locale.US, "TOTAL: %,.2f EGP%n", order.total());
        System.out.println("=====================================");
    }

    private ShoppingCart getCart(int cartNumber) {
        ShoppingCart cart = carts.get(cartNumber);
        if (cart == null) {
            throw new StoreException("Cart " + cartNumber + " does not exist.");
        }
        return cart;
    }

    private int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}