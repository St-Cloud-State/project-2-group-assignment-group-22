import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ManagerMenuState implements State {

    private final Context context;
    private final Warehouse warehouse;

    public ManagerMenuState(Context context, Warehouse warehouse) {
        this.context = context;
        this.warehouse = warehouse;
    }

    @Override
    public void run() {
        Scanner in = context.getScanner();

        System.out.println("\n=== Manager Menu ===");
        System.out.println("1) Add product");
        System.out.println("2) Display waitlist for a product");
        System.out.println("3) Receive shipment");
        System.out.println("4) Become clerk");
        System.out.println("0) Logout");
        System.out.print("Choice: ");

        String choice = in.nextLine().trim();

        switch (choice) {
            case "1":
                addProduct();
                break;
            case "2":
                showWaitlistForProduct();
                break;
            case "3":
                receiveShipment();
                break;
            case "4":
                becomeClerk();
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }
    }

    private void addProduct() {
        Scanner in = context.getScanner();

        System.out.print("Product name: ");
        String name = in.nextLine().trim();

        System.out.print("Unit price: ");
        double price;
        try {
            price = Double.parseDouble(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price. Product not added.");
            return;
        }

        System.out.print("Initial quantity: ");
        int qty;
        try {
            qty = Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity. Product not added.");
            return;
        }

        if (qty < 0) {
            System.out.println("Quantity must be non-negative. Product not added.");
            return;
        }

        Product p = warehouse.addProduct(name, price, qty);
        if (p != null) {
            System.out.println("Added product with ID: " + p.getId());
        } else {
            System.out.println("Failed to add product.");
        }
    }

    // ID or Name - show waitlist entries for that product
    private void showWaitlistForProduct() {
        Scanner in = context.getScanner();
        System.out.print("Product ID or Name: ");
        String input = in.nextLine().trim();

        Product p = findProductByIdOrName(input);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }

        String pid = p.getId();
        List<WaitlistItem> items = warehouse.getProductWaitlist(pid);
        if (items == null || items.isEmpty()) {
            System.out.println("No waitlist entries for product " + pid + ".");
            return;
        }

        System.out.println("Waitlist for " + p.getName() + " (" + pid + "):");
        for (WaitlistItem wi : items) {
            System.out.println("- Client " + wi.getClientId()
                    + " waiting for " + wi.getQty()
                    + " (requested " + wi.getRequestedAt() + ")");
        }
    }

    // ID or Name - receiving shipment safely
    private void receiveShipment() {
        Scanner in = context.getScanner();
        System.out.print("Product ID or Name: ");
        String input = in.nextLine().trim();

        Product p = findProductByIdOrName(input);
        if (p == null) {
            System.out.println("Product not found. Shipment cancelled.");
            return;
        }

        System.out.print("Quantity received: ");
        int qty;
        try {
            qty = Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity. Shipment cancelled.");
            return;
        }

        if (qty <= 0) {
            System.out.println("Quantity must be positive. Shipment cancelled.");
            return;
        }

        try {
            warehouse.receiveShipment(p.getId(), qty); // always use canonical ID
            System.out.println("Shipment of " + qty + " units received for "
                    + p.getName() + " (" + p.getId() + ").");
        } catch (IllegalArgumentException e) {
            // backend threw because of bad data; don't crash UI
            System.out.println("Error receiving shipment: " + e.getMessage());
        }
    }

    private void becomeClerk() {
        context.changeState(Context.EV_BECOME_CLERK);
    }

    private void logout() {
        context.changeState(Context.EV_LOGOUT);
    }

    // finding product by ID or name 
    private Product findProductByIdOrName(String input) {
        String target = input.trim().toLowerCase();
        Iterator<Product> it = warehouse.getProducts();

        while (it.hasNext()) {
            Product p = it.next();
            if (p.getId().equalsIgnoreCase(input)) {
                return p;
            }
            String name = p.getName();
            if (name != null && name.trim().toLowerCase().equals(target)) {
                return p;
            }
        }
        return null;
    }
}
