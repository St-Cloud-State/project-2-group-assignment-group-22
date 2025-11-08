import java.util.*;

public class ClerkMenuState extends UIState {

    private static ClerkMenuState instance;

    private ClerkMenuState() { }

    public static ClerkMenuState instance() {
        if (instance == null) {
            instance = new ClerkMenuState();
        }
        return instance;
    }

    @Override
    public void run() {
        int choice;
        do {
            displayMenu();
            choice = getUserChoice();
            processChoice(choice);
        } while (choice != 0);
    }

    private void displayMenu() {
        System.out.println("\n------ Clerk Menu ------");
        System.out.println("1. Add Client");
        System.out.println("2. Show Products");
        System.out.println("3. Show All Clients");
        System.out.println("4. Show Outstanding Balances");
        System.out.println("5. Record Payment");
        System.out.println("6. Become Client");
        System.out.println("0. Logout");
        System.out.print("Enter choice: ");
    }

    private int getUserChoice() {
        Scanner s = new Scanner(System.in);
        while (true) {
            try {
                return Integer.parseInt(s.nextLine());
            } catch (Exception e) {
                System.out.print("Invalid number. Enter again: ");
            }
        }
    }

    private void processChoice(int choice) {
        switch (choice) {

            case 1:
                addClient();
                break;

            case 2:
                showProducts();
                break;

            case 3:
                showClients();
                break;

            case 4:
                showOutstanding();
                break;

            case 5:
                recordPayment();
                break;

            case 6:
                becomeClient();
                break;

            case 0:
                logout();
                break;

            default:
                System.out.println("Invalid option.");
        }
    }

    @Override
    public void addClient() {
        Scanner s = new Scanner(System.in);
        System.out.print("Client name: ");
        String name = s.nextLine();
        System.out.print("Client address: ");
        String address = s.nextLine();
        System.out.print("Client phone: ");
        String phone = s.nextLine();

        Client c = warehouse.addClient(name, address, phone);
        if (c != null) {
            System.out.println("Client added. ID: " + c.getId());
        } else {
            System.out.println("Failed to add client.");
        }
    }

    @Override
    public void showProducts() {
        Iterator<Product> products = warehouse.getProducts();
        System.out.println("\n--- Product List ---");
        while (products.hasNext()) {
            Product p = products.next();
            System.out.println(p);
        }
    }

    @Override
    public void showClients() {
        Iterator<Client> clients = warehouse.getClients();
        System.out.println("\n--- Client List ---");
        while (clients.hasNext()) {
            System.out.println(clients.next());
        }
    }

    @Override
    public void showOutstanding() {
        Iterator<Client> clients = warehouse.getOutstandingClients();
        System.out.println("\n--- Outstanding Balances ---");
        while (clients.hasNext()) {
            System.out.println(clients.next());
        }
    }

    @Override
    public void recordPayment() {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter Client ID: ");
        String id = s.nextLine();
        System.out.print("Enter amount: ");
        double amt = s.nextDouble();

        boolean ok = warehouse.recordPayment(id, amt);
        if (ok) {
            System.out.println("Payment recorded.");
        } else {
            System.out.println("Could not record payment (invalid client?).");
        }
    }

    @Override
    public void becomeClient() {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter Client ID: ");
        String id = s.nextLine();

        if (warehouse.searchClient(id) != null) {
            context.setUser(id);
            context.changeState(UIContext.CLIENT_STATE);
            System.out.println("Switched to Client Mode.");
        } else {
            System.out.println("Invalid Client ID.");
        }
    }

    @Override
    public void logout() {
        System.out.println("Logging out...");
        context.changeState(UIContext.LOGIN_STATE);
    }
}
