package com.xgimi.samba.bean;

import com.hierynomus.smbj.share.DiskShare;
import com.xgimi.samba.ShareItem;
import com.xgimi.samba.core.ShareClient;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:49
 * desc   :
 */
public class ShareDisk extends AbstractShareItem<ShareDisk> {

    public ShareDisk(ShareClient connection, String sharedName) {
        super(connection, null, "");
        this.shareName = sharedName;
    }

    public ShareDisk(ShareClient connection, DiskShare diskShare) {
        super(connection, diskShare, "");
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ShareDisk) {
            return getShareName().equals(((ShareDisk) object).getShareName());
        }
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public ShareItem renameTo(String newFileName, boolean replaceIfExist) {
        return null;
    }

    @Override
    public ShareItem getParentFile() {
        if (isRoot()) {
            return null;
        }
        return getShareClient().getRootShareItem();
    }

    @Override
    public String getShareName() {
        return shareName != null ? shareName : super.getShareName();
    }

    @Override
    public String getName() {
        return shareName;
    }
}

