package com.ras.console.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarView extends VBox {

    private final Map<String, Button> navButtons = new HashMap<>();
    private final Consumer<String> onNavSelected;
    private String activeTab = "DASHBOARD";

    public SidebarView(Consumer<String> onNavSelected) {
        this.onNavSelected = onNavSelected;

        setPrefWidth(220);
        setMinWidth(220);
        setMaxWidth(220);
        setPadding(new Insets(20, 12, 20, 12));
        setSpacing(4);
        setStyle("-fx-background-color: transparent; -fx-border-color: #334155; -fx-border-width: 0 1px 0 0;");

        Label navHeader = new Label("NAVIGATION");
        navHeader.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 10px; -fx-letter-spacing: 1.2px; -fx-font-family: 'JetBrains Mono', monospace; -fx-padding: 0 0 8 10;");
        getChildren().add(navHeader);

        addNavButton("DASHBOARD", "📊   Dashboard");
        addNavButton("CLIENTS", "🖥   Agent Clients");
        addNavButton("PROCESSES", "⚙   Processes");
        addNavButton("FILES", "📁   File Explorer");
        addNavButton("SCREEN", "📺   Screen Stream");
        addNavButton("TERMINAL", "💻   SOC Terminal");
        addNavButton("AUDIT", "📜   Audit Log");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        VBox footerBox = new VBox(4);
        footerBox.setPadding(new Insets(12, 10, 0, 10));
        footerBox.setStyle("-fx-border-color: #334155 transparent transparent transparent; -fx-border-width: 1px 0 0 0;");

        Label sysLabel = new Label("RAS PLATFORM");
        sysLabel.setStyle("-fx-text-fill: #64748b; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 10px; -fx-font-weight: bold;");

        Label verLabel = new Label("v1.0-SNAPSHOT // ONLINE");
        verLabel.setStyle("-fx-text-fill: #10b981; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 10px;");

        footerBox.getChildren().addAll(sysLabel, verLabel);
        getChildren().add(footerBox);

        setActiveNav("DASHBOARD");
    }

    private void addNavButton(String key, String label) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(getInactiveStyle());

        btn.setOnMouseEntered(e -> {
            if (!key.equals(activeTab)) {
                btn.setStyle(getHoverStyle());
            }
        });

        btn.setOnMouseExited(e -> {
            if (!key.equals(activeTab)) {
                btn.setStyle(getInactiveStyle());
            }
        });

        btn.setOnAction(e -> setActiveNav(key));
        navButtons.put(key, btn);
        getChildren().add(btn);
    }

    public void setActiveNav(String key) {
        this.activeTab = key;
        navButtons.forEach((k, btn) -> {
            if (k.equals(key)) {
                btn.setStyle(getActiveStyle());
            } else {
                btn.setStyle(getInactiveStyle());
            }
        });
        if (onNavSelected != null) {
            onNavSelected.accept(key);
        }
    }

    private String getActiveStyle() {
        return """
            -fx-background-color: #1e293b;
            -fx-text-fill: #f8fafc;
            -fx-border-color: transparent transparent transparent #38bdf8;
            -fx-border-width: 0 0 0 3px;
            -fx-border-radius: 0px 6px 6px 0px;
            -fx-background-radius: 0px 6px 6px 0px;
            -fx-font-weight: 600;
            -fx-font-size: 12px;
            -fx-padding: 9px 14px;
            -fx-cursor: hand;
            """;
    }

    private String getInactiveStyle() {
        return """
            -fx-background-color: transparent;
            -fx-text-fill: #94a3b8;
            -fx-border-color: transparent;
            -fx-border-radius: 6px;
            -fx-background-radius: 6px;
            -fx-font-weight: normal;
            -fx-font-size: 12px;
            -fx-padding: 9px 14px;
            -fx-cursor: hand;
            """;
    }

    private String getHoverStyle() {
        return """
            -fx-background-color: rgba(30, 41, 59, 0.5);
            -fx-text-fill: #f8fafc;
            -fx-border-color: transparent;
            -fx-border-radius: 6px;
            -fx-background-radius: 6px;
            -fx-font-weight: normal;
            -fx-font-size: 12px;
            -fx-padding: 9px 14px;
            -fx-cursor: hand;
            """;
    }
}
