public class Main {
    public static void main(String[] args) {
        ItemRegistry registry = new ItemRegistry();
        registry.seedDemoData(); // sample items so the demo works right away
        try {
            new ConsoleUI(registry).run();
        } catch (IllegalStateException e) {
            System.out.println("\n  Input closed. Exiting UniTrace.");
        }
    }
}
