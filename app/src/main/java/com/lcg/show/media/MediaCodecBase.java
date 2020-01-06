package com.lcg.show.media;

import android.media.MediaCodec;

public abstract class MediaCodecBase {
    MediaCodec mEncoder;
    boolean isRun = false;

    public abstract void prepare();

    public abstract void release();
}
