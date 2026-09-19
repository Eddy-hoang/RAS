package com.ras.console.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class DashboardView extends VBox {

    private final Label activeAgentsVal = new Label("0");
    private final Label onlineCountVal = new Label("0");
    private final Label securityVal = new Label("SECURE");
    private final Label auditVal = new Label("VERIFIED");
    private final Label cpuVal = new Label("N/A");
    private final Label memoryVal = new Label("N/A");

    public DashboardView() {
        setPadding(new Insets(24));
        setSpacing(24);
        setStyle("-fx-background-color: #0f172a;");

        Label pageTitle = new Label("SECURITY OPERATIONS CENTER // SYSTEM TELEMETRY");
        pageTitle.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1.2px; -fx-font-family: 'JetBrains Mono', monospace;");

        // Grid of Telemetry Metric Cards with ample, equal spacing
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        grid.add(createMetricCard("ACTIVE AGENTS", activeAgentsVal, "#38bdf8", "CONNECTED NODES"), 0, 0);
        grid.add(createMetricCard("ONLINE SESSIONS", onlineCountVal, "#10b981", "LIVE RPC SESSIONS"), 1, 0);
        grid.add(createMetricCard("SECURITY ENGINE", securityVal, "#10b981", "mTLS + RBAC ACTIVE"), 2, 0);

        grid.add(createMetricCard("HASH-CHAIN INTEGRITY", auditVal, "#818cf8", "SHA-256 LOGS"), 0, 1);
        grid.add(createMetricCard("CPU USAGE", cpuVal, "#6b7280", "UNEXPOSED BACKEND"), 1, 1);
        grid.add(createMetricCard("MEMORY LOAD", memoryVal, "#6b7280", "UNEXPOSED BACKEND"), 2, 1);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            grid.getColumnConstraints().add(col);
        }

        // System Architecture Panel (Clean, surface color #1e293b, subtle border #334155)
        VBox sysOverview = new VBox(16);
        sysOverview.setPadding(new Insets(20));
        sysOverview.setStyle("""
            -fx-background-color: #1e293b;
            -fx-border-color: #334155;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            """);

        Label sysTitle = new Label("SYSTEM SECURITY ARCHITECTURE & CAPABILITIES");
        sysTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 12px; -fx-letter-spacing: 1px; -fx-font-family: 'JetBrains Mono', monospace;");
        sysOverview.getChildren().add(sysTitle);

        // Invisible Grid for perfectly aligned "Label: Description" pairs
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(16);
        detailsGrid.setVgap(12);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(260);
        labelCol.setMaxWidth(260);

        ColumnConstraints descCol = new ColumnConstraints();
        descCol.setHgrow(Priority.ALWAYS);

        detailsGrid.getColumnConstraints().addAll(labelCol, descCol);

        addDetailGridRow(detailsGrid, 0, "PROTOCOL ENGINE", "12-Byte Custom Binary Packet Header (Opcode + Length + Payload)");
        addDetailGridRow(detailsGrid, 1, "CONCURRENCY ARCHITECTURE", "Java 24 Virtual Threads Per Task Executor");
        addDetailGridRow(detailsGrid, 2, "SECURITY & AUTHENTICATION", "Role-Based Access Control (RBAC) + Session Tokens");
        addDetailGridRow(detailsGrid, 3, "AUDIT LOG ENGINE", "SHA-256 Tamper-Evident Hash-Chain Verification Engine");
        addDetailGridRow(detailsGrid, 4, "SCREEN STREAMING", "64x64 Delta Tile Change Detection & JPEG Compression");

        sysOverview.getChildren().add(detailsGrid);

        getChildren().addAll(pageTitle, grid, sysOverview);
    }

    private VBox createMetricCard(String title, Label valueLabel, String colorHex, String subtext) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: #1e293b;
            -fx-border-color: %s #334155 #334155 #334155;
            -fx-border-width: 2px 1px 1px 1px;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            """.formatted(colorHex));

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        valueLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: 800; -fx-font-size: 26px; -fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        Label sub = new Label(subtext);
        sub.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        card.getChildren().addAll(t, valueLabel, sub);
        return card;
    }

    private void addDetailGridRow(GridPane grid, int row, String labelText, String descText) {
        Label l = new Label("• " + labelText);
        l.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        Label d = new Label(descText);
        d.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 12px; -fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        grid.add(l, 0, row);
        grid.add(d, 1, row);
    }

    public void updateMetrics(int activeClients) {
        activeAgentsVal.setText(String.valueOf(activeClients));
        onlineCountVal.setText(String.valueOf(activeClients));
    }
}
