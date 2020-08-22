package com.xgimi.samba.streams;

import com.blankj.utilcode.util.LogUtils;
import com.hierynomus.smbj.ProgressListener;
import com.hierynomus.smbj.share.File;
import com.xgimi.samba.bean.ShareFile;
import com.xgimi.samba.utils.MethodAverageTime;

import java.io.IOException;
import java.io.InputStream;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/3 11:24
 * desc   :
 */
public class SharedInputStream extends InputStream {
    private static final String TAG = "SharedInputStream";
    /**
     * File that provides the input stream.
     */
    private final File file;
    private final ShareFile shareFile;
    /**
     * Input stream of the file that will be decorated.
     */
    private final InputStream inputStream;

    /**
     * Create a new decorated input stream that respects the reference couting close mechanism of the file.
     *
     * @param file File that will provide the input stream
     */
    public SharedInputStream(File file, ShareFile shareFile) {
        this.file = file;
        this.shareFile = shareFile;
        this.inputStream = file.getInputStream(new ProgressListener() {
            @Override
            public void onProgressChanged(long numBytes, long totalBytes) {
                LogUtils.d("DirectTcpPacketReader",
                        "onProgressChanged numBytes" + numBytes + ",totalBytes=" + totalBytes);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    private MethodAverageTime mMethodAverageTime = new MethodAverageTime(TAG);
    private long allSize;

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        allSize += len;
        LogUtils.d(TAG, "read off=" + off + ",len=" + len + ",allSize=" + allSize);
        mMethodAverageTime.start();
        int result = inputStream.read(b, off, len);
        mMethodAverageTime.end(false);
        double ave = mMethodAverageTime.average / 1000f;
        ave = ave == 0 ? 1 : ave;
        LogUtils.d(TAG, "speed=" + (int) ((len / 1024f) / ave) + "kb/s");
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        LogUtils.d(TAG, "skip to " + n);
        return inputStream.skip(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
        file.close();
        shareFile.getShareClient().getSession().close();
        LogUtils.d(TAG, "close");
    }
}

