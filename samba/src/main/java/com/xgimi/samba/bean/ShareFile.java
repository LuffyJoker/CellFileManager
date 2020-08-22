package com.xgimi.samba.bean;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.xgimi.samba.ShareItem;
import com.xgimi.samba.core.ShareClient;
import com.xgimi.samba.streams.SharedInputStream;
import com.xgimi.samba.utils.ShareUtils;
import com.xgimi.samba.utils.SharedOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:21
 * desc   : samba 共享文件类
 */
public class ShareFile extends AbstractShareItem<ShareFile> {

    public ShareFile(ShareClient shareClient, DiskShare diskShare, String pathName) {
        super(shareClient, diskShare, pathName);
    }

    public ShareFile(ShareClient shareClient, DiskShare diskShare, String pathName,
                     FileIdBothDirectoryInformation fileIdBothDirectoryInformation) {
        super(shareClient, diskShare, pathName, fileIdBothDirectoryInformation);
    }

    public static ShareFile build(String url) {
        return build(url, new AuthenticationContext("", "".toCharArray(), ""));
    }

    public static ShareFile build(String url, AuthenticationContext authenticationContext) {
        SmbPath smbPath = SmbPath.parse(url);
        ShareClient shareClient = null;
        try {
            shareClient = new ShareClient(smbPath.getHostname(), authenticationContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ShareFile(shareClient,
                (DiskShare) shareClient.getSession().connectShare(smbPath.getShareName()), smbPath.getPath());
    }


    /**
     * Create a new file.
     */
    public void createFile() {
        File file = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_ALL),
                null, SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        file.close();
    }

    /**
     * Delete the current file.
     */
    public void deleteFile() {
        getDiskShare().rm(getPath());
    }

    /**
     * Copy the current file to another file on the same server share via server side copy. This does not work as soon
     * the files are on different shares (In that case copy the input/output streams). For more information check out
     * this Samba article: https://wiki.samba.org/index.php/Server-Side_Copy
     *
     * @param destinationSharedFile Other file on the same server share
     * @throws com.hierynomus.protocol.commons.buffer.Buffer.BufferException Buffer related exception
     * @throws TransportException     Transport related exception
     */
    public void copyFileViaServerSideCopy(ShareFile destinationSharedFile)
            throws Buffer.BufferException, TransportException {
        try (
                File sourceFile = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_READ), null,
                        SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
                File destinationFile = getDiskShare().openFile(destinationSharedFile.getPath(),
                        EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL,
                        SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
        ) {
            sourceFile.remoteCopyTo(destinationFile);
        }
    }

    public File openFile() {
        Set<SMB2ShareAccess> s = new HashSet<>();
        s.add(SMB2ShareAccess.ALL.iterator().next());
        return getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_READ), null, s,
                SMB2CreateDisposition.FILE_OPEN, null);
    }

    /**
     * Get the input stream of the file that can be used to download the file.
     *
     * @return Input stream of the shared file
     */
    public InputStream getInputStream() {
        return new SharedInputStream(openFile(), this);
    }

    /**
     * Get the output stream of the file that can be used to upload content to this file.
     *
     * @return Output stream of the shared file
     */

    public OutputStream getOutputStream() {
        return getOutputStream(false);
    }

    /**
     * Get the output stream of the file that can be used to upload and append content to this file.
     *
     * @param appendContent Append content or overwrite it
     * @return Output stream of the shared file
     */

    public OutputStream getOutputStream(boolean appendContent) {
        SMB2CreateDisposition mode = !appendContent
                ? SMB2CreateDisposition.FILE_OVERWRITE_IF
                : SMB2CreateDisposition.FILE_OPEN_IF;
        File file = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess
                .ALL, mode, null);
        return new SharedOutputStream(file, appendContent);
    }

    /**
     * Get the file size of the shared item.
     *
     * @return File size of the shared items in bytes
     */
    public long getFileSize() {
        if (fileIdBothDirectoryInformation != null) {
            return fileIdBothDirectoryInformation.getEndOfFile();
        }
        FileStandardInformation fileStandardInformation = getDiskShare().getFileInformation(getPath())
                                                                        .getStandardInformation();
        return fileStandardInformation.getEndOfFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShareFile renameTo(String newFileName, boolean replaceIfExist) {
        try (File file = getDiskShare().openFile(getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null,
                SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)) {
            String newFilePath = getParentPath() + PATH_SEPARATOR + newFileName;
            file.rename(newFilePath, replaceIfExist);
            return new ShareFile(getShareClient(), getDiskShare(), newFilePath);
        }
    }

    /**
     * Check if the current and the given objects are equals.
     *
     * @param object Given object to compare against
     * @return Status of the check
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof ShareFile) {
            ShareFile sharedFile = (ShareFile) object;
            return getSmbPath().equals(sharedFile.getSmbPath());
        } else {
            return false;
        }
    }

    @Override
    public ShareItem getParentFile() {
        if (ShareUtils.isEmpty(getPath())) {
            return null;
        }
        int lastIndex = getPath().lastIndexOf(PATH_SEPARATOR);
        if (lastIndex == -1) {
            return new ShareDisk(getShareClient(), getDiskShare());
        } else {
            return new ShareFile(getShareClient(), getDiskShare(), getPath().substring(0, lastIndex));
        }
    }
}

