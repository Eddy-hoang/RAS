package com.ras.console.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HeaderView extends HBox {

    private final Label connectionStatusLabel = new Label("SERVER: CONNECTING...");
    private final Label securityBadge = new Label("mTLS ACTIVE  •  RBAC ENFORCED  •  SHA-256 LOGS");
    private final Label activeClientsLabel = new Label("ACTIVE AGENTS: 0");
    private final Button refreshBtn = new Button("Refresh Agents");

    public HeaderView(Runnable onRefresh) {
        setPadding(new Insets(14, 24, 14, 24));
        setSpacing(16);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #0f172a; -fx-border-color: #334155; -fx-border-width: 0 0 1px 0;");

        Label brandEmblem = new Label("RAS // COMMAND CENTER");
        brandEmblem.setStyle("""
            -fx-font-weight: 800;
            -fx-font-size: 14px;
            -fx-text-fill: #f8fafc;
            -fx-letter-spacing: 1.2px;
            -fx-font-family: 'JetBrains Mono', 'Segoe UI', sans-serif;
            """);

        connectionStatusLabel.setStyle("""
            -fx-background-color: rgba(16, 185, 129, 0.1);
            -fx-border-color: rgba(16, 185, 129, 0.3);
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-padding: 3px 10px;
            -fx-text-fill: #10b981;
            -fx-font-weight: 600;
            -fx-font-family: 'JetBrains Mono', monospace;
            -fx-font-size: 11px;
            """);

        securityBadge.setStyle("""
            -fx-background-color: rgba(129, 140, 248, 0.1);
            -fx-border-color: rgba(129, 140, 248, 0.25);
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-padding: 3px 12px;
            -fx-text-fill: #818cf8;
            -fx-font-weight: 600;
            -fx-font-size: 11px;
            -fx-font-family: 'JetBrains Mono', monospace;
            """);

        activeClientsLabel.setStyle("""
            -fx-background-color: #1e293b;
            -fx-border-color: #334155;
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-padding: 3px 10px;
            -fx-text-fill: #38bdf8;
            -fx-font-weight: 600;
            -fx-font-family: 'JetBrains Mono', monospace;
            -fx-font-size: 11px;
            """);

        Label operatorLabel = new Label("OPERATOR: ADMIN");
        operatorLabel.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: 600;");

        refreshBtn.setStyle("""
            -fx-background-color: #1e293b;
            -fx-text-fill: #94a3b8;
            -fx-border-color: #334155;
            -fx-border-radius: 6px;
            -fx-background-radius: 6px;
            -fx-font-weight: 600;
            -fx-font-size: 11px;
            -fx-padding: 5px 12px;
            -fx-cursor: hand;
            """);

        refreshBtn.setOnAction(e -> {
            if (onRefresh != null) onRefresh.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                brandEmblem,
                connectionStatusLabel,
                securityBadge,
                spacer,
                activeClientsLabel,
                operatorLabel,
                refreshBtn
        );
    }

    public void updateServerStatus(boolean connected, String address) {
        if (connected) {
            connectionStatusLabel.setText("SERVER CONNECTED (" + address + ")");
            connectionStatusLabel.setStyle("""
                -fx-background-color: rgba(16, 185, 129, 0.1);
                -fx-border-color: rgba(16, 185, 129, 0.3);
                -fx-border-radius: 12px;
                -fx-background-radius: 12px;
                -fx-padding: 3px 10px;
                -fx-text-fill: #10b981;
                -fx-font-weight: 600;
                -fx-font-family: 'JetBrains Mono', monospace;
                -fx-font-size: 11px;
                """);
        } else {
            connectionStatusLabel.setText("SERVER DISCONNECTED");
            connectionStatusLabel.setStyle("""
                -fx-background-color: rgba(239, 68, 68, 0.1);
                -fx-border-color: rgba(239, 68, 68, 0.3);
                -fx-border-radius: 12px;
                -fx-background-radius: 12px;
                -fx-padding: 3px 10px;
                -fx-text-fill: #ef4444;
                -fx-font-weight: 600;
                -fx-font-family: 'JetBrains Mono', monospace;
                -fx-font-size: 11px;
                """);
        }
    }

    public void updateActiveClientsCount(int count) {
        activeClientsLabel.setText("ACTIVE AGENTS: " + count);
    }
}
