package com.ras.console.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CyberpunkTheme {

    // Modern Enterprise SOC Slate Palette
    public static final String COLOR_BG_ROOT = "#0f172a";       // Slate 900
    public static final String COLOR_BG_SURFACE = "#1e293b";    // Slate 800
    public static final String COLOR_BG_ELEVATED = "#334155";   // Slate 700
    public static final String COLOR_BORDER = "#334155";        // Slate 700
    public static final String COLOR_BORDER_SUBTLE = "rgba(255, 255, 255, 0.08)";

    public static final String COLOR_PRIMARY_CYAN = "#38bdf8";    // Sky 400
    public static final String COLOR_SECONDARY_INDIGO = "#818cf8"; // Indigo 400
    public static final String COLOR_STATUS_ONLINE = "#10b981";   // Emerald 500
    public static final String COLOR_STATUS_WARNING = "#f59e0b";  // Amber 500
    public static final String COLOR_STATUS_DANGER = "#ef4444";   // Red 500

    public static final String COLOR_TEXT_MAIN = "#f8fafc";       // Slate 50
    public static final String COLOR_TEXT_MUTED = "#94a3b8";      // Slate 400
    public static final String COLOR_TEXT_DISABLED = "#6b7280";   // Gray 500

    public static final String FONT_MONO = "'JetBrains Mono', 'Consolas', 'Courier New', monospace";
    public static final String FONT_SANS = "'Inter', 'Segoe UI', sans-serif";

    public static void applyTheme(Scene scene) {
        String css = """
            .root {
                -fx-background-color: #0f172a;
                -fx-font-family: 'Inter', 'Segoe UI', sans-serif;
            }

            /* ScrollPanes */
            .scroll-pane {
                -fx-background-color: #0f172a;
                -fx-background: #0f172a;
                -fx-border-color: #334155;
                -fx-border-radius: 8px;
                -fx-background-radius: 8px;
            }
            .scroll-pane > .viewport {
                -fx-background-color: #0f172a;
            }

            /* ScrollBars */
            .scroll-bar:vertical, .scroll-bar:horizontal {
                -fx-background-color: #0f172a;
            }
            .scroll-bar:vertical .thumb, .scroll-bar:horizontal .thumb {
                -fx-background-color: #334155;
                -fx-background-radius: 4px;
            }
            .scroll-bar:vertical .thumb:hover, .scroll-bar:horizontal .thumb:hover {
                -fx-background-color: #475569;
            }

            /* TableViews */
            .table-view {
                -fx-background-color: #1e293b;
                -fx-border-color: #334155;
                -fx-border-radius: 8px;
                -fx-background-radius: 8px;
                -fx-table-cell-border-color: #1e293b;
                -fx-padding: 0px;
            }
            .table-view .column-header-background {
                -fx-background-color: #0f172a;
                -fx-border-color: transparent transparent #334155 transparent;
            }
            .table-view .column-header {
                -fx-background-color: #0f172a;
                -fx-text-fill: #94a3b8;
                -fx-font-weight: bold;
                -fx-font-size: 11px;
                -fx-font-family: 'JetBrains Mono', monospace;
                -fx-padding: 10px 14px;
            }
            .table-row-cell {
                -fx-background-color: #1e293b;
                -fx-text-fill: #f8fafc;
                -fx-padding: 6px 8px;
            }
            .table-row-cell:odd {
                -fx-background-color: #172033;
            }
            .table-row-cell:hover {
                -fx-background-color: #334155;
                -fx-text-fill: #f8fafc;
            }
            .table-row-cell:selected {
                -fx-background-color: #475569;
                -fx-text-fill: #38bdf8;
            }

            /* Form Controls */
            .text-field {
                -fx-background-color: #1e293b;
                -fx-text-fill: #f8fafc;
                -fx-prompt-text-fill: #64748b;
                -fx-border-color: #334155;
                -fx-border-radius: 6px;
                -fx-background-radius: 6px;
                -fx-padding: 8px 14px;
                -fx-font-size: 13px;
            }
            .text-field:focused {
                -fx-border-color: #38bdf8;
                -fx-effect: dropshadow(three-pass-box, rgba(56, 189, 248, 0.2), 4, 0, 0, 0);
            }

            .text-area {
                -fx-background-color: #0f172a;
                -fx-text-fill: #10b981;
                -fx-font-family: 'JetBrains Mono', 'Consolas', monospace;
                -fx-border-color: #334155;
                -fx-border-radius: 8px;
                -fx-background-radius: 8px;
            }
            .text-area .content {
                -fx-background-color: #0f172a;
            }

            /* Buttons */
            .button {
                -fx-background-color: #1e293b;
                -fx-text-fill: #f8fafc;
                -fx-border-color: #334155;
                -fx-border-radius: 6px;
                -fx-background-radius: 6px;
                -fx-font-weight: 600;
                -fx-font-size: 12px;
                -fx-padding: 8px 16px;
                -fx-cursor: hand;
            }
            .button:hover {
                -fx-background-color: #334155;
                -fx-border-color: #475569;
                -fx-text-fill: #f8fafc;
            }
            .primary-button {
                -fx-background-color: #0284c7;
                -fx-text-fill: #ffffff;
                -fx-border-color: #0284c7;
                -fx-border-radius: 6px;
                -fx-background-radius: 6px;
                -fx-font-weight: 600;
            }
            .primary-button:hover {
                -fx-background-color: #0369a1;
                -fx-border-color: #0369a1;
            }
            .danger-button {
                -fx-background-color: rgba(239, 68, 68, 0.1);
                -fx-text-fill: #ef4444;
                -fx-border-color: rgba(239, 68, 68, 0.3);
                -fx-border-radius: 6px;
                -fx-background-radius: 6px;
                -fx-font-weight: 600;
            }
            .danger-button:hover {
                -fx-background-color: #ef4444;
                -fx-text-fill: #ffffff;
            }
            """;
        scene.getStylesheets().add("data:text/css," + css.replace("\n", " ").replace("\"", "'"));
    }

    public static VBox createCardContainer(String title) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: #1e293b;
            -fx-border-color: #334155;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            """);
        if (title != null && !title.isBlank()) {
            Label header = new Label(title.toUpperCase());
            header.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 12px; -fx-letter-spacing: 1px; -fx-font-family: 'JetBrains Mono', monospace;");
            card.getChildren().add(header);
        }
        return card;
    }
}
