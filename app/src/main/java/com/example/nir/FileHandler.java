package com.example.nir;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileHandler {
    private File file;
    private RandomAccessFile randomAccessFile;

    public FileHandler(File file) {
        try {
            this.file = file;
            this.randomAccessFile = new RandomAccessFile(file, "rw");

// Устанавливаем размер файла 51200 байт
            randomAccessFile.setLength(51200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBytes(byte[] data) throws IOException {
        randomAccessFile.write(data);
    }

    public void writeBytesToPosition(byte[] data, int position) throws IOException {
        randomAccessFile.seek(position);
        randomAccessFile.write(data);
    }

    public byte[] readBytes(int length) throws IOException {
        byte[] data = new byte[length];
        randomAccessFile.read(data);
        return data;
    }

    public byte[] readBytesFromPosition(int length, int position) throws IOException {
        randomAccessFile.seek(position);
        byte[] data = new byte[length];
        randomAccessFile.read(data);
        return data;
    }

    public void close() {
        try {
            randomAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
