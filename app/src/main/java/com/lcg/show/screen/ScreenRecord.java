package com.lcg.show.screen;

import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjection;
import android.view.Surface;

import com.lcg.show.media.VideoMediaCodec;


public class ScreenRecord extends Thread {

    private MediaProjection mMediaProjection;

    private VideoMediaCodec mVideoMediaCodec;

    public ScreenRecord(MediaProjection mp) {
        this.mMediaProjection = mp;
        mVideoMediaCodec = new VideoMediaCodec();
    }

    @Override
    public void run() {
        mVideoMediaCodec.prepare();
        Surface surface = mVideoMediaCodec.getSurface();
        mMediaProjection.createVirtualDisplay("display", Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT, Constant.VIDEO_DPI, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface, null, null);
        mVideoMediaCodec.isRun(true);
        mVideoMediaCodec.getBuffer();
    }

    /**
     * 停止
     **/
    public void release() {
        mVideoMediaCodec.release();
    }

}
