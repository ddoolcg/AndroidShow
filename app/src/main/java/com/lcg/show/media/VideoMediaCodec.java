package com.lcg.show.media;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import com.lcg.show.MainActivity;
import com.lcg.show.screen.Constant;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoMediaCodec extends MediaCodecBase {
    private Surface mSurface;
    private byte[] configbyte;

    public VideoMediaCodec() {
        prepare();
    }

    public Surface getSurface() {
        return mSurface;
    }

    public void isRun(boolean isR) {
        this.isRun = isR;
    }


    @Override
    public void prepare() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(Constant.MIME_TYPE, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, Constant.VIDEO_BITRATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, Constant.VIDEO_FRAMERATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constant.VIDEO_IFRAME_INTER);
            mEncoder = MediaCodec.createEncoderByType(Constant.MIME_TYPE);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mEncoder.createInputSurface();
            mEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        this.isRun = false;
    }

    /**
     * 获取h264数据
     **/
    public void getBuffer() {
        try {
            while (isRun) {
                if (mEncoder == null)
                    break;
                MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
                int TIMEOUT_USEC = 2000;
                int outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = mEncoder.getOutputBuffers()[outputBufferIndex];
                    byte[] outData = new byte[mBufferInfo.size];
                    outputBuffer.get(outData);
                    if (mBufferInfo.flags == 2) {
                        configbyte = new byte[mBufferInfo.size];
                        configbyte = outData;
                    } else if (mBufferInfo.flags == 1) {
                        byte[] keyframe = new byte[mBufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                        MainActivity.putData(keyframe, 1, mBufferInfo.presentationTimeUs * 1000L);
                    } else {
                        MainActivity.putData(outData, 2, mBufferInfo.presentationTimeUs * 1000L);
                    }
                    mEncoder.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
