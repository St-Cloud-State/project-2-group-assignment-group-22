import java.util.Iterator;
import java.util.Scanner;

public class OpeningState implements State {

    private final Context context;
    private final Warehouse warehouse;

    public OpeningState(Context context, Warehouse warehouse) {
        this.context = context;
        this.warehouse = warehouse;
    }

    @Override
    public void run() {
        Scanner in = context.getScanner();

        // showing opening menu for login choices
        System.out.println("\n=== Opening Menu ===");
        System.out.println("1) Login as Client");
        System.out.println("2) Login as Clerk");
        System.out.println("3) Login as Manager");
        System.out.println("0) Exit");
        System.out.print("Choice: ");

        String choice = in.nextLine().trim();

        // handling user selection and routing to proper state
        switch (choice) {
            case "1":
                clientLogin();
                break;
            case "2":
                clerkLogin();
                break;
            case "3":
                managerLogin();
                break;
            case "0":
                context.stop();
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }
    }

    private void clientLogin() {
        Scanner in = context.getScanner();
        // taking client id or name and verifying
        System.out.print("Enter Client ID or Name: ");
        String input = in.nextLine().trim();

        Client c = findClientByIdOrName(input);
        if (c != null) {
            // setting current client and switching to client state
            context.setCurrentClientId(c.getId(), Context.ST_OPENING);
            context.changeState(Context.EV_LOGIN_CLIENT);
        } else {
            System.out.println("Unknown client. Please check ID/name.");
        }
    }

    // finding client using id or name (case-insensitive)
    private Client findClientByIdOrName(String input) {
        Iterator<Client> clients = warehouse.getClients();
        String target = input.trim().toLowerCase();

        while (clients.hasNext()) {
            Client c = clients.next();
            if (c.getId().equalsIgnoreCase(input)) {
                return c;
            }
            if (c.getName() != null && c.getName().trim().toLowerCase().equals(target)) {
                return c;
            }
        }
        return null;
    }

    private void clerkLogin() {
        // switching to clerk menu state
        context.changeState(Context.EV_LOGIN_CLERK);
    }

    private void managerLogin() {
        // switching to manager menu state
        context.changeState(Context.EV_LOGIN_MANAGER);
    }

    
}
