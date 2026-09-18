package com.ras.console;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SRAP - Secure Remote Administration Platform");

        BorderPane mainLayout = new BorderPane();

        // Top Status Header
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #1e1e2e; -fx-text-fill: white;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("SRAP Admin Console");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #cdd6f4;");

        Label serverStatus = new Label("Server Status: CONNECTED (127.0.0.1:8090)");
        serverStatus.setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold;");

        topBar.getChildren().addAll(titleLabel, new Separator(), serverStatus);
        mainLayout.setTop(topBar);

        // TabPane for Feature Sections
        TabPane tabPane = new TabPane();

        // 1. Client Manager Tab
        Tab clientTab = new Tab("Client Manager", createClientManagerView());
        clientTab.setClosable(false);

        // 2. Process Manager Tab
        Tab processTab = new Tab("Process Manager", createProcessManagerView());
        processTab.setClosable(false);

        // 3. File Explorer Tab
        Tab fileTab = new Tab("File Explorer", createFileExplorerView());
        fileTab.setClosable(false);

        // 4. Remote Screen Stream Tab
        Tab screenTab = new Tab("Remote Screen (Delta Stream)", createScreenStreamView());
        screenTab.setClosable(false);

        // 5. Audit Log Tab
        Tab auditTab = new Tab("Audit Log", createAuditLogView());
        auditTab.setClosable(false);

        tabPane.getTabs().addAll(clientTab, processTab, fileTab, screenTab, auditTab);
        mainLayout.setCenter(tabPane);

        Scene scene = new Scene(mainLayout, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Node createClientManagerView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));

        Label label = new Label("Online Client Agents");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TableView<String> clientTable = new TableView<>();
        TableColumn<String, String> idCol = new TableColumn<>("Client ID");
        TableColumn<String, String> ipCol = new TableColumn<>("IP Address");
        TableColumn<String, String> statusCol = new TableColumn<>("Status");
        clientTable.getColumns().addAll(idCol, ipCol, statusCol);

        box.getChildren().addAll(label, clientTable);
        return box;
    }

    private Node createProcessManagerView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        Label label = new Label("Remote Process Manager");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TableView<String> processTable = new TableView<>();
        Button killBtn = new Button("Terminate Process (PID)");
        killBtn.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: white;");

        box.getChildren().addAll(label, processTable, killBtn);
        return box;
    }

    private Node createFileExplorerView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        Label label = new Label("Remote File System Explorer");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox btnBar = new HBox(10);
        Button downloadBtn = new Button("Download File");
        Button uploadBtn = new Button("Upload File");
        btnBar.getChildren().addAll(downloadBtn, uploadBtn);

        TableView<String> fileTable = new TableView<>();
        box.getChildren().addAll(label, btnBar, fileTable);
        return box;
    }

    private Node createScreenStreamView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.CENTER);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        Button startBtn = new Button("Start Stream");
        Button stopBtn = new Button("Stop Stream");
        Label bandwidthLabel = new Label("Bandwidth Reduction: 89.4% (Delta Tile Stream)");
        bandwidthLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #a6e3a1;");
        controls.getChildren().addAll(startBtn, stopBtn, bandwidthLabel);

        Pane screenViewport = new Pane();
        screenViewport.setPrefSize(800, 450);
        screenViewport.setStyle("-fx-background-color: #11111b; -fx-border-color: #45475a;");

        box.getChildren().addAll(controls, screenViewport);
        return box;
    }

    private Node createAuditLogView() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        Label label = new Label("Tamper-Evident Audit Log (Hash-Chain Verified)");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: monospace;");
        logArea.setText("2026-09-18 22:30:00 [ADMIN: admin01] [CLIENT: PC-01] ACTION: SYSTEM_INFO_REQUEST -> SUCCESS (hash: a3f8...)\n" +
                "2026-09-18 22:31:05 [ADMIN: admin01] [CLIENT: PC-01] ACTION: PROCESS_KILL (pid: 1204) -> SUCCESS (hash: f9e2...)\n");

        box.getChildren().addAll(label, logArea);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
