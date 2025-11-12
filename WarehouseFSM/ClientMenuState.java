import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ClientMenuState implements State {

    private final Context context;
    private final Warehouse warehouse;

    public ClientMenuState(Context context, Warehouse warehouse) {
        this.context = context;
        this.warehouse = warehouse;
    }

    @Override
    public void run() {
        String clientId = context.getCurrentClientId();
        if (clientId == null) {
            // handling missing active client and returning to opening
            System.out.println("No active client. Returning to opening menu.");
            context.changeState(Context.EV_LOGOUT);
            return;
        }

        Scanner in = context.getScanner();

        // showing client-specific menu options
        System.out.println("\n=== Client Menu (Client " + clientId + ") ===");
        System.out.println("1) Show client details");
        System.out.println("2) Show list of products (with price)");
        System.out.println("3) Show client transactions");
        System.out.println("4) Add item to wishlist");
        System.out.println("5) Display wishlist");
        System.out.println("6) Place an order");
        System.out.println("7) Show waitlisted items");
        System.out.println("0) Logout");
        System.out.print("Choice: ");

        String choice = in.nextLine().trim();

        // routing choice to corresponding client action
       switch (choice) {
            case "1":
                showClientDetails();
                break;
            case "2":
                showProducts();
                break;
            case "3":
                showTransactions();
                break;
            case "4":
                addToWishlist();
                break;
            case "5":
                showWishlist();
                break;
            case "6":
                placeOrder();
                break;
            case "7":
                showWaitlistedItems();
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }

    }

    private void showClientDetails() {
        Client c = findClientById(context.getCurrentClientId());
        if (c == null) {
            System.out.println("Client not found.");
            return;
        }
        // displaying basic client profile info
        System.out.println("Client ID: " + c.getId());
        System.out.println("Name: " + c.getName());
        System.out.println("Address: " + c.getAddress());
        System.out.println("Balance: " + c.getBalance());
    }

    private void showProducts() {
        // listing all products with price for browsing
        Iterator<Product> products = warehouse.getProducts();
        while (products.hasNext()) {
            Product p = products.next();
            System.out.println(p.getId() + " : " + p.getName()
                    + " | price $" + p.getPrice());
        }
    }

    private void showTransactions() {
        Client c = findClientById(context.getCurrentClientId());
        if (c == null) {
            System.out.println("Client not found.");
            return;
        }

        // showing client invoice / transaction history
        List<Invoice> invoices = c.getInvoices();
        if (invoices == null || invoices.isEmpty()) {
            System.out.println("No transactions for this client.");
            return;
        }
        for (Invoice inv : invoices) {
            System.out.println(inv);
        }
    }

    private void addToWishlist() {
        Scanner in = context.getScanner();
        String clientId = context.getCurrentClientId();

        Client c = warehouse.findClient(clientId);
        if (c == null) {
            System.out.println("Client not found.");
            return;
        }

        System.out.print("Enter Product ID or Name: ");
        String input = in.nextLine().trim();

        Product p = findProductByIdOrName(input);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.print("Enter quantity: ");
        int qty;
        try {
            qty = Integer.parseInt(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }
        if (qty <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        try {
            warehouse.addToWishlist(clientId, p.getId(), qty);
            System.out.println("Added " + qty + " of " + p.getName()
                    + " (ID " + p.getId() + ") to wishlist for client " + clientId + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }




    private void showWishlist() {
        String clientId = context.getCurrentClientId();
        Client c = warehouse.findClient(clientId);
        if (c == null) {
            System.out.println("Client not found.");
            return;
        }

        // Using Warehouse.getWishlist(clientId) -> List<WishlistItem>
        java.util.List<WishlistItem> items = warehouse.getWishlist(clientId);
        if (items == null || items.isEmpty()) {
            System.out.println("Wishlist is empty.");
            return;
        }

        System.out.println("Wishlist for client " + clientId + ":");
        for (WishlistItem wi : items) {
            Product p = warehouse.findProduct(wi.getProductId());
            String name = (p != null) ? p.getName() : "?";
            System.out.println("- " + wi.getProductId()
                    + " (" + name + ") x " + wi.getQty());
        }
    }



    private void placeOrder() {
        String clientId = context.getCurrentClientId();
        Client c = warehouse.findClient(clientId);
        if (c == null) {
            System.out.println("Client not found.");
            return;
        }

        try {
            
            Invoice inv = warehouse.placeOrderFromWishlist(clientId);

            if (inv == null) {
                System.out.println("Nothing could be fulfilled from wishlist "
                        + "(wishlist empty or all items waitlisted).");
            } else {
                System.out.println("Order placed for client " + clientId + ".");
                System.out.println("Invoice details:");
                System.out.println(inv);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error placing order: " + e.getMessage());
        }
    }



    private void logout() {
        // clearing active client and returning to previous state
        context.clearCurrentClient();
        context.changeState(Context.EV_LOGOUT);
    }

    // finding client by id from warehouse
    private Client findClientById(String id) {
        Iterator<Client> clients = warehouse.getClients();
        while (clients.hasNext()) {
            Client c = clients.next();
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    // finding product by id or name from warehouse
   private Product findProductByIdOrName(String input) {
        String target = input.trim().toLowerCase();
        java.util.Iterator<Product> it = warehouse.getProducts();

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

    private void showWaitlistedItems() {
        String clientId = context.getCurrentClientId();
        Client c = warehouse.findClient(clientId);
        if (c == null) {
            System.out.println("Client not found.");
            return;
        }

        java.util.List<WaitlistItem> items = warehouse.getClientWaitlist(clientId);
        if (items == null || items.isEmpty()) {
            System.out.println("No outstanding waitlisted items for client " + clientId + ".");
            return;
        }

        System.out.println("Waitlisted items for client " + clientId + ":");
        for (WaitlistItem wi : items) {
            Product p = warehouse.findProduct(wi.getProductId());
            String name = (p != null) ? p.getName() : "?";
            System.out.println("- " + wi.getProductId()
                    + " (" + name + ") x " + wi.getQty()
                    + " (requested " + wi.getRequestedAt() + ")");
        }
    }


}
