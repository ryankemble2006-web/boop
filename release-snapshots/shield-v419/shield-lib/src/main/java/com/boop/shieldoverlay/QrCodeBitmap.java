package com.boop.shieldoverlay;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public final class QrCodeBitmap {
    private QrCodeBitmap() { }

    public static Bitmap render(String payload, int size) {
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("QR payload is required");
        }
        if (size < 1) {
            throw new IllegalArgumentException("QR size must be positive");
        }

        final BitMatrix matrix;
        try {
            matrix = new MultiFormatWriter().encode(
                    payload,
                    BarcodeFormat.QR_CODE,
                    size,
                    size);
        } catch (WriterException e) {
            throw new IllegalArgumentException("Could not make pairing code", e);
        }

        int[] pixels = new int[size * size];
        for (int y = 0; y < size; y++) {
            int row = y * size;
            for (int x = 0; x < size; x++) {
                pixels[row + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888);
    }
}
