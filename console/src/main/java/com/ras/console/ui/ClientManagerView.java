package com.ras.console.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class ClientManagerView extends VBox {

    private final TableView<Map<String, Object>> clientTable = new TableView<>();
    private final ObservableList<Map<String, Object>> clientData = FXCollections.observableArrayList();

    public ClientManagerView(Runnable onRefresh) {
        setPadding(new Insets(24));
        setSpacing(20);
        setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("AGENT MANAGEMENT // REGISTERED CLIENT SESSIONS");
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1.2px; -fx-font-family: 'JetBrains Mono', monospace;");

        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("Fetch Active Agents");
        refreshBtn.getStyleClass().add("primary-button");
        refreshBtn.setOnAction(e -> {
            if (onRefresh != null) onRefresh.run();
        });

        topBar.getChildren().add(refreshBtn);

        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty("● ONLINE"));
        statusCol.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("CLIENT ID / AGENT");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get("clientId"))));
        idCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + "; -fx-text-fill: #38bdf8;");

        TableColumn<Map<String, Object>, String> ipCol = new TableColumn<>("IP ADDRESS");
        ipCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get("ipAddress"))));
        ipCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        TableColumn<Map<String, Object>, String> stateCol = new TableColumn<>("SESSION STATE");
        stateCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get("state"))));
        stateCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + "; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        clientTable.getColumns().setAll(statusCol, idCol, ipCol, stateCol);
        clientTable.setItems(clientData);

        VBox.setVgrow(clientTable, Priority.ALWAYS);

        getChildren().addAll(title, topBar, clientTable);
    }

    public void updateClients(List<Map<String, Object>> list) {
        clientData.clear();
        clientData.addAll(list);
    }

    public int getClientCount() {
        return clientData.size();
    }
}
