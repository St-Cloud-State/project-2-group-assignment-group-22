import java.util.Iterator;
import java.util.Scanner;

public class ClerkMenuState implements State {

    private final Context context;
    private final Warehouse warehouse;

    public ClerkMenuState(Context context, Warehouse warehouse) {
        this.context = context;
        this.warehouse = warehouse;
    }

    @Override
    public void run() {
        Scanner in = context.getScanner();

        // showing main menu for clerk actions
        System.out.println("\n=== Clerk Menu ===");
        System.out.println("1) Add client");
        System.out.println("2) Show products (qty + price)");
        System.out.println("3) Show all clients");
        System.out.println("4) Show clients with outstanding balance");
        System.out.println("5) Record payment from client");
        System.out.println("6) Become client");
        System.out.println("0) Logout");
        System.out.print("Choice: ");

        String choice = in.nextLine().trim();

        // handling user input for different options
        switch (choice) {
             case "1":
                addClient();
                break;
            case "2":
                showProducts();
                break;
            case "3":
                showAllClients();
                break;
            case "4":
                showClientsWithBalance();
                break;
            case "5":
                recordPayment();
                break;
            case "6":
                becomeClient();
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }
    }

    private void addClient() {
        Scanner in = context.getScanner();
        // taking client details and adding to warehouse
        System.out.print("Name: ");
        String name = in.nextLine().trim();
        System.out.print("Address: ");
        String address = in.nextLine().trim();

        Client c = warehouse.addClient(name, address);
        // confirming result of add operation
        if (c != null) {
            System.out.println("Added client with ID: " + c.getId());
        } else {
            System.out.println("Failed to add client.");
        }
    }

    private void showProducts() {
        // listing all products with quantity and price
        Iterator<Product> products = warehouse.getProducts();
        while (products.hasNext()) {
            Product p = products.next();
            System.out.println(p);
        }
    }

    private void showAllClients() {
        // displaying all registered clients
        Iterator<Client> clients = warehouse.getClients();
        while (clients.hasNext()) {
            Client c = clients.next();
            System.out.println(c.getId() + " : " + c.getName()
                    + " | balance=" + c.getBalance());
        }
    }

    private void showClientsWithBalance() {
        // showing only clients who owe balance
        Iterator<Client> clients = warehouse.getClients();
        while (clients.hasNext()) {
            Client c = clients.next();
            if (c.getBalance() > 0) {
                System.out.println(c.getId() + " : " + c.getName()
                        + " | balance=" + c.getBalance());
            }
        }
    }

    private void recordPayment() {
        Scanner in = context.getScanner();

        System.out.print("Client ID or Name: ");
        String input = in.nextLine().trim();

        Client c = findClientByIdOrName(input);
        if (c == null) {
            System.out.println("Unknown client. Payment cancelled.");
            return; // back to menu, no crash
        }

        System.out.print("Amount: ");
        double amount;
        try {
            amount = Double.parseDouble(in.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Payment cancelled.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be positive. Payment cancelled.");
            return;
        }

        try {
            warehouse.recordPayment(c.getId(), amount); // always use canonical ID
            System.out.println("Payment of $" + amount + " recorded for client "
                    + c.getId() + " (" + c.getName() + ").");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private void becomeClient() {
        Scanner in = context.getScanner();
        System.out.print("Enter Client ID or Name to become: ");
        String input = in.nextLine().trim();

        Client c = findClientByIdOrName(input);
        if (c != null) {
            context.setCurrentClientId(c.getId(), Context.ST_CLERK_MENU);
            context.changeState(Context.EV_BECOME_CLIENT);
        } else {
            System.out.println("Invalid client ID/name.");
        }
    }

    private void logout() {
        // logging out and changing to previous state
        context.changeState(Context.EV_LOGOUT);
    }

    private Client findClientByIdOrName(String input) {
        String target = input.trim().toLowerCase();
        Iterator<Client> it = warehouse.getClients();

        while (it.hasNext()) {
            Client c = it.next();
            if (c.getId().equalsIgnoreCase(input)) {
                return c;
            }
            String name = c.getName();
            if (name != null && name.trim().toLowerCase().equals(target)) {
                return c;
            }
        }
        return null;
    }

}
