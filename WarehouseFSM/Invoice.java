import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Invoice implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String id = "I-" + UUID.randomUUID().toString().substring(0, 8);
  private final String clientId;
  private final Date createdAt = new Date();
  private final Map<String, Line> lines = new LinkedHashMap<>();
  private double total;

  public static class Line implements Serializable {
    public final String productId;
    public final String productName;
    public final int qty;
    public final double unitPrice;
    public Line(String productId, String productName, int qty, double unitPrice) {
      this.productId = productId; this.productName = productName;
      this.qty = qty; this.unitPrice = unitPrice;
    }
    public double lineTotal() { return qty * unitPrice; }
    @Override public String toString() {
      return productName + " (" + productId + ") x " + qty + " @ $" + unitPrice + " = $" + lineTotal();
    }
  }

  public Invoice(String clientId) { this.clientId = clientId; }

  public void addLine(String productId, String productName, int qty, double unitPrice) {
    Line l = new Line(productId, productName, qty, unitPrice);
    lines.put(productId + "#" + lines.size(), l);
    total += l.lineTotal();
  }

  public String getId() { return id; }
  public String getClientId() { return clientId; }
  public double getTotal() { return total; }
  public Date getCreatedAt() { return createdAt; }
  public Iterable<Line> getLines() { return lines.values(); }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Invoice ").append(id).append(" for ").append(clientId)
      .append(" @ ").append(createdAt).append("\n");
    for (Line l : lines.values()) sb.append("  - ").append(l).append("\n");
    sb.append("TOTAL: $").append(total);
    return sb.toString();
  }
}
