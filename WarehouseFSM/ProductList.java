import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ProductList implements Serializable {
  private static final long serialVersionUID = 1L;

  private final List<Product> products = new LinkedList<>();
  private static ProductList instance;

  private ProductList() {}

  public static ProductList instance() {
    if (instance == null) instance = new ProductList();
    return instance;
  }

  public boolean insertProduct(Product p) { return products.add(p); }
  public Iterator<Product> getProducts() { return products.iterator(); }

  public Product search(String id) {
    for (Product p : products) if (p.getId().equals(id)) return p;
    return null;
  }

  /** for  Convenience - finding by ordinal P1..Pn; returns null if not found */
  public Product findByNumber(int n) {
    String id = "P" + n;
    return search(id);
  }
}
