package com.xgimi.filemanager.helper

import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.CatalogInfo
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.utils.FileCategoryUtil
import com.xgimi.filemanager.utils.FileUtil
import java.text.Collator
import java.util.*

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 15:33
 *    desc   :
 */
object Comparators {
    enum class SortMode {
        NAME, LAST_MODIFIED
    }

    /**
     * 获取文件排序方式
     *
     * @param comparator
     * @return
     */
    fun getForFile(comparator: Int): Comparator<Any>? {
        return if (comparator == SortMode.NAME.ordinal) {
            NameComparator(false)
        } else {
            LastModifiedComparator(true)
        }
    }

    /**
     * 获取 Samba 文件排序方式
     *
     * @param comparator
     * @return
     */
    fun getForSamba(comparator: Int): Comparator<*>? {
        return if (comparator == SortMode.NAME.ordinal) {
            NameComparator(false)
        } else {
            ChangeTimeComparatorForSamba(true)
        }
    }

    fun getForDoc(comparator: Int): Comparator<Any>? {
        return if (comparator == SortMode.NAME.ordinal) {
            NameComparator(true)
        } else {
            LastModifiedComparator(false)
        }
    }

    fun getLastModifiedComparator(): Comparator<*>? {
        return LastModifiedComparator(true)
    }

    /**
     * 获取设备排序方式
     *
     * @return
     */
    fun getForDevice(): Comparator<*>? {
        return DeviceNameComparator()
    }

    /**
     * 时间排序
     */
    internal class LastModifiedComparator(private val isNormal: Boolean) : Comparator<Any> {
        override fun compare(lhs: Any?, rhs: Any?): Int {
            if (lhs == null && rhs == null) {
                return 0
            }
            if (lhs == null) {
                return 1
            }
            if (rhs == null) {
                return -1
            }
            if (lhs is BaseData && rhs is BaseData) {
                val lhsBaseData = lhs
                val rhsBaseData = rhs
                val lType = lhsBaseData.category
                val rType = rhsBaseData.category
                return if (lType == 0 && rType != 0 && !isNormal) {
                    -1
                } else if (lType != 0 && rType == 0 && !isNormal) {
                    1
                } else {
                    var lhsLastModified = lhsBaseData.lastModified
                    var rhsLastModified = rhsBaseData.lastModified
                    if (lhsLastModified == 0L) {
                        lhsLastModified = FileUtil.getModifyTimeLong(lhsBaseData.path)
                    }
                    if (rhsLastModified == 0L) {
                        rhsLastModified = FileUtil.getModifyTimeLong(rhsBaseData.path)
                    }
                    rhsLastModified.compareTo(lhsLastModified)
                }
            } else if (lhs is BaseData && rhs is CatalogInfo) {
                return 1
            } else if (lhs is CatalogInfo && rhs is BaseData) {
                return -1
            } else if (lhs is CatalogInfo && rhs is CatalogInfo) {
                return NameComparator(false).compare(lhs, rhs)
            }
            return 0
        }

    }

    /**
     * Samba 文件排序
     */
    internal class ChangeTimeComparatorForSamba(private val isNormal: Boolean) :
        Comparator<Any?> {
        override fun compare(lhs: Any?, rhs: Any?): Int {
            if (lhs == null && rhs == null) {
                return 0
            }
            if (lhs == null) {
                return 1
            }
            if (rhs == null) {
                return -1
            }
            if (lhs is BaseData && rhs is BaseData) {
                val lhsBaseData = lhs
                val rhsBaseData = rhs
                val lType = lhsBaseData.category
                val rType = rhsBaseData.category
                return if (lType == 0 && rType != 0 && !isNormal) {
                    -1
                } else if (lType != 0 && rType == 0 && !isNormal) {
                    1
                } else {
                    var lhsLastModified = lhsBaseData.modifyTime
                    var rhsLastModified = rhsBaseData.modifyTime
                    if (lhsLastModified == 0L) {
                        lhsLastModified = FileUtil.getModifyTimeLong(lhsBaseData.path)
                    }
                    if (rhsLastModified == 0L) {
                        rhsLastModified = FileUtil.getModifyTimeLong(rhsBaseData.path)
                    }
                    rhsLastModified.compareTo(lhsLastModified)
                }
            } else if (lhs is BaseData && rhs is CatalogInfo) {
                return 1
            } else if (lhs is CatalogInfo && rhs is BaseData) {
                return -1
            } else if (lhs is CatalogInfo && rhs is CatalogInfo) {
                return NameComparator(false).compare(lhs, rhs)
            }
            return 0
        }

    }

    /**
     * 设备名称排序
     */
    internal class DeviceNameComparator : Comparator<DeviceInfo?> {
        override fun compare(lData: DeviceInfo?, rData: DeviceInfo?): Int {
            if (lData == null && rData == null) {
                return 0
            }
            if (lData == null) {
                return 1
            }
            if (rData == null) {
                return -1
            }
            val lType = lData.deviceType
            val rType = rData.deviceType
            if (rType == DeviceInfo.DeviceCategory.LOCALUSB.ordinal) {
                return 1
            }
            if (lType == DeviceInfo.DeviceCategory.LOCALUSB.ordinal) {
                return -1
            }
            return if (lType < rType) {
                -1
            } else if (lType > rType) {
                1
            } else {
                val lName = lData.deviceName
                val rName = rData.deviceName
                val collator =
                    Collator.getInstance(Locale.CHINA)
                collator?.compare(lName.toLowerCase(), rName.toLowerCase()) ?: 0
            }
        }
    }

