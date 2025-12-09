package Resources;
public abstract class Resource {
    private String name;
    private int quantity;
    private int cost;
    private String description;

    public Resource(String name, int quantity, int cost, String description) {
        this.name = name;
        this.quantity = quantity;
        this.cost = cost;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public int getQuantity() {
        return quantity;
    }

    public int getCost() {
        return cost;
    }

    public String getDescription() {
        return description;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
}
