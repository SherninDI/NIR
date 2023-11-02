package com.example.nir;

import android.util.Log;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class GroupFormat {
    private byte[] byteArray;
    private CheckSum checkSum = new CheckSum();

    public GroupFormat(byte[] group) {
        byteArray = group;
    }

//    public void writeGroup(String title, String time, byte mode, byte spectre, byte maxFreq) {
//
//        String fileIdStr = "EG01";
//        byte[] fileIdBytes = fileIdStr.getBytes(Charset.forName("windows-1251"));
//        writeBytes(fileIdBytes, 0);
//
//        byte titleLengthByte = (byte)title.length();
//        writeByte(titleLengthByte, 4);
//        byte[] titleBytes = reverseByteArray(title.getBytes(Charset.forName("windows-1251")));
//        writeBytes(titleBytes, 5);
//
//        int timeInt = Integer.parseInt(time);
//        byte[] timeBytes = reverseByteArray(BigInteger.valueOf(timeInt).toByteArray());
//        writeBytes(timeBytes, 20);
//
//        writeByte(mode, 22);
//
//        writeByte(spectre, 23);
//
//        writeByte(maxFreq, 24);
//    }

    public void writeFileId() {
        String fileIdStr = "EG01";
        byte[] fileIdBytes = fileIdStr.getBytes(Charset.forName("windows-1251"));
        writeBytes(fileIdBytes, 0);
    }

    public void writeTitle(String title) {
        byte titleLengthByte = (byte)title.length();
        writeByte(titleLengthByte, 4);
        byte[] titleBytes = reverseByteArray(title.getBytes(Charset.forName("windows-1251")));
        writeBytes(titleBytes, 5);
    }

    public String readTitle() {
        int length = readByte(4);
        byte[] title = reverseByteArray(readBytes(length, 5));
        return new String(title,Charset.forName("windows-1251"));
    }
    public int readTitleLength() {
        return readByte(4);
    }


    public void writeTime(String time) {
        int timeInt = Integer.parseInt(time);
        byte[] timeBytes = reverseByteArray(BigInteger.valueOf(timeInt).toByteArray());
        writeBytes(timeBytes, 20);
    }

    public String readTime() {
        byte[] time = reverseByteArray(readBytes(2, 20));
        int timeInt = new BigInteger(time).intValue();
        return String.valueOf(timeInt & 0xffff);
    }

    public void writeMode(byte mode) {
        writeByte(mode, 22);
    }

    public int readMode() {
        return readByte(22);
    }

    public void writeSpectre(byte spectre) {
        writeByte(spectre, 23);
    }

    public int readSpectre() {
        return readByte(23);
    }

    public void writeMaxFreq(byte maxFreq) {
        writeByte(maxFreq, 24);
    }

    public int readMaxFreq() {
        return readByte(24);
    }

    public void writeStepCount(int stepCount) {
        writeByte((byte) stepCount, 26);
    }

    public int readStepCount() {
        return readByte(26);
    }

    public void writeStep(String type, int code, int ampl, int stepTime, int stepPos) {
        byte[] typeBytes = type.getBytes(Charset.forName("windows-1251"));
        writeBytes(typeBytes, 27 + stepPos * 6);
        byte[] codeBytes = reverseByteArray(BigInteger.valueOf(code).toByteArray());
        writeBytes(codeBytes, 27 + stepPos * 6 + 1);
        byte[] amplBytes = BigInteger.valueOf(ampl).toByteArray();
        writeBytes(amplBytes, 27 + stepPos * 6 + 3);
        byte[] stepTimeBytes = reverseByteArray(BigInteger.valueOf(stepTime).toByteArray());
        writeBytes(stepTimeBytes, 27 + stepPos * 6 + 5);
    }

    public void writeAmpl(int ampl, int stepPos) {
        byte[] amplBytes = BigInteger.valueOf(ampl).toByteArray();
        writeBytes(amplBytes, 27 + stepPos * 6 + 3);

    }
    public int readAmpl(int stepPos) {
        byte[] amplBytes = readBytes(1,27 + stepPos * 6 + 3);
        return new BigInteger(amplBytes).intValue();
    }

    public void writeStepTime(int stepTime, int stepPos) {
        byte[] stepTimeBytes = reverseByteArray(BigInteger.valueOf(stepTime).toByteArray());
        writeBytes(stepTimeBytes, 27 + stepPos * 6 + 5);
    }

    public int readStepTime(int stepPos) {
        byte[] stepTimeBytes = readBytes(2,27 + stepPos * 6 + 5);
        return new BigInteger(stepTimeBytes).intValue();
    }

    public byte[] readStep(int stepPos) {
        return readBytes(6,27 + stepPos * 6);
    }

    public int readValue(int stepPos) {
        byte[] codeBytes = reverseByteArray(readBytes(2, 27 + stepPos * 6 + 1));
        return new BigInteger(codeBytes).intValue();
    }


    public void deleteStep(int position) {
        int stepLength = 6;
        int stepsLength = 480;
        byte[] step = new byte[stepLength];
        int afterPos = (position + 1) * stepLength;
        int beforePosStart = 0;
        int beforePosEnd = position * stepLength;
        if (position == 0) {
            byte[] after = readBytes(stepsLength - stepLength, afterPos);
            writeBytes(after, beforePosStart);
            writeBytes(step, stepsLength - stepLength);

        } else {
            byte[] before = readBytes(beforePosEnd, beforePosStart);
            byte[] after = readBytes(stepsLength - afterPos, afterPos);
            writeBytes(before, beforePosStart);
            writeBytes(after, beforePosEnd);
            writeBytes(step, stepsLength - stepLength);
        }
    }

    public void writeSteps(byte[] steps) {
        writeBytes(steps,27);
    }

    public byte[] readSteps() {
        return readBytes(480,27);
    }

    public void writeCRC() {
        byte[] crc32Byte = readBytes(503,4);
        int crc32 = checkSum.CRC32sum(crc32Byte);
        writeBytes(reverseByteArray(BigInteger.valueOf(crc32).toByteArray()),507);
    }


    public void writeExec(int exec) {
        writeByte((byte) exec, 511);
    }

    public int readExec() {
        return readByte(511);
    }


    private void writeBytes(byte[] data, int position) {
        if (position < 0 || position >= byteArray.length) {
            throw new IllegalArgumentException("Invalid position");
        }

        int dataLength = Math.min(data.length, byteArray.length - position);
        System.arraycopy(data, 0, byteArray, position, dataLength);
    }

    public void writeByte(byte b, int position) {
        if (position < 0 || position >= byteArray.length) {
            throw new IllegalArgumentException("Invalid position");
        }
        byteArray[position] = b;
    }

    public byte[] readBytes(int length, int position) {
        if (position < 0 || position >= byteArray.length) {
            throw new IllegalArgumentException("Invalid position");
        }

        int maxLength = Math.min(length, byteArray.length - position);
        byte[] data = new byte[maxLength];
        System.arraycopy(byteArray, position, data, 0, maxLength);
        return data;
    }

    public byte readByte(int position) {
        if (position < 0 || position >= byteArray.length) {
            throw new IllegalArgumentException("Invalid position");
        }
        return byteArray[position];
    }

    private byte[] reverseByteArray(byte[] in) {
        byte[] out = new byte[in.length];
        for(int j = 0; j < in.length; j++) {
            out[j] = in[in.length - 1 - j];
        }
        return out;
    }

    public byte[] getBytes() {
        return byteArray;
    }
}
