package com.boop.alpha1;

import android.content.Context;

import com.k2fsa.sherpa.onnx.FeatureConfig;
import com.k2fsa.sherpa.onnx.KeywordSpotter;
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig;
import com.k2fsa.sherpa.onnx.KeywordSpotterResult;
import com.k2fsa.sherpa.onnx.OnlineModelConfig;
import com.k2fsa.sherpa.onnx.OnlineStream;
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig;

final class BoopSherpaWakeSpotter implements AutoCloseable {
    private static final int SAMPLE_RATE_HZ = 16_000;

    private KeywordSpotter spotter;
    private OnlineStream stream;

    BoopSherpaWakeSpotter(Context context) {
        FeatureConfig feature = new FeatureConfig();
        feature.setSampleRate(SAMPLE_RATE_HZ);
        feature.setFeatureDim(80);
        feature.setDither(0.0f);

        OnlineTransducerModelConfig transducer = new OnlineTransducerModelConfig();
        transducer.setEncoder("boop-kws/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx");
        transducer.setDecoder("boop-kws/decoder-epoch-12-avg-2-chunk-16-left-64.onnx");
        transducer.setJoiner("boop-kws/joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx");

        OnlineModelConfig model = new OnlineModelConfig();
        model.setTransducer(transducer);
        model.setTokens("boop-kws/tokens.txt");
        model.setNumThreads(2);
        model.setProvider("cpu");
        model.setModelType("zipformer2");

        KeywordSpotterConfig config = new KeywordSpotterConfig();
        config.setFeatConfig(feature);
        config.setModelConfig(model);
        config.setKeywordsFile("boop-kws/keywords.txt");
        config.setMaxActivePaths(4);
        config.setKeywordsScore(BoopWakeSensitivity.keywordScore(context));
        config.setKeywordsThreshold(0.25f);
        config.setNumTrailingBlanks(1);

        spotter = new KeywordSpotter(context.getAssets(), config);
        stream = spotter.createStream("");
    }

    boolean accept(short[] pcm, int count) {
        if (spotter == null || stream == null || pcm == null || count <= 0) {
            return false;
        }

        int bounded = Math.min(count, pcm.length);
        float[] samples = new float[bounded];
        for (int i = 0; i < bounded; i++) {
            samples[i] = pcm[i] / 32768.0f;
        }

        stream.acceptWaveform(samples, SAMPLE_RATE_HZ);
        while (spotter.isReady(stream)) {
            spotter.decode(stream);
        }

        KeywordSpotterResult result = spotter.getResult(stream);
        String keyword = result == null ? null : result.getKeyword();
        if (keyword != null && !keyword.isEmpty() && keyword.equalsIgnoreCase("BOOP")) {
            spotter.reset(stream);
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        if (stream != null) {
            stream.release();
            stream = null;
        }
        if (spotter != null) {
            spotter.release();
            spotter = null;
        }
    }
}
