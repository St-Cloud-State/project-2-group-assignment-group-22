import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// storing wishlist items per client (productId -> qty)
public class Wishlist implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Map<String, Integer> items = new LinkedHashMap<>();

  // adding product to wishlist
  public void add(String productId, int qty) {
    if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");
    items.merge(productId, qty, Integer::sum);
  }

  // getting wishlist items as objects
  public List<WishlistItem> getItems() {
    List<WishlistItem> out = new LinkedList<>();
    for (Map.Entry<String,Integer> e : items.entrySet()) {
      out.add(new WishlistItem(e.getKey(), e.getValue()));
    }
    return out;
  }

  // getting quantity for product
  public int getQuantity(String productId) { return items.getOrDefault(productId, 0); }

  // checking emptiness
  public boolean isEmpty() { return items.isEmpty(); }

  // removing product from wishlist
  public void remove(String productId) { items.remove(productId); }

  // clearing wishlist
  public void clear() { items.clear(); }

  @Override public String toString() { return items.toString(); }
}