    /**
     * 带数字的名称排序
     */
    internal class NameComparator(isDoc: Boolean) : Comparator<Any> {
        var isDoc = false
        var collator = Collator.getInstance()

        fun findDigitEnd(arrChar: CharArray, at: Int): Int {
            var k = at
            var c = arrChar[k]
            var bFirstZero = c == '0'
            while (k < arrChar.size) {
                c = arrChar[k]
                //first non-digit which is a high chance.
                if (c > '9' || c < '0') {
                    break
                } else if (bFirstZero && c == '0') {
                    k++
                } else {
                    bFirstZero = false
                }
                k++
            }
            return k
        }

        override fun compare(lData: Any?, rData: Any?): kotlin.Int {
            if (lData == null && rData == null) {
                return 0
            }
            if (lData == null) {
                return 1
            }
            if (rData == null) {
                return -1
            }
            var lDataName: String? = null
            var rDataName: String? = null
            if (lData is BaseData && rData is BaseData) {
                val lBaseData = lData
                val rBaseData = rData
                val lType: Int = lBaseData.category
                val rType: Int = rBaseData.category
                if (lType == FileCategory.Folder.ordinal && rType != FileCategory.Folder.ordinal) {
                    return -1
                } else if (lType != FileCategory.Folder.ordinal && rType == FileCategory.Folder.ordinal) {
                    return 1
                } else {
                    if (isDoc && lBaseData.category == FileCategory.Document.ordinal && lBaseData.category == FileCategory.Document.ordinal) {
                        return compareDoc(lBaseData, rBaseData)
                    } else {
                        lDataName = lData.name
                        rDataName = rData.name
                    }
                }
            } else if (lData is BaseData && rData is CatalogInfo) {
                return 1
            } else if (lData is CatalogInfo && rData is BaseData) {
                return -1
            } else if (lData is CatalogInfo && rData is CatalogInfo) {
                lDataName = lData.name
                rDataName = rData.name
            }
            return compareName(lDataName, rDataName)
        }

        private fun compareDoc(lData: BaseData, rData: BaseData): Int {
            val lType: Int = FileCategoryUtil.getDocType(lData.path!!)
            val rType: Int = FileCategoryUtil.getDocType(rData.path!!)
            return if (lType > rType) {
                -1
            } else if (lType < rType) {
                1
            } else {
                compareName(lData.name, rData.name)
            }
            //                return 0;
        }

        private fun compareName(lDataName: String?, rDataName: String?): Int {
            val str =
                arrayOf(lDataName!!.substring(0, 1), rDataName!!.substring(0, 1))
            val type = intArrayOf(1, 1)
            for (i in 0..1) {
                if (str[i].matches(Regex("[\\u4E00-\\u9FA5]+"))) {//中文字符
                    type[i] = 1
                } else {
                    type[i] = 2
                }
            }
            return if (type[0] == type[1]) {
                collator.compare(lDataName, rDataName)
            } else type[1] - type[0]
            /*if (StringUtils.isEmpty(lDataName) && StringUtils.isEmpty(rDataName)) {
                return 0;
            }
            if (StringUtils.isEmpty(lDataName)) {
                return 1;
            }
            if (StringUtils.isEmpty(rDataName)) {
                return -1;
            }
            char[] a = lDataName.toLowerCase().toCharArray();
            char[] b = rDataName.toLowerCase().toCharArray();
            if (a != null && b != null) {
                Int aNonzeroIndex = new Int();
                Int bNonzeroIndex = new Int();
                int aIndex = 0, bIndex = 0,
                        aComparedUnitTailIndex, bComparedUnitTailIndex;

                while (aIndex < a.length && bIndex < b.length) {
                    //aIndex <
                    aNonzeroIndex.i = aIndex;
                    bNonzeroIndex.i = bIndex;
                    aComparedUnitTailIndex = findDigitEnd(a, aNonzeroIndex);
                    bComparedUnitTailIndex = findDigitEnd(b, bNonzeroIndex);
                    //compare by number
                    if (aComparedUnitTailIndex > aIndex && bComparedUnitTailIndex > bIndex) {
                        int aDigitIndex = aNonzeroIndex.i;
                        int bDigitIndex = bNonzeroIndex.i;
                        int aDigit = aComparedUnitTailIndex - aDigitIndex;
                        int bDigit = bComparedUnitTailIndex - bDigitIndex;
                        //compare by digit
                        if (aDigit != bDigit)
                            return aDigit - bDigit;
                        //the number of their digit is same.
                        while (aDigitIndex < aComparedUnitTailIndex) {
                            if (a[aDigitIndex] != b[bDigitIndex])
                                return a[aDigitIndex] - b[bDigitIndex];
                            aDigitIndex++;
                            bDigitIndex++;
                        }
                        //if they are equal compared by number, compare the number of '0' when start with "0"
                        //ps note: paNonZero and pbNonZero can be added the above loop "while", but it is changed
                        meanwhile.
                        //so, the following comparsion is ok.
                        aDigit = aNonzeroIndex.i - aIndex;
                        bDigit = bNonzeroIndex.i - bIndex;
                        if (aDigit != bDigit)
                            return aDigit - bDigit;
                        aIndex = aComparedUnitTailIndex;
                        bIndex = bComparedUnitTailIndex;
                    } else {
                        if (a[aIndex] != b[bIndex]) {
                            return a[aIndex] - b[bIndex];
                        }
                        aIndex++;
                        bIndex++;
                    }

                }

            }
            return a.length - b.length;*/
        }

        init {
            this.isDoc = isDoc
        }
    }
}