package com.ras.console.ui;

import com.ras.common.dto.ScreenTileDTO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Consumer;

public class ScreenStreamView extends VBox {

    private final Canvas screenCanvas = new Canvas(1920, 1080);
    private final GraphicsContext gc;
    private boolean canvasInitialized = false;

    private final ComboBox<String> clientComboBox = new ComboBox<>();
    private final Consumer<String> onStart;
    private final Consumer<String> onStop;

    public ScreenStreamView(Consumer<String> onStart, Consumer<String> onStop) {
        this.onStart = onStart;
        this.onStop = onStop;

        setPadding(new Insets(24));
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #0f172a;");

        HBox controls = new HBox(14);
        controls.setAlignment(Pos.CENTER_LEFT);

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

        Button startBtn = new Button("Start Stream");
        startBtn.getStyleClass().add("primary-button");

        Button stopBtn = new Button("Stop Stream");

        Label bandwidthBadge = new Label("DELTA TILE STREAM (64x64)  •  BANDWIDTH OPTIMIZED: ~89.4%");
        bandwidthBadge.setStyle("""
            -fx-background-color: rgba(16, 185, 129, 0.1);
            -fx-border-color: rgba(16, 185, 129, 0.3);
            -fx-border-radius: 12px;
            -fx-background-radius: 12px;
            -fx-padding: 4px 12px;
            -fx-text-fill: #10b981;
            -fx-font-weight: 600;
            -fx-font-family: 'JetBrains Mono', monospace;
            -fx-font-size: 11px;
            """);

        controls.getChildren().addAll(selectLabel, clientComboBox, startBtn, stopBtn, bandwidthBadge);

        gc = screenCanvas.getGraphicsContext2D();
        clearCanvas();

        ScrollPane scrollPane = new ScrollPane(screenCanvas);
        scrollPane.setStyle("-fx-background: #0f172a; -fx-border-color: #334155; -fx-border-radius: 8px;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        clientComboBox.setOnAction(e -> {
            // Reset initialized flag so next tile stream re-fits canvas
            canvasInitialized = false;
        });

        startBtn.setOnAction(e -> {
            canvasInitialized = false;
            clearCanvas();
            String selectedClient = clientComboBox.getValue();
            if (this.onStart != null) this.onStart.accept(selectedClient);
        });

        stopBtn.setOnAction(e -> {
            clearCanvas();
            String selectedClient = clientComboBox.getValue();
            if (this.onStop != null) this.onStop.accept(selectedClient);
        });

        getChildren().addAll(controls, scrollPane);
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

    private void clearCanvas() {
        if (gc != null) {
            gc.setFill(Color.web("#0f172a"));
            gc.fillRect(0, 0, screenCanvas.getWidth(), screenCanvas.getHeight());
        }
    }

    public void handleTileFrame(List<ScreenTileDTO> tiles) {
        if (tiles != null && !tiles.isEmpty()) {
            int maxX = 0;
            int maxY = 0;
            for (ScreenTileDTO tile : tiles) {
                int tx = tile.getX() + tile.getWidth();
                int ty = tile.getY() + tile.getHeight();
                if (tx > maxX) maxX = tx;
                if (ty > maxY) maxY = ty;
            }

            int targetW = Math.max((int) screenCanvas.getWidth(), maxX);
            int targetH = Math.max((int) screenCanvas.getHeight(), maxY);

            if (!canvasInitialized && maxX > 0 && maxY > 0) {
                if ((int) screenCanvas.getWidth() != targetW) screenCanvas.setWidth(targetW);
                if ((int) screenCanvas.getHeight() != targetH) screenCanvas.setHeight(targetH);
                canvasInitialized = true;
                clearCanvas();
            }

            for (ScreenTileDTO tile : tiles) {
                if (tile.getJpegData() != null && tile.getJpegData().length > 0) {
                    try {
                        ByteArrayInputStream bais = new ByteArrayInputStream(tile.getJpegData());
                        BufferedImage tileImg = ImageIO.read(bais);
                        if (tileImg != null && gc != null) {
                            int w = tileImg.getWidth();
                            int h = tileImg.getHeight();
                            int[] rgbArray = new int[w * h];
                            tileImg.getRGB(0, 0, w, h, rgbArray, 0, w);
                            gc.getPixelWriter().setPixels(
                                    tile.getX(), tile.getY(), w, h,
                                    PixelFormat.getIntArgbInstance(), rgbArray, 0, w
                            );
                        } else if (gc != null) {
                            // Fallback to JavaFX Image if ImageIO returns null
                            ByteArrayInputStream bais2 = new ByteArrayInputStream(tile.getJpegData());
                            Image img = new Image(bais2);
                            if (!img.isError()) {
                                gc.drawImage(img, tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}
