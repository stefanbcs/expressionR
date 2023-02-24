package com.example.expressionr;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


public class Classifier {

    /** Reads label list from Assets. */
    public static List<String> loadLabelList(Activity activity, String labelsName) throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(activity.getAssets().open(labelsName)));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    /** Memory-map the model file in Assets. */
    public static MappedByteBuffer loadModelFile(Activity activity, String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    public static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap, int IMG_HEIGHT, int IMG_WIDTH, int NUM_CHANNEL) {
        ByteBuffer mImageData;
        mImageData = ByteBuffer.allocateDirect(4 * IMG_HEIGHT * IMG_WIDTH * NUM_CHANNEL);
        mImageData.order(ByteOrder.nativeOrder());

        int[] mImagePixels = new int[IMG_HEIGHT * IMG_WIDTH]; //array pentru a prelua valorile pixelilor bitmapului
        bitmap.getPixels(mImagePixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int min = min(mImagePixels);
        int max = max(mImagePixels);

        int pixel = 0;
        for (int i = 0; i < IMG_WIDTH; ++i) {
            for (int j = 0; j < IMG_HEIGHT; ++j) {
                int value = mImagePixels[pixel++];
                mImageData.putFloat(normalizePixel(convertPixel(value), min, max));
            }
        }
        return mImageData;
    }


    //rgb to gray
    private static float convertPixel(int color) {
        return (255 - (((color >> 16) & 0xFF) * 0.299f
                + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f)) / 255.0f;

    }

    private static float normalizePixel(float pixel, int min, int max) {
        int newMin = -1;
        int newMax = 1;
        float norm = ((pixel-min)*(newMax-newMin)/(max-min))+newMin;
        norm = (float)Math.round(norm * 100000f) / 100000f;
        return norm;
    }

    private static int min(int[] imgPixels){
        int min = (int) convertPixel(imgPixels[0]);
        for (int p: imgPixels) {
            p = (int) convertPixel(p);
            if(min > p)
                min = p;
        }
        return min;
    }

    private static int max(int[] imgPixels){
        int max = (int) convertPixel(imgPixels[0]);
        for (int p: imgPixels) {
            p = (int) convertPixel(p);
            if(max < p)
                max = p;
        }
        return max;
    }

}
