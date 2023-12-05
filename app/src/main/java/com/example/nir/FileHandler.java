package com.example.nir;

import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;

public class FileHandler {
    private File file;
    private int groupSize = 512;
    private RandomAccessFile randomAccessFile;

    public FileHandler(File file) {
        try {
            this.file = file;
            this.randomAccessFile = new RandomAccessFile(file, "rw");

            randomAccessFile.setLength(51200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBytes(byte[] data) throws IOException {
        randomAccessFile.write(data);
    }

    public void writeBytesToPosition(byte[] data, int position) throws IOException {
        randomAccessFile.seek((long) position * groupSize);
        randomAccessFile.write(data);
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] data = new byte[length];
        randomAccessFile.read(data);
        return data;
    }

    public byte[] readBytesFromPosition(int position) throws IOException {
        randomAccessFile.seek((long) position * groupSize);
        byte[] data = new byte[groupSize];
        randomAccessFile.read(data);
        return data;
    }

    public void deleteBytesFromPosition(int position) throws IOException {
        byte[] zeros = new byte[groupSize];

        int afterPos = (position + 1) * groupSize;
        int beforePosStart = 0;
        int beforePosEnd = position * groupSize;

        byte[] before = new byte[beforePosEnd];
        byte[] after = new byte[(int)file.length() - afterPos];

        if (position == 0) {
            randomAccessFile.seek(afterPos);
            randomAccessFile.read(after);

            writeBytesToPosition(after, 0);
            writeBytesToPosition(zeros, (int)file.length() - afterPos);
        } else {
            randomAccessFile.seek(beforePosStart);
            randomAccessFile.read(before);
            randomAccessFile.seek(afterPos);
            randomAccessFile.read(after);

            writeBytesToPosition(before, beforePosStart);
            writeBytesToPosition(after, beforePosEnd / 512);
            writeBytesToPosition(zeros, ((int)file.length() - groupSize) / 512);
        }
    }

    public void close() {
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
