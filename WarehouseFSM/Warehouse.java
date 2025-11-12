import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Warehouse implements Serializable {
  private static final long serialVersionUID = 1L;

  private static Warehouse warehouse;
  private final ClientList clientList;
  private final ProductList productList;
  private final Waitlist waitlist = new Waitlist();
  private int clientIdCounter = 1;

  // initializing lists
  private Warehouse() {
    clientList = ClientList.instance();
    productList = ProductList.instance();
  }

  // creating singleton instance
  public static Warehouse instance() {
    if (warehouse == null) warehouse = new Warehouse();
    return warehouse;
  }

  // adding client to client list
  public Client addClient(String name, String address) {
    Client c = new Client(name, address, clientIdCounter++);
    clientList.insertClient(c);
    return c;
  }

  // getting iterator of all clients
  public Iterator<Client> getClients() { return clientList.getClients(); }

  // finding client by id
  public Client findClient(String id) { return clientList.search(id); }

  // recording payment from client
  public void recordPayment(String clientId, double amount) {
    Client c = findClient(clientId);
    if (c == null) throw new IllegalArgumentException("Client not found: " + clientId);
    c.recordPayment(amount);
  }

  // adding product to product list
  public Product addProduct(String name, double price, int qty) {
    Product p = new Product(name, price, qty);
    productList.insertProduct(p);
    return p;
  }

  // getting iterator of all products
  public Iterator<Product> getProducts() { return productList.getProducts(); }

  // finding product by id
  public Product findProduct(String id) { return productList.search(id); }

  // adding product to client's wishlist
  public void addToWishlist(String clientId, String productId, int qty) {
    Client c = findClient(clientId);
    Product p = findProduct(productId);
    if (c == null || p == null) throw new IllegalArgumentException("Bad client or product id");
    c.getWishlist().add(productId, qty);
  }

  // getting wishlist of client
  public List<WishlistItem> getWishlist(String clientId) {
    Client c = findClient(clientId);
    if (c == null) return Collections.emptyList();
    return c.getWishlist().getItems();
  }

  // placing order from wishlist and processing fulfillment and waitlist
  public Invoice placeOrderFromWishlist(String clientId) {
    Client c = findClient(clientId);
    if (c == null) throw new IllegalArgumentException("Client not found: " + clientId);
    if (c.getWishlist().isEmpty()) return null;

    Invoice invoice = new Invoice(clientId);

    // iterating through wishlist items
    for (WishlistItem wi : c.getWishlist().getItems()) {
      Product p = findProduct(wi.getProductId());
      if (p == null) continue;

      int want = wi.getQty();
      int got = p.fulfill(want);

      // adding fulfilled items to invoice
      if (got > 0) {
        invoice.addLine(p.getId(), p.getName(), got, p.getPrice());
      }

      // adding unfulfilled items to waitlist
      int shortfall = want - got;
      if (shortfall > 0) {
        waitlist.add(p.getId(), shortfall, clientId);
      }
    }

    // clearing wishlist after processing
    c.getWishlist().clear();

    // adding invoice only if something was fulfilled
    if (invoice.getTotal() > 0) {
      c.addInvoice(invoice);
      return invoice;
    } else {
      return null;
    }
  }

  // receiving shipment and processing waitlist fulfillment
  public void receiveShipment(String productId, int qty) {
    Product p = findProduct(productId);
    if (p == null) throw new IllegalArgumentException("Product not found: " + productId);
    if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");

    // adding received quantity to stock
    p.receive(qty);

    // filling waitlist FIFO while stock is available
    Deque<WaitlistItem> q = waitlist.queueFor(productId);
    if (q == null) return;

    while (!q.isEmpty() && p.getStockQuantity() > 0) {
      WaitlistItem head = q.peekFirst();
      int can = Math.min(p.getStockQuantity(), head.getQty());
      if (can <= 0) break;

      // fulfilling entire waitlist item
      if (can == head.getQty()) {
        p.fulfill(can);
        Client c = findClient(head.getClientId());
        if (c != null) {
          Invoice inv = new Invoice(c.getId());
          inv.addLine(p.getId(), p.getName(), can, p.getPrice());
          c.addInvoice(inv);
        }
        q.removeFirst();
      } else {
        // stopping if not enough to fulfill next waitlist item
        break;
      }
    }
  }

  // getting waitlist by product
  public List<WaitlistItem> getProductWaitlist(String productId) {
    Deque<WaitlistItem> q = waitlist.queueFor(productId);
    return (q == null) ? Collections.emptyList() : new ArrayList<>(q);
  }

  // getting waitlist by client
  public List<WaitlistItem> getClientWaitlist(String clientId) {
    return waitlist.getClientWaitlist(clientId);
  }
}
