package com.xgimi.filemanager.enums

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/6 17:29
 *    desc   :
 */
enum class ThemeStyleTypeEnum(typeId: Int, typeName: String) {
    /**
     * 自适应
     */
    AUTO_ADAPT(0, "自适应"),
    /**
     * 仅导航栏强制暗色模式（适配仅有大海报的页面）
     */
    FORCE_DARK_MODE(1, "仅导航栏强制暗色模式"),
    /**
     * 整个页面强制暗色模式（适配具有全局背景的页面）
     */
    FORCE_DARK_MODE_ALL(2, "整个页面强制暗色模式");

    private var typeId: Int
    private var typeName: String
    fun getTypeId(): Int {
        return typeId
    }

    fun setTypeId(typeId: Int) {
        this.typeId = typeId
    }

    fun getTypeName(): String {
        return typeName
    }

    fun setTypeName(typeName: String) {
        this.typeName = typeName
    }

    companion object {
        fun isDark(type: Int): Boolean {
            return FORCE_DARK_MODE_ALL.typeId == type || FORCE_DARK_MODE.typeId == type
        }
    }

    init {
        this.typeId = typeId
        this.typeName = typeName
    }
}
