package com.example.expressionr;

public class Result {

    private static int mNumber;
    private static float mProbability;
    private static long mTimeCost;

    public Result(float[] probs, long timeCost) {
        mNumber = argmax(probs);
        mProbability = probs[mNumber];
        mTimeCost = timeCost;
    }

    public static int getNumber() {
        return mNumber;
    }

    public static float getProbability() {
        return mProbability;
    }

    public static long getTimeCost() {
        return mTimeCost;
    }

    private static int argmax(float[] probs) {
        int maxIdx = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

}
