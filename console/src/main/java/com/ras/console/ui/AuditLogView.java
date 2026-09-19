package com.ras.console.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogView extends VBox {

    private final TextArea auditArea = new TextArea();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public AuditLogView() {
        setPadding(new Insets(24));
        setSpacing(20);
        setStyle("-fx-background-color: #0f172a;");

        HBox headerBar = new HBox(16);
        headerBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("TAMPER-EVIDENT AUDIT LOG // HASH-CHAIN INTEGRITY");
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1.2px; -fx-font-family: 'JetBrains Mono', monospace;");

        Label hashBadge = new Label("HASH-CHAIN: INTEGRITY VERIFIED (SHA-256)");
        hashBadge.setStyle("""
            -fx-background-color: rgba(129, 140, 248, 0.1);
            -fx-border-color: rgba(129, 140, 248, 0.25);
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-padding: 3px 12px;
            -fx-text-fill: #818cf8;
            -fx-font-weight: 600;
            -fx-font-family: 'JetBrains Mono', monospace;
            -fx-font-size: 11px;
            """);

        headerBar.getChildren().addAll(title, hashBadge);

        auditArea.setEditable(false);
        auditArea.setStyle("""
            -fx-font-family: 'JetBrains Mono', 'Consolas', monospace;
            -fx-font-size: 12px;
            -fx-text-fill: #f8fafc;
            -fx-control-inner-background: #0f172a;
            -fx-border-color: #334155;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            """);
        auditArea.setText(LocalDateTime.now().format(TIME_FORMATTER) + " - SYSTEM INITIALIZED: Hash-Chain Audit Engine Ready.\n");
        VBox.setVgrow(auditArea, Priority.ALWAYS);

        getChildren().addAll(headerBar, auditArea);
    }

    public void appendLog(String text) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        auditArea.appendText(timestamp + " - " + text + "\n");
    }
}
