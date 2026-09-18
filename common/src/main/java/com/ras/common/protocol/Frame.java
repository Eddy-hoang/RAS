package com.ras.common.protocol;

import java.util.Arrays;
import java.util.Objects;

public class Frame {
    public static final short MAGIC_HEADER = 0x5352; // "SR"
    public static final byte PROTOCOL_VERSION = 0x01;
    public static final int HEADER_SIZE = 12; // 4 + 4 + 4
    public static final int MAX_PAYLOAD_SIZE = 16 * 1024 * 1024; // 16 MB limit

    private final FrameType frameType;
    private final CommandType commandType;
    private final byte[] payload;

    public Frame(FrameType frameType, CommandType commandType, byte[] payload) {
        this.frameType = Objects.requireNonNull(frameType, "frameType cannot be null");
        this.commandType = Objects.requireNonNull(commandType, "commandType cannot be null");
        this.payload = payload != null ? payload : new byte[0];

        if (this.payload.length > MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("Payload size (" + this.payload.length + 
                ") exceeds maximum allowed size of " + MAX_PAYLOAD_SIZE + " bytes");
        }
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getPayloadLength() {
        return payload.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frame frame = (Frame) o;
        return frameType == frame.frameType &&
               commandType == frame.commandType &&
               Arrays.equals(payload, frame.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(frameType, commandType);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "frameType=" + frameType +
                ", commandType=" + commandType +
                ", payloadLength=" + payload.length +
                '}';
    }
}
