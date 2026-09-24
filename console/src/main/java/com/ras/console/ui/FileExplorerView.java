package com.ras.console.ui;

import com.ras.common.dto.FileItemDTO;
import javafx.application.Platform;
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
import java.util.function.Consumer;

public class FileExplorerView extends VBox {

    private final TableView<FileItemDTO> fileTable = new TableView<>();
    private final ObservableList<FileItemDTO> fileData = FXCollections.observableArrayList();
    private final Label currentPathLabel = new Label("CURRENT PATH: .");

    private final ComboBox<String> clientComboBox = new ComboBox<>();
    private final Consumer<String> onFetch;

    public FileExplorerView(Consumer<String> onFetch) {
        this.onFetch = onFetch;

        setPadding(new Insets(24));
        setSpacing(20);
        setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("REMOTE FILE SYSTEM EXPLORER // DIRECTORY BROWSER");
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1.2px; -fx-font-family: 'JetBrains Mono', monospace;");

        HBox btnBar = new HBox(14);
        btnBar.setAlignment(Pos.CENTER_LEFT);

        Label selectLabel = new Label("TARGET AGENT:");
        selectLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px;");

        clientComboBox.setPromptText("Select Agent...");
        clientComboBox.setStyle("""
            -fx-background-color: #1e293b;
            -fx-text-fill: #38bdf8;
            -fx-border-color: #334155;
            -fx-border-radius: 6px;
            -fx-font-weight: bold;
            """);

        Button fetchFilesBtn = new Button("Fetch Files (.)");
        fetchFilesBtn.getStyleClass().add("primary-button");
        fetchFilesBtn.setOnAction(e -> {
            String selectedClient = clientComboBox.getValue();
            if (this.onFetch != null) this.onFetch.accept(selectedClient);
        });

        currentPathLabel.setStyle("""
            -fx-background-color: #1e293b;
            -fx-border-color: #334155;
            -fx-border-radius: 6px;
            -fx-background-radius: 6px;
            -fx-padding: 6px 14px;
            -fx-text-fill: #94a3b8;
            -fx-font-family: 'JetBrains Mono', monospace;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
            """);

        btnBar.getChildren().addAll(selectLabel, clientComboBox, fetchFilesBtn, currentPathLabel);

        TableColumn<FileItemDTO, String> nameCol = new TableColumn<>("FILE NAME");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + "; -fx-text-fill: #38bdf8;");

        TableColumn<FileItemDTO, String> sizeCol = new TableColumn<>("SIZE (BYTES)");
        sizeCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getSizeBytes())));
        sizeCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        TableColumn<FileItemDTO, String> dirCol = new TableColumn<>("TYPE");
        dirCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isDirectory() ? "DIR" : "FILE"));
        dirCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + "; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fileTable.getColumns().setAll(nameCol, sizeCol, dirCol);
        fileTable.setItems(fileData);

        VBox.setVgrow(fileTable, Priority.ALWAYS);

        getChildren().addAll(title, btnBar, fileTable);
    }

    public void updateClientList(List<String> clientIds) {
        Platform.runLater(() -> {
            String current = clientComboBox.getValue();
            clientComboBox.getItems().setAll(clientIds);
            if (current != null && clientIds.contains(current)) {
                clientComboBox.setValue(current);
            } else if (!clientIds.isEmpty()) {
                clientComboBox.setValue(clientIds.get(0));
            }
        });
    }

    public void updateFiles(List<FileItemDTO> list) {
        fileData.clear();
        fileData.addAll(list);
    }
}
