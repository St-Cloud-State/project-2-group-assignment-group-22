import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class UserInterface {
  private static UserInterface ui;
  private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  private final Warehouse wh = Warehouse.instance();

  // Menu
  private static final int EXIT = 0;
  private static final int ADD_CLIENT = 1;
  private static final int SHOW_CLIENTS = 2;
  private static final int ADD_PRODUCT = 3;
  private static final int SHOW_PRODUCTS = 4;
  private static final int ADD_TO_WISHLIST = 5;
  private static final int SHOW_WISHLIST = 6;
  private static final int PLACE_ORDER = 7;
  private static final int SHOW_PRODUCT_WAITLIST = 8;
  private static final int RECEIVE_SHIPMENT = 9;
  private static final int RECORD_PAYMENT = 10;
  private static final int SHOW_INVOICES = 11;
  private static final int SHOW_CLIENT_WAITLIST = 12;


  private UserInterface() {}

  public static UserInterface instance() {
    if (ui == null) ui = new UserInterface();
    return ui;
  }

  // Low-level prompting helpers
  private String prompt(String s) {
    try {
      System.out.print(s + ": ");
      return reader.readLine();
    } catch (IOException e) { throw new RuntimeException(e); }
  }

  /** Menu safe integer: returns null if user types 'q' */
  private Integer intPrompt(String s) {
    while (true) {
      String input = prompt(s + " (or 'q' to quit)");
      if (input == null) return null;
      input = input.trim();
      if (input.equalsIgnoreCase("q")) return null;
      if (input.equalsIgnoreCase("help") || input.equalsIgnoreCase("menu")) return 99;

      try {
        return Integer.parseInt(input);
      } catch (NumberFormatException e) {
        System.out.println(" Error: Invalid input. Please enter a whole number or 'q' to quit.");
      }
    }
  }

  /** Money/price or general double: returns null if 'q' or EOF */
  private Double doublePrompt(String s) {
    while (true) {
      String input = prompt(s + " (or 'q' to quit)");
      if (input == null) return null;
      input = input.trim();
      if (input.equalsIgnoreCase("q")) return null;
      try {
        return Double.parseDouble(input);
      } catch (NumberFormatException e) {
        System.out.println("Error: Invalid input. Please enter a number like 12 or 12.5, or 'q' to quit.");
      }
    }
  }

  /** non empty string prompt; returns null on q */
  private String stringPrompt(String s) {
    while (true) {
      String in = prompt(s + " (or 'q' to quit)");
      if (in == null) return null;
      in = in.trim();
      if (in.equalsIgnoreCase("q")) return null;
      if (!in.isEmpty()) return in;
      System.out.println("Error: Please enter a non-empty value or 'q' to quit.");
    }
  }

  // id/Name resolution (accept names or ids)
  private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("(?i)^C\\d+$");
  private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("(?i)^P\\d+$");

  /** Resolving client input to an actual client id (accepts id or name, case-insensitive). Returns null if not found. */
  private String resolveClientId(String raw) {
    if (raw == null) return null;
    String s = raw.trim();
    if (s.isEmpty()) return null;

    // If it looks like an ID, normalizing case and check
    if (CLIENT_ID_PATTERN.matcher(s).matches()) {
      String id = "C" + s.replaceAll("(?i)^C", "");
      Client c = wh.findClient(id);
      if (c != null) return c.getId();
      return null;
    }

    // Otherwise, try by name (it is case-insensitive exact match)
    Iterator<Client> it = wh.getClients();
    while (it.hasNext()) {
      Client c = it.next();
      if (c.getName().equalsIgnoreCase(s)) return c.getId();
    }
    return null;
  }

  /** Resolving product input to product ID (accepts ID or name, case-insensitive). Returns null if not found. */
  private String resolveProductId(String raw) {
    if (raw == null) return null;
    String s = raw.trim();
    if (s.isEmpty()) return null;

    // If it looks like an ID (P#), normalize to uppercase P
    if (PRODUCT_ID_PATTERN.matcher(s).matches()) {
      String id = "P" + s.replaceAll("(?i)^P", "");
      Product p = wh.findProduct(id);
      if (p != null) return p.getId();
      return null;
    }

    // Otherwise, try by name
    Iterator<Product> it = wh.getProducts();
    while (it.hasNext()) {
      Product p = it.next();
      if (p.getName().equalsIgnoreCase(s)) return p.getId();
    }
    return null;
  }

  // Menu actions 
  private void addClient() {
    String name = stringPrompt("Client name");
    if (name == null) return;
    String addr = stringPrompt("Client address");
    if (addr == null) return;
    Client c = wh.addClient(name, addr);
    System.out.println("Added " + c);
  }

  private void showClients() {
    Iterator<Client> it = wh.getClients();
    System.out.println("=== Clients ===");
    while (it.hasNext()) System.out.println(it.next());
  }

  private void addProduct() {
    String name = stringPrompt("Product name");
    if (name == null) return;
    Double price = doublePrompt("Unit price");
    if (price == null) return;
    Integer qty = intPrompt("Starting quantity");
    if (qty == null) return;
    Product p = wh.addProduct(name, price, qty);
    System.out.println("Added " + p);
  }

  private void showProducts() {
    Iterator<Product> it = wh.getProducts();
    System.out.println("=== Products ===");
    while (it.hasNext()) System.out.println(it.next());
  }

  private void addToWishlist() {
    String cidRaw = stringPrompt("Client (ID like C1 or name like 'Sam')");
    if (cidRaw == null) return;
    String cid = resolveClientId(cidRaw);
    if (cid == null) {
      System.out.println("error: Client not found. Please enter a valid client ID (e.g., C1) or exact name.");
      return;
    }

    String pidRaw = stringPrompt("Product (ID like P1 or name like 'p1')");
    if (pidRaw == null) return;
    String pid = resolveProductId(pidRaw);
    if (pid == null) {
      System.out.println("error:  Product not found. Please enter a valid product ID (e.g., P1) or exact name.");
      return;
    }

    Integer qty = intPrompt("Quantity");
    if (qty == null) return;

    try {
      wh.addToWishlist(cid, pid, qty);
      System.out.println("Added to wishlist.");
    } catch (Exception e) {
      System.out.println("error: " + e.getMessage());
    }
  }

  private void showWishlist() {
    String cidRaw = stringPrompt("Client (ID like C1 or name)");
    if (cidRaw == null) return;
    String cid = resolveClientId(cidRaw);
    if (cid == null) {
      System.out.println("error:  Client not found.");
      return;
    }
    List<WishlistItem> items = wh.getWishlist(cid);
    System.out.println("=== Wishlist for " + cid + " ===");
    for (WishlistItem wi : items) System.out.println("  " + wi);
  }

  private void placeOrder() {
    String cidRaw = stringPrompt("Client (ID like C1 or name)");
    if (cidRaw == null) return;
    String cid = resolveClientId(cidRaw);
    if (cid == null) {
      System.out.println("error:  Client not found.");
      return;
    }
    Invoice inv = wh.placeOrderFromWishlist(cid);
    if (inv != null) {
      System.out.println("Order fulfilled now; invoice created:");
      System.out.println(inv);
    } else {
      System.out.println("Nothing fulfilled immediately (all went to waitlist or wishlist was empty).");
    }
  }

  private void showProductWaitlist() {
    String pidRaw = stringPrompt("Product (ID like P1 or name)");
    if (pidRaw == null) return;
    String pid = resolveProductId(pidRaw);
    if (pid == null) {
      System.out.println("error: Product not found.");
      return;
    }
    List<WaitlistItem> q = wh.getProductWaitlist(pid);
    System.out.println("=== Waitlist for " + pid + " ===");
    for (WaitlistItem wi : q) System.out.println("  " + wi);
  }

  private void receiveShipment() {
    String pidRaw = stringPrompt("Product (ID like P1 or name)");
    if (pidRaw == null) return;
    String pid = resolveProductId(pidRaw);
    if (pid == null) {
      System.out.println("error:  Product not found.");
      return;
    }
    Integer qty = intPrompt("Shipment quantity");
    if (qty == null) return;

    try {
      wh.receiveShipment(pid, qty);
      System.out.println("Shipment processed.");
    } catch (Exception e) {
      System.out.println("error: " + e.getMessage());
    }
  }

  private void recordPayment() {
    String cidRaw = stringPrompt("Client (ID like C1 or name)");
    if (cidRaw == null) return;
    String cid = resolveClientId(cidRaw);
    if (cid == null) {
      System.out.println("error:  Client not found.");
      return;
    }
    Double amt = doublePrompt("Payment amount");
    if (amt == null) return;

    try {
      wh.recordPayment(cid, amt);
      System.out.println("Payment recorded.");
    } catch (Exception e) {
      System.out.println(" error: " + e.getMessage());
    }
  }

  private void showInvoices() {
    String cidRaw = stringPrompt("Client (ID like C1 or name)");
    if (cidRaw == null) return;
    String cid = resolveClientId(cidRaw);
    if (cid == null) {
      System.out.println("Error: Client not found.");
      return;
    }
    Client c = wh.findClient(cid);
    if (c == null) { System.out.println("Client not found"); return; }
    System.out.println("=== Invoices for " + cid + " ===");
    for (Invoice inv : c.getInvoices()) {
      System.out.println(inv);
      System.out.println();
    }
  }

  private void showClientWaitlist() {
  String cidRaw = stringPrompt("Client (ID like C1 or name)");
  if (cidRaw == null) return;
  String cid = resolveClientId(cidRaw);
  if (cid == null) {
    System.out.println("Error: Client not found.");
    return;
  }

  List<WaitlistItem> items = wh.getClientWaitlist(cid);
  System.out.println("=== Waitlist items for " + cid + " ===");
  if (items.isEmpty()) {
    System.out.println("No pending waitlist items.");
  } else {
    for (WaitlistItem wi : items) System.out.println("  " + wi);
  }
}


  private void help() {
    System.out.println("\n--- MENU ---");
    System.out.println(ADD_CLIENT + ". Add client");
    System.out.println(SHOW_CLIENTS + ". Show clients");
    System.out.println(ADD_PRODUCT + ". Add product");
    System.out.println(SHOW_PRODUCTS + ". Show products");
    System.out.println(ADD_TO_WISHLIST + ". Add to wishlist");
    System.out.println(SHOW_WISHLIST + ". Show a client's wishlist");
    System.out.println(PLACE_ORDER + ". Place order from wishlist");
    System.out.println(SHOW_PRODUCT_WAITLIST + ". Show waitlist for product");
    System.out.println(RECEIVE_SHIPMENT + ". Receive shipment for product");
    System.out.println(RECORD_PAYMENT + ". Record payment for client");
    System.out.println(SHOW_INVOICES + ". Show invoices for client");
    System.out.println(SHOW_CLIENT_WAITLIST + ". Show waitlist for client");

    System.out.println(EXIT + ". Exit\n");
  }

  // Main menu loop (robust, accepts 'q' to quit)
  public void process() {
    help(); // displaying initial menu

    while (true) {
      Integer cmd = intPrompt("Enter choice (help shows menu; 0 or 'q' to exit)");
      if (cmd == null) { // user typed 'q' or Ctrl+D
        System.out.println("Exiting...");
        return;
      }

      if (cmd == 99) { help(); continue; }

      switch (cmd) {
        case EXIT:
          System.out.println("Exiting...");
          return;

        case ADD_CLIENT:
          addClient();
          break;

        case SHOW_CLIENTS:
          showClients();
          break;

        case ADD_PRODUCT:
          addProduct();
          break;

        case SHOW_PRODUCTS:
          showProducts();
          break;

        case ADD_TO_WISHLIST:
          addToWishlist();
          break;

        case SHOW_WISHLIST:
          showWishlist();
          break;

        case PLACE_ORDER:
          placeOrder();
          break;

        case SHOW_PRODUCT_WAITLIST:
          showProductWaitlist();
          break;

        case RECEIVE_SHIPMENT:
          receiveShipment();
          break;

        case RECORD_PAYMENT:
          recordPayment();
          break;

        case SHOW_INVOICES:
          showInvoices();
          break;
        case SHOW_CLIENT_WAITLIST:
          showClientWaitlist();
          break;


        default:
          System.out.println(" nknown option. Type 99 for menu or 'q' to quit.");
      }
    }
  }

  public static void main(String[] args) { UserInterface.instance().process(); }
}
