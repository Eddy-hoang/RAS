package com.ras.common.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FrameCodec {

    /**
     * Serializes a Frame into byte array representation matching 12-byte header specs.
     * [Magic 2B][Version 1B][Reserved 1B][Length 4B][FrameType 2B][CommandType 2B][Payload N B]
     */
    public static byte[] encode(Frame frame) {
        int payloadLen = frame.getPayloadLength();
        byte[] buffer = new byte[Frame.HEADER_SIZE + payloadLen];
        ByteBuffer bb = ByteBuffer.wrap(buffer);

        // 4 bytes: Magic (2B) + Version (1B) + Reserved (1B)
        bb.putShort(Frame.MAGIC_HEADER);
        bb.put(Frame.PROTOCOL_VERSION);
        bb.put((byte) 0x00); // Reserved byte

        // 4 bytes: Payload Size
        bb.putInt(payloadLen);

        // 4 bytes: FrameType (2B) + CommandType (2B)
        bb.putShort((short) frame.getFrameType().getId());
        bb.putShort((short) frame.getCommandType().getId());

        // Payload
        if (payloadLen > 0) {
            bb.put(frame.getPayload());
        }

        return buffer;
    }

    /**
     * Encodes and writes a Frame directly to an OutputStream.
     */
    public static void writeFrame(OutputStream out, Frame frame) throws IOException {
        byte[] bytes = encode(frame);
        out.write(bytes);
        out.flush();
    }

    /**
     * Reads a full Frame synchronously from an InputStream.
     */
    public static Frame readFrame(InputStream in) throws IOException {
        DataInputStream dis = (in instanceof DataInputStream) ? (DataInputStream) in : new DataInputStream(in);

        // Read 12-byte Header
        short magic = dis.readShort();
        if (magic != Frame.MAGIC_HEADER) {
            throw new IOException(String.format("Invalid protocol magic header: 0x%04X (expected 0x%04X)", magic, Frame.MAGIC_HEADER));
        }

        byte version = dis.readByte();
        if (version != Frame.PROTOCOL_VERSION) {
            throw new IOException(String.format("Unsupported protocol version: %d (expected %d)", version, Frame.PROTOCOL_VERSION));
        }

        dis.readByte(); // skip reserved

        int payloadLen = dis.readInt();
        if (payloadLen < 0 || payloadLen > Frame.MAX_PAYLOAD_SIZE) {
            throw new IOException("Illegal payload size: " + payloadLen);
        }

        short frameTypeId = dis.readShort();
        short commandTypeId = dis.readShort();

        FrameType frameType = FrameType.fromId(frameTypeId & 0xFFFF);
        CommandType commandType = CommandType.fromId(commandTypeId & 0xFFFF);

        byte[] payload = new byte[payloadLen];
        if (payloadLen > 0) {
            dis.readFully(payload);
        }

        return new Frame(frameType, commandType, payload);
    }

    /**
     * Decode frames from an accumulating ByteBuffer (useful for NIO or async stream processing).
     * Modifies position of buffer as frames are consumed.
     */
    public static List<Frame> decodeBuffer(ByteBuffer buffer) throws IOException {
        List<Frame> frames = new ArrayList<>();
        while (buffer.remaining() >= Frame.HEADER_SIZE) {
            buffer.mark();
            
            short magic = buffer.getShort();
            if (magic != Frame.MAGIC_HEADER) {
                throw new IOException(String.format("Invalid protocol magic header: 0x%04X", magic));
            }

            byte version = buffer.get();
            if (version != Frame.PROTOCOL_VERSION) {
                throw new IOException(String.format("Unsupported protocol version: %d", version));
            }

            buffer.get(); // reserved byte

            int payloadLen = buffer.getInt();
            if (payloadLen < 0 || payloadLen > Frame.MAX_PAYLOAD_SIZE) {
                throw new IOException("Illegal payload size: " + payloadLen);
            }

            if (buffer.remaining() < 4 + payloadLen) {
                // Incomplete frame, reset buffer position to mark and wait for more data
                buffer.reset();
                break;
            }

            short frameTypeId = buffer.getShort();
            short commandTypeId = buffer.getShort();

            FrameType frameType = FrameType.fromId(frameTypeId & 0xFFFF);
            CommandType commandType = CommandType.fromId(commandTypeId & 0xFFFF);

            byte[] payload = new byte[payloadLen];
            if (payloadLen > 0) {
                buffer.get(payload);
            }

            frames.add(new Frame(frameType, commandType, payload));
        }
        return frames;
    }
}
