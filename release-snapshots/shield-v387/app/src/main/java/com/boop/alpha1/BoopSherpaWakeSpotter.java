package com.boop.alpha1;

import android.content.Context;
import android.util.Log;

import com.k2fsa.sherpa.onnx.FeatureConfig;
import com.k2fsa.sherpa.onnx.KeywordSpotter;
import com.k2fsa.sherpa.onnx.KeywordSpotterConfig;
import com.k2fsa.sherpa.onnx.KeywordSpotterResult;
import com.k2fsa.sherpa.onnx.OnlineModelConfig;
import com.k2fsa.sherpa.onnx.OnlineStream;
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class BoopSherpaWakeSpotter implements AutoCloseable {
    private static final String TAG = "BOOP-Wake";
    private static final int SAMPLE_RATE_HZ = 16_000;
    private static final String KEYWORDS_ASSET = "boop-kws/keywords.txt";
    private static final String BPE_ASSET = "boop-kws/bpe.model";

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
        config.setKeywordsFile(KEYWORDS_ASSET);
        config.setMaxActivePaths(4);
        config.setKeywordsScore(BoopWakeSensitivity.keywordScore(context));
        config.setKeywordsThreshold(0.25f);
        config.setNumTrailingBlanks(0);

        spotter = new KeywordSpotter(context.getAssets(), config);
        String selectedName = BoopWakeNameStore.load(context);
        String combined = null;
        if (!BoopWakeName.isDefault(selectedName)) {
            try {
                String baseKeywords = readAssetText(context, KEYWORDS_ASSET);
                BoopSentencePieceBpe tokenizer = new BoopSentencePieceBpe(readAssetBytes(context, BPE_ASSET));
                combined = BoopWakeKeywordBuilder.combinedKeywords(baseKeywords, tokenizer, selectedName);
            } catch (Throwable error) {
                Log.w(TAG, "Custom wake name could not be prepared; BOOP fallback remains active", error);
            }
        }
        if (combined == null || combined.isBlank()) {
            stream = spotter.createStream("");
        } else {
            try {
                stream = spotter.createStream(combined);
            } catch (Throwable error) {
                Log.w(TAG, "Custom wake stream failed; using BOOP fallback only", error);
                stream = spotter.createStream("");
            }
        }
    }

    boolean accept(short[] pcm, int count) {
        if (spotter == null || stream == null || pcm == null || count <= 0) return false;
        int bounded = Math.min(count, pcm.length);
        float[] samples = new float[bounded];
        for (int i = 0; i < bounded; i++) samples[i] = pcm[i] / 32768.0f;
        stream.acceptWaveform(samples, SAMPLE_RATE_HZ);
        while (spotter.isReady(stream)) spotter.decode(stream);
        KeywordSpotterResult result = spotter.getResult(stream);
        String keyword = result == null ? null : result.getKeyword();
        if (keyword != null && !keyword.isEmpty()) {
            spotter.reset(stream);
            return true;
        }
        return false;
    }

    private static String readAssetText(Context context, String path) throws IOException {
        return new String(readAssetBytes(context, path), StandardCharsets.UTF_8);
    }

    private static byte[] readAssetBytes(Context context, String path) throws IOException {
        try (InputStream input = context.getAssets().open(path); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }

    @Override public void close() {
        if (stream != null) { stream.release(); stream = null; }
        if (spotter != null) { spotter.release(); spotter = null; }
    }
}
