package com.ras.common.dto;

public class ScreenTileDTO {
    private int x;
    private int y;
    private int width;
    private int height;
    private String tileHash;
    private byte[] jpegData;

    public ScreenTileDTO() {}

    public ScreenTileDTO(int x, int y, int width, int height, String tileHash, byte[] jpegData) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tileHash = tileHash;
        this.jpegData = jpegData;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public String getTileHash() { return tileHash; }
    public void setTileHash(String tileHash) { this.tileHash = tileHash; }

    public byte[] getJpegData() { return jpegData; }
    public void setJpegData(byte[] jpegData) { this.jpegData = jpegData; }
}
