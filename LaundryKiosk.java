import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LaundryKiosk {

    // ─── Constants ────────────────────────────────────────────────
    static final String GCASH_NUMBER = "0917-123-4567";
    static final String BUSINESS_NAME = "QuickWash Laundry Kiosk";

    // Pricing
    static final double WASH_ONLY_RATE    = 40.0;  // per kg
    static final double DRY_ONLY_RATE     = 30.0;  // per kg
    static final double FULL_SERVICE_RATE = 65.0;  // per kg

    // Add-on prices
    static final Map<String, Double> ADDON_PRICES = new LinkedHashMap<>() {{
        put("Fabric Conditioner", 20.0);
        put("Detergent Upgrade",  15.0);
        put("Express Drying",     30.0);
        put("Stain Removal",      25.0);
        put("Ironing",            50.0);
    }};

    // ─── State ────────────────────────────────────────────────────
    static int orderCounter = 1001;
    static List<Order> orders = new ArrayList<>();
    static Set<String> usedReferenceNumbers = new HashSet<>();
    static Scanner scanner = new Scanner(System.in);

    // ─── Main ─────────────────────────────────────────────────────
    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> createOrder();
                case 2 -> viewOrders();
                case 3 -> processPayment();
                case 4 -> viewReceipt();
                case 5 -> { System.out.println("\n  Thank you for using " + BUSINESS_NAME + "! Goodbye!\n"); running = false; }
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    // ─── Menu Helpers ─────────────────────────────────────────────
    static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║        " + BUSINESS_NAME + "        ║");
        System.out.println("  ║      GCash Payment System v1.0           ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        System.out.println();
    }

    static void printMainMenu() {
        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.println("  │            MAIN MENU                │");
        System.out.println("  ├─────────────────────────────────────┤");
        System.out.println("  │  1. Create New Order                │");
        System.out.println("  │  2. View All Orders                 │");
        System.out.println("  │  3. Process GCash Payment           │");
        System.out.println("  │  4. Print Receipt                   │");
        System.out.println("  │  5. Exit                            │");
        System.out.println("  └─────────────────────────────────────┘");
    }

    // ─── 1. Create Order ──────────────────────────────────────────
    static void createOrder() {
        System.out.println("\n  ════════ NEW ORDER ════════");

        // Customer name
        System.out.print("  Customer Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  [!] Name cannot be empty."); return; }

        // Service type
        System.out.println("\n  Select Service:");
        System.out.println("    1. Wash Only     (PHP " + WASH_ONLY_RATE + "/kg)");
        System.out.println("    2. Dry Only      (PHP " + DRY_ONLY_RATE + "/kg)");
        System.out.println("    3. Full Service  (PHP " + FULL_SERVICE_RATE + "/kg)");
        int serviceChoice = readInt("  Choice: ");
        String serviceType;
        double ratePerKg;
        switch (serviceChoice) {
            case 1 -> { serviceType = "Wash Only";    ratePerKg = WASH_ONLY_RATE; }
            case 2 -> { serviceType = "Dry Only";     ratePerKg = DRY_ONLY_RATE; }
            case 3 -> { serviceType = "Full Service"; ratePerKg = FULL_SERVICE_RATE; }
            default -> { System.out.println("  [!] Invalid service choice."); return; }
        }

        // Weight
        double weight = readDouble("  Enter weight (kg): ");
        if (weight <= 0) { System.out.println("  [!] Weight must be greater than 0."); return; }

        // Add-ons
        List<String> selectedAddons = new ArrayList<>();
        double addonTotal = 0;
        System.out.println("\n  Available Add-ons (enter 0 to finish):");
        List<String> addonList = new ArrayList<>(ADDON_PRICES.keySet());
        for (int i = 0; i < addonList.size(); i++) {
            String addon = addonList.get(i);
            System.out.printf("    %d. %-22s PHP %.0f%n", i + 1, addon, ADDON_PRICES.get(addon));
        }
        System.out.println("    0. Done selecting add-ons");
        while (true) {
            int addonChoice = readInt("  Select add-on: ");
            if (addonChoice == 0) break;
            if (addonChoice < 1 || addonChoice > addonList.size()) {
                System.out.println("  [!] Invalid add-on choice.");
                continue;
            }
            String chosen = addonList.get(addonChoice - 1);
            if (selectedAddons.contains(chosen)) {
                System.out.println("  [!] Already added: " + chosen);
            } else {
                selectedAddons.add(chosen);
                addonTotal += ADDON_PRICES.get(chosen);
                System.out.println("  [+] Added: " + chosen);
            }
        }

        // Compute total
        double serviceTotal = ratePerKg * weight;
        double total = serviceTotal + addonTotal;

        // Create order
        String orderId = "ORD" + orderCounter++;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Order order = new Order(orderId, name, serviceType, weight, selectedAddons, total, timestamp);
        orders.add(order);

        // Summary
        System.out.println("\n  ──────────────────────────────────────");
        System.out.println("  ORDER CREATED SUCCESSFULLY");
        System.out.println("  ──────────────────────────────────────");
        System.out.printf("  Order ID    : %s%n", orderId);
        System.out.printf("  Customer    : %s%n", name);
        System.out.printf("  Service     : %s%n", serviceType);
        System.out.printf("  Weight      : %.1f kg%n", weight);
        System.out.printf("  Add-ons     : %s%n", selectedAddons.isEmpty() ? "None" : String.join(", ", selectedAddons));
        System.out.printf("  TOTAL       : PHP %.2f%n", total);
        System.out.println("  ──────────────────────────────────────");
        System.out.println("  Proceed to Option 3 to complete payment.\n");
    }

    // ─── 2. View Orders ───────────────────────────────────────────
    static void viewOrders() {
        System.out.println("\n  ════════ ALL ORDERS ════════");
        if (orders.isEmpty()) {
            System.out.println("  No orders found.\n");
            return;
        }
        System.out.printf("  %-10s %-20s %-14s %8s %-10s%n",
                "Order ID", "Customer", "Service", "Total", "Status");
        System.out.println("  " + "─".repeat(68));
        for (Order o : orders) {
            System.out.printf("  %-10s %-20s %-14s %8.2f %-10s%n",
                    o.orderId, truncate(o.customerName, 19), o.serviceType, o.total, o.status);
        }
        System.out.println();
    }

    // ─── 3. Process GCash Payment ─────────────────────────────────
    static void processPayment() {
        System.out.println("\n  ════════ GCASH PAYMENT ════════");

        // Show pending orders
        List<Order> pending = orders.stream().filter(o -> o.status.equals("Pending")).toList();
        if (pending.isEmpty()) {
            System.out.println("  No pending orders to pay.\n");
            return;
        }

        System.out.println("  Pending Orders:");
        for (Order o : pending) {
            System.out.printf("    %-10s | %-20s | PHP %.2f%n", o.orderId, o.customerName, o.total);
        }

        // Select order
        System.out.print("\n  Enter Order ID to pay: ");
        String orderId = scanner.nextLine().trim().toUpperCase();
        Order order = findOrder(orderId);
        if (order == null) {
            System.out.println("  [!] Order not found: " + orderId);
            return;
        }
        if (!order.status.equals("Pending")) {
            System.out.println("  [!] This order is already " + order.status + ".");
            return;
        }

        // Display GCash payment instructions
        System.out.println();
        System.out.println("  ┌────────────────────────────────────────┐");
        System.out.println("  │         GCASH PAYMENT DETAILS          │");
        System.out.println("  ├────────────────────────────────────────┤");
        System.out.printf("  │  GCash Number : %-23s│%n", GCASH_NUMBER);
        System.out.printf("  │  Amount to Pay: PHP %-19.2f│%n", order.total);
        System.out.printf("  │  Order ID     : %-23s1│%n", order.orderId);
        System.out.println("  ├────────────────────────────────────────┤");
        System.out.println("  │  1. Open GCash app                     │");
        System.out.println("  │  2. Send PHP to the number above       │");
        System.out.println("  │  3. Enter the reference no. below      │");
        System.out.println("  └────────────────────────────────────────┘");

        // Reference number input
        System.out.println();
        String refNo = "";
        while (true) {
            System.out.print("  Enter GCash Reference Number: ");
            refNo = scanner.nextLine().trim();

            String validationMsg = validateReferenceNumber(refNo);
            if (validationMsg != null) {
                System.out.println("  [!] " + validationMsg);
                System.out.println("  [!] Please enter a valid reference number to continue.");
            } else {
                break;
            }
        }

        // Screenshot filename input
        String screenshotFile = "";
        while (true) {
            System.out.print("  Enter Screenshot Filename (e.g. payment_proof.jpg): ");
            screenshotFile = scanner.nextLine().trim();

            if (screenshotFile.isEmpty()) {
                System.out.println("  [!] Screenshot filename cannot be empty. Please provide proof of payment.");
            } else if (!isValidFilename(screenshotFile)) {
                System.out.println("  [!] Invalid filename format. Use letters, numbers, underscores, hyphens, and a valid extension (e.g. .jpg, .png, .pdf).");
            } else {
                break;
            }
        }

        // Simulate verification
        System.out.println("\n  [~] Verifying payment...");
        simulateDelay(1500);
        System.out.println("  [~] Validating reference number...");
        simulateDelay(1000);
        System.out.println("  [~] Checking screenshot submission...");
        simulateDelay(800);
        System.out.println("  [✓] Payment verified successfully!");

        // Update order
        usedReferenceNumbers.add(refNo);
        order.gcashRefNo = refNo;
        order.screenshotFile = screenshotFile;
        order.status = "Paid";
        order.paidAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("\n  ══════════════════════════════════════════");
        System.out.println("  ✅  PAYMENT SUCCESSFUL");
        System.out.printf("  Order %-8s marked as PAID.%n", order.orderId);
        System.out.println("  Proceed to Option 4 to print your receipt.");
        System.out.println("  ══════════════════════════════════════════\n");
    }

    // ─── 4. Print Receipt ─────────────────────────────────────────
    static void viewReceipt() {
        System.out.println("\n  ════════ PRINT RECEIPT ════════");
        if (orders.isEmpty()) {
            System.out.println("  No orders found.\n");
            return;
        }

        System.out.print("  Enter Order ID: ");
        String orderId = scanner.nextLine().trim().toUpperCase();
        Order order = findOrder(orderId);
        if (order == null) {
            System.out.println("  [!] Order not found.\n");
            return;
        }

        printReceipt(order);
    }

    static void printReceipt(Order order) {
        System.out.println();
        System.out.println("  =================================");
        System.out.println("          LAUNDRY RECEIPT");
        System.out.println("       " + BUSINESS_NAME);
        System.out.println("  =================================");
        System.out.printf("  Order ID    : %s%n", order.orderId);
        System.out.printf("  Date        : %s%n", order.createdAt);
        System.out.printf("  Customer    : %s%n", order.customerName);
        System.out.printf("  Service     : %s%n", order.serviceType);
        System.out.printf("  Weight      : %.1f kg%n", order.weight);
        System.out.printf("  Add-ons     : %s%n", order.addons.isEmpty() ? "None" : String.join(", ", order.addons));
        System.out.println("  ---------------------------------");
        System.out.printf("  Total       : PHP %.2f%n", order.total);
        System.out.println("  ---------------------------------");

        if (!order.status.equals("Pending")) {
            System.out.printf("  GCash Ref No: %s%n", order.gcashRefNo);
            System.out.printf("  Pay. Proof  : %s%n", order.screenshotFile);
            System.out.printf("  Paid At     : %s%n", order.paidAt);
        } else {
            System.out.println("  GCash Ref No: Not yet paid");
            System.out.println("  Pay. Proof  : Not yet submitted");
        }

        System.out.println("  ---------------------------------");
        System.out.printf("  Status      : %s%n", order.status);
        System.out.println("  =================================");
        System.out.println("     Thank you for your business!");
        System.out.println("       Please come again soon.");
        System.out.println("  =================================\n");
    }

    // ─── Validation Helpers ───────────────────────────────────────

    static String validateReferenceNumber(String refNo) {
        if (refNo == null || refNo.isEmpty()) {
            return "Reference number cannot be empty.";
        }
        // Must be 10-13 digits (GCash format)
        if (!refNo.matches("\\d{10,13}")) {
            return "Invalid format. GCash reference numbers must be 10–13 digits (numbers only). Got: \"" + refNo + "\"";
        }
        if (usedReferenceNumbers.contains(refNo)) {
            return "Duplicate reference number. This reference number has already been used.";
        }
        return null; // valid
    }

    static boolean isValidFilename(String filename) {
        // Must have a valid extension and no path separators
        return filename.matches("[\\w\\-. ]+\\.(jpg|jpeg|png|pdf|bmp|gif|webp)") &&
               !filename.contains("/") && !filename.contains("\\");
    }

    // ─── Utilities ────────────────────────────────────────────────
    static Order findOrder(String orderId) {
        return orders.stream().filter(o -> o.orderId.equalsIgnoreCase(orderId)).findFirst().orElse(null);
    }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try { return Integer.parseInt(line); }
            catch (NumberFormatException e) { System.out.println("  [!] Please enter a valid number."); }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try { return Double.parseDouble(line); }
            catch (NumberFormatException e) { System.out.println("  [!] Please enter a valid number."); }
        }
    }

    static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    static void simulateDelay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // ─── Order Model ──────────────────────────────────────────────
    static class Order {
        String orderId;
        String customerName;
        String serviceType;
        double weight;
        List<String> addons;
        double total;
        String status;
        String gcashRefNo;
        String screenshotFile;
        String createdAt;
        String paidAt;

        Order(String orderId, String customerName, String serviceType,
              double weight, List<String> addons, double total, String createdAt) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.serviceType = serviceType;
            this.weight = weight;
            this.addons = addons;
            this.total = total;
            this.createdAt = createdAt;
            this.status = "Pending";
            this.gcashRefNo = "";
            this.screenshotFile = "";
            this.paidAt = "";
        }
    }
}
