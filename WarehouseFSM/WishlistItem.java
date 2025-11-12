import java.io.Serializable;

public class WishlistItem implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String productId;
  private int qty;

  public WishlistItem(String productId, int qty) {
    if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
    this.productId = productId; this.qty = qty;
  }

  public String getProductId() { return productId; }
  public int getQty() { return qty; }
  public void setQty(int qty) {
    if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
    this.qty = qty;
  }

  @Override public String toString() {
    Product p = ProductList.instance().search(productId);
    String name = (p != null ? p.getName() : "?");
    return "WishlistItem{" + name + " " + productId + " x " + qty + "}";
  }
}
