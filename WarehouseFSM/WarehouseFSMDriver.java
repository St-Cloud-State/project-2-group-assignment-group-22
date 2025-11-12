public class WarehouseFSMDriver {
    public static void main(String[] args) {

        // creating shared warehouse backend instance
        Warehouse warehouse = Warehouse.instance();

        // wiring FSM context with warehouse and starting main loop
        Context context = new Context(warehouse);
        context.run();
    }
}
