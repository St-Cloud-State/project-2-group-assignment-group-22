import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Client implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final String CLIENT_PREFIX = "C";
  private final String id;
  private String name;
  private String address;
  private final Wishlist wishlist = new Wishlist();
  private final List<Invoice> invoices = new ArrayList<>();
  private double balance; // positive => client owes

  public Client(String name, String address, int idNum) {
    this.name = name;
    this.address = address;
    this.id = CLIENT_PREFIX + idNum;
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public String getAddress() { return address; }
  public double getBalance() { return balance; }
  public Wishlist getWishlist() { return wishlist; }
  public List<Invoice> getInvoices() { return invoices; }

  public void setName(String newName) { this.name = newName; }
  public void setAddress(String newAddress) { this.address = newAddress; }

  public void addInvoice(Invoice inv) {
    invoices.add(inv);
    balance += inv.getTotal();
  }

  public void recordPayment(double amount) {
    if (amount <= 0) throw new IllegalArgumentException("Payment must be positive");
    balance -= amount;
  }

  @Override
  public String toString() {
    return "Client " + id + " | " + name + " | " + address +
           " | Balance: $" + String.format("%.2f", balance);
  }
}
