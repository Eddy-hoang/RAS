package com.ras.common.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FrameCodecTest {

    @Test
    @DisplayName("Encode and decode a normal control frame via InputStream/OutputStream")
    void testEncodeDecodeNormalFrame() throws IOException {
        String json = "{\"version\":1,\"requestId\":\"req-101\",\"type\":\"SYSTEM_INFO_REQUEST\"}";
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        Frame originalFrame = new Frame(FrameType.CONTROL, CommandType.SYSTEM_INFO_REQUEST, payload);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FrameCodec.writeFrame(baos, originalFrame);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Frame decodedFrame = FrameCodec.readFrame(bais);

        assertEquals(originalFrame.getFrameType(), decodedFrame.getFrameType());
        assertEquals(originalFrame.getCommandType(), decodedFrame.getCommandType());
        assertArrayEquals(originalFrame.getPayload(), decodedFrame.getPayload());
        assertEquals(originalFrame, decodedFrame);
    }

    @Test
    @DisplayName("Encode and decode a frame with empty payload")
    void testEncodeDecodeEmptyPayload() throws IOException {
        Frame originalFrame = new Frame(FrameType.HEARTBEAT, CommandType.HEARTBEAT_PING, new byte[0]);

        byte[] encodedBytes = FrameCodec.encode(originalFrame);
        assertEquals(Frame.HEADER_SIZE, encodedBytes.length);

        ByteArrayInputStream bais = new ByteArrayInputStream(encodedBytes);
        Frame decodedFrame = FrameCodec.readFrame(bais);

        assertEquals(FrameType.HEARTBEAT, decodedFrame.getFrameType());
        assertEquals(CommandType.HEARTBEAT_PING, decodedFrame.getCommandType());
        assertEquals(0, decodedFrame.getPayloadLength());
    }

    @Test
    @DisplayName("Decode multiple concatenated frames (packet coalescing) using ByteBuffer")
    void testPacketCoalescing() throws IOException {
        Frame frame1 = new Frame(FrameType.CONTROL, CommandType.HEARTBEAT_PING, "ping".getBytes());
        Frame frame2 = new Frame(FrameType.DATA, CommandType.FILE_CHUNK_DATA, new byte[]{1, 2, 3, 4, 5});

        byte[] bytes1 = FrameCodec.encode(frame1);
        byte[] bytes2 = FrameCodec.encode(frame2);

        ByteBuffer buffer = ByteBuffer.allocate(bytes1.length + bytes2.length);
        buffer.put(bytes1);
        buffer.put(bytes2);
        buffer.flip();

        List<Frame> decodedFrames = FrameCodec.decodeBuffer(buffer);
        assertEquals(2, decodedFrames.size());
        assertEquals(frame1, decodedFrames.get(0));
        assertEquals(frame2, decodedFrames.get(1));
        assertEquals(0, buffer.remaining());
    }

    @Test
    @DisplayName("Decode partial stream (fragmentation) wait for remaining data")
    void testPartialFrameFragmentation() throws IOException {
        Frame frame = new Frame(FrameType.CONTROL, CommandType.AUTH_REQUEST, "hello-auth".getBytes());
        byte[] fullBytes = FrameCodec.encode(frame);

        // Send only first 8 bytes (incomplete header + payload)
        ByteBuffer buffer = ByteBuffer.allocate(fullBytes.length);
        buffer.put(fullBytes, 0, 8);
        buffer.flip();

        List<Frame> decodedFrames = FrameCodec.decodeBuffer(buffer);
        assertEquals(0, decodedFrames.size(), "Should return no frames for incomplete header");
        assertEquals(8, buffer.remaining(), "Buffer position should reset to beginning of partial frame");

        // Now append remaining bytes
        buffer.compact();
        buffer.put(fullBytes, 8, fullBytes.length - 8);
        buffer.flip();

        decodedFrames = FrameCodec.decodeBuffer(buffer);
        assertEquals(1, decodedFrames.size());
        assertEquals(frame, decodedFrames.get(0));
    }

    @Test
    @DisplayName("Reject frame with invalid magic header")
    void testInvalidMagicHeader() {
        byte[] invalidHeader = new byte[]{0x12, 0x34, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x01};
        ByteArrayInputStream bais = new ByteArrayInputStream(invalidHeader);

        assertThrows(IOException.class, () -> FrameCodec.readFrame(bais));
    }

    @Test
    @DisplayName("Reject frame exceeding max payload size limit")
    void testExceedMaxPayloadSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Frame(FrameType.DATA, CommandType.FILE_CHUNK_DATA, new byte[Frame.MAX_PAYLOAD_SIZE + 1]);
        });
    }
}
