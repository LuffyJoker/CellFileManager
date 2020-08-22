package com.xgimi.samba;

import java.util.List;

/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/30 16:17
 * desc   :
 */
public interface ShareItem {

    /**
     * Check if the shared item does exist.
     *
     * @return Status of the check
     */
    boolean isExisting();

    /**
     * Check if the shared item is a directory.
     *
     * @return Status of the check
     */
    boolean isDirectory();

    /**
     * Check if the shared item is a file.
     *
     * @return Status of the check
     */
    boolean isFile();

    /**
     * Get the server name of the connected server.
     *
     * @return Name of the server
     */
    String getServerName();

    /**
     * Get the share name of the connected server.
     *
     * @return Name of the share
     */
    String getShareName();

    /**
     * Get the name of the shared item. If the shared item is a root item, an string name will be returned.
     *
     * @return Real name or empty string
     */
    String getName();

    /**
     * Get the path of the shared item. If the shared item is a root item, an empty string will be returned.
     *
     * @return Real path or empty string
     */
    String getPath();

    /**
     * Get the full SMB path including the server, share and path, separated by backslashes.
     *
     * @return Full SMB path
     */
    String getSmbPath();

    /**
     * Rename the current item and return it as newly renamed item.
     *
     * @param newFileName    New file name
     * @param replaceIfExist Flag to replace an existing path of the same type (File/Directory)
     * @return Newly renamed shared item with the new path
     */
    ShareItem renameTo(String newFileName, boolean replaceIfExist);

    /**
     * Get the creation time of the shared item.
     *
     * @return Creation time of the shared item
     */
    long getCreationTime();

    /**
     * Get the last access time of the shared item.
     *
     * @return Last access time of the shared item
     */

    long getLastAccessTime();

    /**
     * Get the last write time of the shared item.
     *
     * @return Last write time of the shared item
     */

    long getLastWriteTime();

    /**
     * Get the change time of the shared item.
     *
     * @return Change time of the shared item
     */

    long getChangeTime();

    /**
     * Get child file list
     *
     * @return Child file list
     */
    List<ShareItem> getFileList();

    /**
     * Get parent file
     *
     * @return parent file
     */
    ShareItem getParentFile();

    /**
     * Get parent file path
     * @return parent file path
     */
    String getParentPath();

    boolean isRoot();
}

