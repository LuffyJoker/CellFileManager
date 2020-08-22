package com.xgimi.dlna.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/8/4 10:15
 * desc   :
 */
public final class FileUtil {
    public final static byte[] load(String fileName) {
        try {
            FileInputStream fin = new FileInputStream(fileName);
            return load(fin);
        } catch (Exception e) {
            Debug.warning(e);
            return new byte[0];
        }
    }

    public final static byte[] load(File file) {
        try {
            FileInputStream fin = new FileInputStream(file);
            return load(fin);
        } catch (Exception e) {
            Debug.warning(e);
            return new byte[0];
        }
    }

    public final static byte[] load(FileInputStream fin) {
        byte readBuf[] = new byte[512 * 1024];

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            int readCnt = fin.read(readBuf);
            while (0 < readCnt) {
                bout.write(readBuf, 0, readCnt);
                readCnt = fin.read(readBuf);
            }

            fin.close();

            return bout.toByteArray();
        } catch (Exception e) {
            Debug.warning(e);
            return new byte[0];
        }
    }

    public final static boolean isXMLFileName(String name) {
        if (StringUtil.hasData(name) == false) {
            return false;
        }
        String lowerName = name.toLowerCase();
        return lowerName.endsWith("xml");
    }
}


