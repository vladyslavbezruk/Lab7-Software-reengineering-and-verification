import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ShoppingCart class manages items and calculates prices.
 */
public class ShoppingCart {

    private static final NumberFormat MONEY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        MONEY_FORMAT = new DecimalFormat("$#.00", symbols);
    }

    private final List<Item> items = new ArrayList<>();

    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Apple", 0.99, 5, ItemType.NEW);
        cart.addItem("Banana", 20.00, 4, ItemType.SECOND_FREE);
        cart.addItem("Toilet Paper", 17.20, 1, ItemType.SALE);
        cart.addItem("Nails", 2.00, 500, ItemType.REGULAR);
        System.out.println(cart.getFormattedTicket());
    }

    public static int calculateDiscount(ItemType type, int quantity) {
        int discount = switch (type) {
            case NEW -> 0;
            case REGULAR -> 0;
            case SECOND_FREE -> quantity > 1 ? 50 : 0;
            case SALE -> 70;
        };
        if (type != ItemType.NEW) {
            discount += Math.min(80, quantity / 10);
        }
        return Math.min(discount, 80);
    }

    public static void appendFormatted(StringBuilder sb, String value, int alignment, int width) {
        if (value.length() > width) {
            value = value.substring(0, width);
        }
        int padding = width - value.length();
        int leftPadding = alignment == -1 ? 0 : alignment == 1 ? padding : padding / 2;
        int rightPadding = padding - leftPadding;

        sb.append(" ".repeat(leftPadding))
                .append(value)
                .append(" ".repeat(rightPadding))
                .append(" ");
    }

    /**
     * Adds a new item to the shopping cart.
     *
     * @param title    item title (1-32 characters)
     * @param price    item price (> 0)
     * @param quantity item quantity (>= 1)
     * @param type     item type
     * @throws IllegalArgumentException if any argument is invalid
     */
    public void addItem(String title, double price, int quantity, ItemType type) {
        if (title == null || title.isEmpty() || title.length() > 32) {
            throw new IllegalArgumentException("Title must be 1 to 32 characters long.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        items.add(new Item(title, price, quantity, type));
    }

    /**
     * Generates a formatted receipt for the shopping cart.
     *
     * @return formatted receipt as a string
     */
    public String getFormattedTicket() {
        if (items.isEmpty()) {
            return "No items.";
        }

        StringBuilder receipt = new StringBuilder();
        String[] header = {"#", "Item", "Price", "Quan.", "Discount", "Total"};
        int[] align = {1, -1, 1, 1, 1, 1};
        List<String[]> rows = new ArrayList<>();
        double totalPrice = 0;

        int index = 0;
        for (Item item : items) {
            int discount = calculateDiscount(item.getType(), item.getQuantity());
            double itemTotal = item.getPrice() * item.getQuantity() * (100 - discount) / 100;
            rows.add(new String[]{
                    String.valueOf(++index),
                    item.getTitle(),
                    MONEY_FORMAT.format(item.getPrice()),
                    String.valueOf(item.getQuantity()),
                    discount == 0 ? "-" : discount + "%",
                    MONEY_FORMAT.format(itemTotal)
            });
            totalPrice += itemTotal;
        }

        String[] footer = {String.valueOf(index), "", "", "", "", MONEY_FORMAT.format(totalPrice)};

        int[] columnWidths = calculateColumnWidths(header, rows, footer);
        appendLine(receipt, header, align, columnWidths);
        appendSeparator(receipt, columnWidths);
        for (String[] row : rows) {
            appendLine(receipt, row, align, columnWidths);
        }
        appendSeparator(receipt, columnWidths);
        appendLine(receipt, footer, align, columnWidths);

        return receipt.toString();
    }

    private int[] calculateColumnWidths(String[] header, List<String[]> rows, String[] footer) {
        int columnCount = header.length;
        int[] columnWidths = new int[columnCount];

        for (String[] line : rows) {
            updateColumnWidths(columnWidths, line);
        }
        updateColumnWidths(columnWidths, header);
        updateColumnWidths(columnWidths, footer);

        return columnWidths;
    }

    private void updateColumnWidths(int[] widths, String[] columns) {
        for (int i = 0; i < widths.length; i++) {
            widths[i] = Math.max(widths[i], columns[i].length());
        }
    }

    private void appendSeparator(StringBuilder sb, int[] columnWidths) {
        int totalWidth = Arrays.stream(columnWidths).sum() + columnWidths.length - 1;
        sb.append("-".repeat(totalWidth)).append("\n");
    }

    private void appendLine(StringBuilder sb, String[] line, int[] align, int[] widths) {
        for (int i = 0; i < line.length; i++) {
            appendFormatted(sb, line[i], align[i], widths[i]);
        }
        sb.append("\n");
    }

    public enum ItemType {NEW, REGULAR, SECOND_FREE, SALE}

    private static class Item {
        private final String title;
        private final double price;
        private final int quantity;
        private final ItemType type;

        public Item(String title, double price, int quantity, ItemType type) {
            this.title = title;
            this.price = price;
            this.quantity = quantity;
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public ItemType getType() {
            return type;
        }
    }
}