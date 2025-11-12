import java.io.Serializable;

public class Product implements Serializable {
  private static final long serialVersionUID = 1L;

  private static int counter = 1;
  private final String id;
  private String name;
  private double price;
  private int stockQuantity;

  public Product(String name, double price, int stockQuantity) {
    this.id = "P" + counter++;
    this.name = name;
    this.price = price;
    this.stockQuantity = stockQuantity;
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public double getPrice() { return price; }
  public int getStockQuantity() { return stockQuantity; }

  public void setName(String name) { this.name = name; }
  public void setPrice(double price) { this.price = price; }
  public void setStockQuantity(int q) { this.stockQuantity = q; }

  /** Reducing stock by qty; returns actual fulfilled */
  public int fulfill(int qty) {
    int take = Math.min(stockQuantity, qty);
    stockQuantity -= take;
    return take;
  }

  /** Receiving qty into stock */
  public void receive(int qty) {
    if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
    stockQuantity += qty;
  }

  @Override
  public String toString() {
    return id + " | " + name + " | $" + price + " | qty=" + stockQuantity;
  }
}
 