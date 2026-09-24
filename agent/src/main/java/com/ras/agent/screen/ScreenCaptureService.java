package com.ras.agent.screen;

import com.ras.common.dto.ScreenTileDTO;
import com.ras.common.util.ChecksumUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenCaptureService {
    private static final Logger log = LoggerFactory.getLogger(ScreenCaptureService.class);
    private static final int TILE_SIZE = 64; // 64x64 tiles

    private final Robot robot;
    private final Map<String, String> prevTileHashes = new HashMap<>();
    private final Dimension screenSize;
    private final Rectangle screenRect;
    private boolean streaming = false;

    public ScreenCaptureService() throws AWTException {
        this.robot = new Robot();
        this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenRect = new Rectangle(screenSize);
    }

    public synchronized List<ScreenTileDTO> captureDeltaFrame() throws IOException {
        BufferedImage fullImage = robot.createScreenCapture(screenRect);
        List<ScreenTileDTO> deltaTiles = new ArrayList<>();

        int cols = (int) Math.ceil((double) fullImage.getWidth() / TILE_SIZE);
        int rows = (int) Math.ceil((double) fullImage.getHeight() / TILE_SIZE);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * TILE_SIZE;
                int y = r * TILE_SIZE;
                int w = Math.min(TILE_SIZE, fullImage.getWidth() - x);
                int h = Math.min(TILE_SIZE, fullImage.getHeight() - y);

                BufferedImage tileImg = fullImage.getSubimage(x, y, w, h);
                byte[] jpegBytes = compressToJpeg(tileImg, 0.75f);
                String tileHash = ChecksumUtil.calculateSHA256(jpegBytes);
                String tileKey = x + "_" + y;

                String prevHash = prevTileHashes.get(tileKey);
                if (prevHash == null || !prevHash.equals(tileHash)) {
                    prevTileHashes.put(tileKey, tileHash);
                    deltaTiles.add(new ScreenTileDTO(x, y, w, h, tileHash, jpegBytes));
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Captured delta frame: {} changed tiles out of {} total tiles", deltaTiles.size(), cols * rows);
        }
        return deltaTiles;
    }

    private byte[] compressToJpeg(BufferedImage image, float quality) throws IOException {
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rgbImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean written = ImageIO.write(rgbImage, "jpg", baos);
        if (!written || baos.size() == 0) {
            baos.reset();
            ImageIO.write(rgbImage, "png", baos);
        }
        return baos.toByteArray();
    }

    public boolean isStreaming() { return streaming; }
    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
        if (streaming) {
            synchronized (this) {
                prevTileHashes.clear();
            }
        }
    }
}
