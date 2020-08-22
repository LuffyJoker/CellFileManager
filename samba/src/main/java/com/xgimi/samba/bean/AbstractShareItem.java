package com.xgimi.samba.bean;

import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;
import com.xgimi.samba.ShareItem;
import com.xgimi.samba.core.ShareClient;
import com.xgimi.samba.utils.ShareUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:21
 * desc   :
 */
public abstract class AbstractShareItem<T extends ShareItem> implements ShareItem {

    public static final String SHARE_ROOT = "smb:shareRoot";
    public static final String DISK_ROOT = "smb:diskRoot";
    public FileIdBothDirectoryInformation fileIdBothDirectoryInformation;

    protected String shareName;
    /**
     * String used to separate paths.
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * Shared connection to access the server.
     */
    protected final ShareClient mShareClient;

    /**
     * Path name of the abstract shared item.
     */
    private String pathName;

    private DiskShare diskShare;


    /**
     * Create a new abstract shared item based on the shared connection and the path name.
     *
     * @param shareClient Shared connection
     * @param pathName    Path name
     * @throws RuntimeException Exception in case of an invalid path name
     */
    public AbstractShareItem(ShareClient shareClient, DiskShare diskShare, String pathName) {
        this(shareClient, diskShare, pathName, null);
    }

    public AbstractShareItem(ShareClient shareClient, DiskShare diskShare, String pathName,
                             FileIdBothDirectoryInformation fileIdBothDirectoryInformation) {
        this.mShareClient = shareClient;
        this.diskShare = diskShare;
        this.fileIdBothDirectoryInformation = fileIdBothDirectoryInformation;
        if (ShareUtils.isValidSharedItemName(pathName)) {
            this.pathName = pathName;
        } else {
            throw new RuntimeException("The given path name is not a valid share path");
        }
    }

    public synchronized DiskShare getDiskShare() {
        if (diskShare == null && shareName != null) {
            diskShare = (DiskShare) mShareClient.getSession().connectShare(shareName);
        }
        return diskShare;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExisting() {
        return isDirectory() || isFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirectory() {
        if (fileIdBothDirectoryInformation != null) {
            return (fileIdBothDirectoryInformation.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_DIRECTORY
                    .getValue()) ==
                    FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue();
        }
        return getDiskShare().folderExists(pathName);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFile() {
        return !isDirectory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        if (!pathName.isEmpty()) {
            int lastIndex = pathName.lastIndexOf(PATH_SEPARATOR);
            return pathName.substring(lastIndex + 1, pathName.length());
        } else {
            return pathName;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerName() {
        return mShareClient.getServerName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShareName() {
        return getDiskShare().getSmbPath().getShareName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return pathName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSmbPath() {
        SmbPath smbPath = new SmbPath(getServerName(), getShareName(), pathName.replace(PATH_SEPARATOR, "\\"));
        return smbPath.toUncPath();
    }

    @Override
    public String getParentPath() {
        int lastIndex = pathName.lastIndexOf(PATH_SEPARATOR);
        if (lastIndex != -1) {
            return pathName.substring(0, lastIndex);
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCreationTime() {
        if (fileIdBothDirectoryInformation != null) {
            return fileIdBothDirectoryInformation.getCreationTime().toEpochMillis();
        }
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getCreationTime().toEpochMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastAccessTime() {
        if (fileIdBothDirectoryInformation != null) {
            return fileIdBothDirectoryInformation.getLastAccessTime().toEpochMillis();
        }
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getLastAccessTime().toEpochMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastWriteTime() {
        if (fileIdBothDirectoryInformation != null) {
            return fileIdBothDirectoryInformation.getLastWriteTime().toEpochMillis();
        }
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getLastWriteTime().toEpochMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getChangeTime() {
        if (fileIdBothDirectoryInformation != null) {
            return fileIdBothDirectoryInformation.getChangeTime().toEpochMillis();
        }
        FileBasicInformation fileBasicInformation = getDiskShare().getFileInformation(pathName).getBasicInformation();
        return fileBasicInformation.getChangeTime().toEpochMillis();
    }

    /**
     * Check if the current and the given objects are equals.
     *
     * @param object Given object to compare against
     * @return Status of the check
     */
    @Override
    public abstract boolean equals(Object object);

    @Override
    public List<ShareItem> getFileList() {
        List<ShareItem> fileList = new ArrayList<>();
        for (FileIdBothDirectoryInformation item : getDiskShare().list(getPath())) {
            // if (!ShareUtils.isValidSharedItemName(
            //         item.getFileName()) || (item.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_HIDDEN
            //         .getValue()) == FileAttributes.FILE_ATTRIBUTE_HIDDEN
            //         .getValue()) {
            //     continue;
            // }
            // String path = getPath() + (ShareUtils.isEmpty(getPath()) ? "" : PATH_SEPARATOR) + item.getFileName();
            // fileList.add(new ShareFile(getShareClient(), getDiskShare(), path, item));
        }
        return fileList;
    }

    /**
     * Get the shared connection.
     *
     * @return Shared connection
     */
    public ShareClient getShareClient() {
        return mShareClient;
    }

    @Override
    public boolean isRoot() {
        return false;
    }
}

