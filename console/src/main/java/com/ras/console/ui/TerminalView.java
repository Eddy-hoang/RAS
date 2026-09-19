package com.ras.console.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class TerminalView extends VBox {

    private final TextArea terminalArea = new TextArea();
    private final TextField inputField = new TextField();
    private final Consumer<String> commandHandler;

    public TerminalView(Consumer<String> commandHandler) {
        this.commandHandler = commandHandler;

        setPadding(new Insets(24));
        setSpacing(20);
        setStyle("-fx-background-color: #0f172a;");

        Label title = new Label("SOC INTERACTIVE TERMINAL // DIRECT BACKEND RPC CLI");
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1.2px; -fx-font-family: 'JetBrains Mono', monospace;");

        terminalArea.setEditable(false);
        terminalArea.setStyle("""
            -fx-font-family: 'JetBrains Mono', 'Consolas', monospace;
            -fx-font-size: 12px;
            -fx-text-fill: #10b981;
            -fx-control-inner-background: #0f172a;
            -fx-border-color: #334155;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            """);
        terminalArea.setText("RAS SECURE REMOTE ADMINISTRATION TERMINAL [v1.0-SNAPSHOT]\nType 'help' to view available SOC commands mapped to live backend capabilities.\n\nRAS://SYSTEM> ");
        VBox.setVgrow(terminalArea, Priority.ALWAYS);

        HBox inputBar = new HBox(12);
        inputBar.setAlignment(Pos.CENTER_LEFT);

        Label promptLabel = new Label("RAS://COMMAND>");
        promptLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12px;");

        inputField.setPromptText("Enter command (e.g. system.info, client.list, process.list, audit.verify, help)...");
        inputField.setStyle("-fx-font-family: 'JetBrains Mono', monospace;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        inputField.setOnAction(e -> executeCommand());

        Button sendBtn = new Button("Execute");
        sendBtn.getStyleClass().add("primary-button");
        sendBtn.setOnAction(e -> executeCommand());

        inputBar.getChildren().addAll(promptLabel, inputField, sendBtn);

        getChildren().addAll(title, terminalArea, inputBar);
    }

    private void executeCommand() {
        String cmd = inputField.getText().trim();
        if (cmd.isEmpty()) return;

        appendOutput("\nRAS://OPERATOR> " + cmd);
        inputField.clear();

        String lowerCmd = cmd.toLowerCase();
        if (lowerCmd.equals("help")) {
            appendOutput("""
                AVAILABILITY MAPPED COMMANDS:
                  system.info     - Fetch core server telemetry & runtime configuration
                  client.list     - Fetch connected active agent sessions
                  process.list    - Fetch remote system processes
                  audit.verify    - Verify SHA-256 Hash-Chain log integrity
                  clear           - Clear terminal window
                """);
        } else if (lowerCmd.equals("clear")) {
            terminalArea.setText("RAS SECURE REMOTE ADMINISTRATION TERMINAL [v1.0-SNAPSHOT]\n\nRAS://SYSTEM> ");
        } else if (lowerCmd.equals("system.info") || lowerCmd.equals("client.list") || lowerCmd.equals("process.list") || lowerCmd.equals("audit.verify")) {
            appendOutput("Executing command [" + cmd + "] via live backend RPC dispatcher...");
            if (commandHandler != null) {
                commandHandler.accept(lowerCmd);
            }
        } else {
            appendOutput("NOT IMPLEMENTED: Command '" + cmd + "' is not mapped to an active backend capability. Type 'help' for valid commands.");
        }
    }

    public void appendOutput(String text) {
        terminalArea.appendText(text + "\n");
    }
}
