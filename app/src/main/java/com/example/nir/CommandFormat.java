package com.example.nir;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class CommandFormat {
    public byte[] command = new byte[11];
    private CheckSum checkSum = new CheckSum();
    public byte[] getCommand(int sys, byte[] data) {
        byte[] result = new byte[11];
        switch (sys) {
            case Constants.SYS_NOP:
                data[0] = Constants.SYS_NOP;
                command = generateCommand(data);
                break;
            case Constants.SYS_RESET:
                data[0] = Constants.SYS_RESET;
                command = generateCommand(data);
                break;
            case Constants.SYS_PUT:
                ByteBuffer putDataBuffer = ByteBuffer.allocate(9);
                putDataBuffer.put((byte)Constants.SYS_PUT);
                putDataBuffer.put("EG**".getBytes(StandardCharsets.UTF_8));
                byte[] revLenByte = reverseByteArray(ByteBuffer.allocate(4).putInt(data.length).array());
                putDataBuffer.put(revLenByte);
                data = putDataBuffer.array();
                command = generateCommand(data);
                break;
            case Constants.SYS_GET:
                ByteBuffer getBuffer = ByteBuffer.allocate(9);
                getBuffer.put((byte)Constants.SYS_GET);
                getBuffer.put("EG**".getBytes(StandardCharsets.UTF_8));
                byte[] nulls = new byte[4];
                getBuffer.put(nulls);
                command = generateCommand(getBuffer.array());
                break;
            case Constants.SYS_CANCEL:
                data[0] = Constants.SYS_CANCEL;
                command = generateCommand(data);
                break;
            case Constants.SYS_DATA:
                ByteBuffer dataBuffer = ByteBuffer.allocate(9);
                dataBuffer.put((byte) Constants.SYS_DATA);
                dataBuffer.put(data);
                command = generateCommand(dataBuffer.array());
                break;
        }
        return command;
    }

    private byte[] generateCommand(byte[] bytes) {
        byte header = 0x01;
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.put(header);
        bb.put(bytes);
        byte sum = checkSum.CRC8sum(bb.array(), bb.array().length);
        ByteBuffer commandBuffer = ByteBuffer.wrap(command);
        commandBuffer.put(bb.array());
        commandBuffer.put(sum);
        return commandBuffer.array();
    }

    private byte[] reverseByteArray(byte[] in) {
        byte[] out = new byte[in.length];
        for(int j = 0; j < in.length; j++) {
            out[j] = in[in.length - 1 - j];
        }
        return out;
    }
}
