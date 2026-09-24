package com.ras.console.ui;

import com.ras.common.dto.ProcessInfoDTO;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ProcessManagerView extends VBox {

    private final TableView<ProcessInfoDTO> processTable = new TableView<>();
    private final ObservableList<ProcessInfoDTO> processData = FXCollections.observableArrayList();
    private final FilteredList<ProcessInfoDTO> filteredProcessData;

    private final ComboBox<String> clientComboBox = new ComboBox<>();
    private final Consumer<String> onFetch;
    private final BiConsumer<String, Long> onKill;

    public ProcessManagerView(Consumer<String> onFetch, BiConsumer<String, Long> onKill) {
        this.onFetch = onFetch;
        this.onKill = onKill;

        setPadding(new Insets(24));
        setSpacing(20);
        setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("REMOTE PROCESS MANAGER // SYSTEM PROCESS TELEMETRY");
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1.2px; -fx-font-family: 'JetBrains Mono', monospace;");

        HBox topControls = new HBox(14);
        topControls.setAlignment(Pos.CENTER_LEFT);

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

        Button fetchProcBtn = new Button("Fetch Process List");
        fetchProcBtn.getStyleClass().add("primary-button");
        fetchProcBtn.setOnAction(e -> {
            String selectedClient = clientComboBox.getValue();
            if (this.onFetch != null) this.onFetch.accept(selectedClient);
        });

        TextField searchField = new TextField();
        searchField.setPromptText("Search processes by Name, PID, User...");
        searchField.setPrefWidth(320);

        topControls.getChildren().addAll(selectLabel, clientComboBox, fetchProcBtn, searchField);

        filteredProcessData = new FilteredList<>(processData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredProcessData.setPredicate(proc -> {
                if (newValue == null || newValue.isBlank()) return true;
                String filter = newValue.toLowerCase().trim();
                if (proc.getName() != null && proc.getName().toLowerCase().contains(filter)) return true;
                if (String.valueOf(proc.getPid()).contains(filter)) return true;
                if (proc.getUser() != null && proc.getUser().toLowerCase().contains(filter)) return true;
                return false;
            });
        });

        TableColumn<ProcessInfoDTO, Long> pidCol = new TableColumn<>("PID");
        pidCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPid()));
        pidCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + "; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        TableColumn<ProcessInfoDTO, String> nameCol = new TableColumn<>("PROCESS NAME");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + "; -fx-text-fill: #38bdf8;");

        TableColumn<ProcessInfoDTO, String> userCol = new TableColumn<>("USER ACCOUNT");
        userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser()));
        userCol.setStyle("-fx-font-family: " + CyberpunkTheme.FONT_MONO + ";");

        processTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        processTable.getColumns().setAll(pidCol, nameCol, userCol);
        processTable.setItems(filteredProcessData);

        Button killBtn = new Button("Terminate Selected Process (PID)");
        killBtn.getStyleClass().add("danger-button");
        killBtn.setOnAction(e -> {
            ProcessInfoDTO selected = processTable.getSelectionModel().getSelectedItem();
            String selectedClient = clientComboBox.getValue();
            if (selected != null && this.onKill != null) {
                this.onKill.accept(selectedClient, selected.getPid());
            }
        });

        VBox.setVgrow(processTable, Priority.ALWAYS);

        getChildren().addAll(title, topControls, processTable, killBtn);
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

    public void updateProcesses(List<ProcessInfoDTO> list) {
        processData.clear();
        processData.addAll(list);
    }
}
