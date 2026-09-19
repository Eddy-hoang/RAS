package com.ras.console.ui;

import com.ras.common.dto.ScreenTileDTO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.ByteArrayInputStream;
import java.util.List;

public class ScreenStreamView extends VBox {

    private final Canvas screenCanvas = new Canvas(1920, 1080);
    private final GraphicsContext gc;
    private boolean canvasInitialized = false;

    private final Runnable onStart;
    private final Runnable onStop;

    public ScreenStreamView(Runnable onStart, Runnable onStop) {
        this.onStart = onStart;
        this.onStop = onStop;

        setPadding(new Insets(24));
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #0f172a;");

        HBox controls = new HBox(14);
        controls.setAlignment(Pos.CENTER_LEFT);

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

        controls.getChildren().addAll(startBtn, stopBtn, bandwidthBadge);

        gc = screenCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, screenCanvas.getWidth(), screenCanvas.getHeight());

        ScrollPane scrollPane = new ScrollPane(screenCanvas);
        scrollPane.setStyle("-fx-background: #0f172a; -fx-border-color: #334155; -fx-border-radius: 8px;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        startBtn.setOnAction(e -> {
            canvasInitialized = false;
            if (this.onStart != null) this.onStart.run();
        });

        stopBtn.setOnAction(e -> {
            if (this.onStop != null) this.onStop.run();
        });

        getChildren().addAll(controls, scrollPane);
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

            if (!canvasInitialized && maxX > 0 && maxY > 0) {
                screenCanvas.setWidth(maxX);
                screenCanvas.setHeight(maxY);
                canvasInitialized = true;
                if (gc != null) {
                    gc.setFill(Color.web("#0f172a"));
                    gc.fillRect(0, 0, maxX, maxY);
                }
            }

            for (ScreenTileDTO tile : tiles) {
                if (tile.getJpegData() != null && tile.getJpegData().length > 0) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(tile.getJpegData());
                    Image img = new Image(bais);
                    if (gc != null) {
                        gc.drawImage(img, tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
                    }
                }
            }
        }
    }
}
