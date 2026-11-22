package io.ecomarket.desktop.service;

import io.ecomarket.desktop.schema.entity.Order;
import io.ecomarket.desktop.schema.entity.OrderItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Generates printable HTML receipts and Carbon Offset Certificates.
 */
public class ReceiptExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public static String generateHtmlReceipt(Order order) {
        StringBuilder itemsHtml = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                String title = item.getProduct() != null ? item.getProduct().getTitle() : "Sustainable Item";
                BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal carbon = item.getProduct() != null && item.getProduct().getCarbonSavedKg() != null ?
                        item.getProduct().getCarbonSavedKg().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO;

                itemsHtml.append(String.format("""
                    <tr>
                        <td style="padding: 10px; border-bottom: 1px solid #e0e0e0;">%s</td>
                        <td style="padding: 10px; border-bottom: 1px solid #e0e0e0; text-align: center;">%d</td>
                        <td style="padding: 10px; border-bottom: 1px solid #e0e0e0; text-align: right;">$%.2f</td>
                        <td style="padding: 10px; border-bottom: 1px solid #e0e0e0; text-align: right; color: #2e7d32; font-weight: bold;">-%.2f kg</td>
                        <td style="padding: 10px; border-bottom: 1px solid #e0e0e0; text-align: right; font-weight: bold;">$%.2f</td>
                    </tr>
                """, title, item.getQuantity(), item.getUnitPrice(), carbon, lineTotal));
            }
        }

        String buyerName = order.getBuyer() != null ? order.getBuyer().getFullName() : "Valued Customer";
        String buyerEmail = order.getBuyer() != null ? order.getBuyer().getEmail() : "N/A";
        String dateStr = order.getCreatedAt() != null ? DATE_FMT.format(order.getCreatedAt()) : "N/A";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>ecoMarket Receipt — %s</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #2e3d2e; margin: 40px; background-color: #f7faf7; }
                    .container { max-width: 680px; margin: 0 auto; background: white; padding: 35px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.06); border: 1px solid #e1ebe1; }
                    .header { border-bottom: 2px solid #2e7d32; padding-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
                    .brand { font-size: 26px; font-weight: bold; color: #1b5e20; }
                    .badge { background: #e8f5e9; color: #2e7d32; padding: 6px 12px; border-radius: 20px; font-weight: bold; font-size: 13px; }
                    .cert-box { background: linear-gradient(135deg, #e8f5e9, #c8e6c9); border: 1px solid #a5d6a7; border-radius: 8px; padding: 18px; margin: 25px 0; text-align: center; }
                    .cert-title { font-size: 18px; font-weight: bold; color: #1b5e20; margin-bottom: 6px; }
                    .cert-metric { font-size: 28px; font-weight: 800; color: #2e7d32; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 20px; }
                    th { background: #f1f8e9; padding: 12px 10px; text-align: left; font-size: 13px; color: #33691e; }
                    .total-box { margin-top: 25px; padding-top: 15px; border-top: 2px dashed #cfd8dc; text-align: right; }
                    .total-amount { font-size: 22px; font-weight: bold; color: #1b5e20; }
                    .footer { text-align: center; margin-top: 35px; font-size: 12px; color: #757575; border-top: 1px solid #eeeeee; padding-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div>
                            <div class="brand">🌿 ecoMarket</div>
                            <div style="font-size: 13px; color: #666;">Sustainable Desktop Commerce</div>
                        </div>
                        <div class="badge">Official Carbon Offset Receipt</div>
                    </div>

                    <div style="margin-top: 25px; font-size: 14px;">
                        <p><strong>Order Number:</strong> %s</p>
                        <p><strong>Date:</strong> %s</p>
                        <p><strong>Customer:</strong> %s (%s)</p>
                        <p><strong>Shipping Address:</strong> %s</p>
                        <p><strong>Delivery Method:</strong> %s</p>
                    </div>

                    <div class="cert-box">
                        <div class="cert-title">🌱 Verified Carbon Reduction Certificate</div>
                        <div>By choosing preloved, upcycled, or zero-waste products, this purchase prevented:</div>
                        <div class="cert-metric">%.2f kg CO₂</div>
                        <div style="font-size: 12px; color: #1b5e20; margin-top: 4px;">from entering the atmosphere!</div>
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th style="text-align: center;">Qty</th>
                                <th style="text-align: right;">Unit Price</th>
                                <th style="text-align: right;">CO₂ Saved</th>
                                <th style="text-align: right;">Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>

                    <div class="total-box">
                        <div style="font-size: 14px; color: #555;">Payment Status: <strong>PAID</strong> (%s)</div>
                        <div class="total-amount">Total: $%.2f</div>
                    </div>

                    <div class="footer">
                        <p>Thank you for shopping sustainably with <strong>ecoMarket</strong>.</p>
                        <p>Backed by Supabase Cloud Infrastructure • Every purchase counts towards a greener planet.</p>
                    </div>
                </div>
            </body>
            </html>
        """,
        order.getOrderNumber(),
        order.getOrderNumber(),
        dateStr,
        buyerName,
        buyerEmail,
        order.getShippingAddress(),
        order.getDeliveryMethod(),
        order.getTotalCarbonSavedKg(),
        itemsHtml.toString(),
        order.getStatus(),
        order.getTotalAmount());
    }

    public static void saveReceiptToFile(Order order, File destination) throws IOException {
        String html = generateHtmlReceipt(order);
        try (FileWriter writer = new FileWriter(destination)) {
            writer.write(html);
        }
    }
}
