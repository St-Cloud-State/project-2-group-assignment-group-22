import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// storing per-product FIFO queues
public class Waitlist implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Map<String, Deque<WaitlistItem>> byProduct = new HashMap<>();

  // adding new waitlist item for product and client
  public String add(String productId, int qty, String clientId) {
    byProduct.computeIfAbsent(productId, k -> new ArrayDeque<>())
             .addLast(new WaitlistItem(productId, clientId, qty));
    return productId + "#" + byProduct.get(productId).size();
  }

  // getting queue for specific product
  public Deque<WaitlistItem> queueFor(String productId) {
    return byProduct.computeIfAbsent(productId, k -> new ArrayDeque<>());
  }

  // counting total waitlist items for product
  public int countForProduct(String productId) {
    return queueFor(productId).size();
  }

  // getting iterator for all product queues
  public Iterator<Map.Entry<String, Deque<WaitlistItem>>> entries() {
    return byProduct.entrySet().iterator();
  }

  // getting all waitlist items for a specific client
  public List<WaitlistItem> getClientWaitlist(String clientId) {
    List<WaitlistItem> results = new ArrayList<>();
    for (Deque<WaitlistItem> queue : byProduct.values()) {
      for (WaitlistItem wi : queue) {
        if (wi.getClientId().equalsIgnoreCase(clientId)) {
          results.add(wi);
        }
      }
    }
    return results;
  }

  // returning string representation of all waitlists
  @Override
  public String toString() { return byProduct.toString(); }
}
