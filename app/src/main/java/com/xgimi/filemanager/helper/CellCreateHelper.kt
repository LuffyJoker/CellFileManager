package com.xgimi.filemanager.helper

import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.xgimi.filemanager.R
import com.xgimi.filemanager.bean.BaseData
import com.xgimi.filemanager.bean.CatalogInfo
import com.xgimi.filemanager.bean.DeviceInfo
import com.xgimi.filemanager.enums.FileCategory
import com.xgimi.filemanager.menus.XgimiMenuItem
import com.xgimi.gimiskin.cell.setStyle
import com.xgimi.gimiskin.sdk.SkinEngine
import com.xgimi.userbehavior.entity.setting.Focus
import com.xgimi.view.cell.Cell
import com.xgimi.view.cell.CellEvent
import com.xgimi.view.cell.Layout
import com.xgimi.view.cell.component.*
import com.xgimi.view.cell.layout.AbsoluteLayout
import com.xgimi.view.cell.layout.FrameLayout
import com.xgimi.view.cell.layout.Gravity
import com.xgimi.view.cell.layout.LinearLayout

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/10 9:56
 *    desc   :
 */
object CellCreateHelper {

    const val TAG_TITLE = "TAG_TITLE"
    const val TAG_PATH = "TAG_PATH"
    const val TAG_RIGHT_ARR = "TAG_RIGHT_ARR"

    /**
     * 图标模式显示文件，cell创建
     */
    private fun fileIconModeImageCell(any: Any, parent: Cell): Cell {
        var iconRes: Any? = null
        if (any is DeviceInfo) {
            iconRes = IconHelper.getDeviceIcon(any.deviceType)
        } else if (any is BaseData) {

            if (any.category != FileCategory.Picture.ordinal && any.category != FileCategory.Video.ordinal) { // 显示通用信息
                iconRes = IconHelper.getCategoryIcon(any.category, any.name)
            } else if (any.category == FileCategory.Video.ordinal) {
                iconRes = R.drawable.ic_icon_explorer_movie
            }

        } else if (any is CatalogInfo) {
            if (any.category != FileCategory.Picture.ordinal) { // 显示通用信息
                iconRes = IconHelper.getCategoryIcon(any.category)
            } else if (any.category == FileCategory.Music.ordinal) {
                iconRes = R.drawable.ic_icon_explorer_music
            }
        }

        return Cell().apply {
            addComponent(
                FocusComponent()
                    .watch(parent)
                    .setCorner(24f)
            )
            addCell(
                Cell(
                    ColorComponent()
                        .setColor(SkinEngine.getColor(R.color.color_bg_alpha_3))
                        .setCorner(24F)
                ),
                Layout.Params(248, 248)
            )
            addCell(Cell(ImageComponent().apply {
                res = iconRes
                setShowWithAlpha(true)
                setCorner(40f)
                setAntiAlias(true)
            }), FrameLayout.Params(144, 144, Gravity.CENTER))
        }
    }

    /**
     * 文本 Cell
     */
    fun textCell(any: Any, style: Int): Cell {

        var text: String? = null

        when (any) {
            is DeviceInfo -> {
                var deviceName = ""
                val deviceType: Int = any.deviceType
                if (deviceType == DeviceInfo.DeviceCategory.Samba.ordinal) {
                    val hostname: String = any.hostName
                    deviceName = if (StringUtils.isEmpty(hostname)) {
                        any.ip
                    } else {
                        hostname
                    }
                } else {
                    deviceName = any.deviceName
                }
                text = deviceName
            }
            is XgimiMenuItem -> {
                text = any.name
            }
            is BaseData -> {
                text = any.name
            }
            is CatalogInfo -> {
                when (any.category) {
                    FileCategory.Picture.ordinal -> {
                        text = StringUtils.getString(
                            R.string.info_file_sum_picture,
                            any.name,
                            any.datas.size
                        )
                    }
                    FileCategory.Music.ordinal -> {
                        text = StringUtils.getString(
                            R.string.info_file_sum_music,
                            any.name,
                            any.datas.size
                        )
                    }
                    FileCategory.Video.ordinal -> {
                        text = StringUtils.getString(
                            R.string.info_file_sum_movie,
                            any.name,
                            any.datas.size
                        )
                    }
                    else -> {
                        text = any.name + "(" + any.datas.size + ")"
                    }
                }
            }
            is String -> {
                text = any
            }
            else -> {
                return Cell().addComponent(
                    TextComponent()
                        .setStyle(style)
                        .setEllipsis(true)
                ).setMask(true)
            }
        }

        return Cell(TAG_TITLE).addComponent(
            TextComponent()
                .setText(text)
                .setStyle(style)
                .setEllipsis(true)
        ).setMask(true)
    }

    fun getItemCell(
        any: Any,
        onClickListener: CellEvent.OnClickListener,
        longPressListener: CellEvent.OnLongPressListener,
        isContainDes: Boolean = false
    ): Cell {
        return Cell(LinearLayout(LinearLayout.VERTICAL)).apply {
            // 添加图标
            addCell(
                fileIconModeImageCell(any, this),
                LinearLayout.Params(Layout.Params.WRAP, Layout.Params.WRAP, Gravity.CENTER)
            )
            // 文本
            addCell(
                textCell(any, R.style.font_body_medium_4),
                LinearLayout.Params(
                    248,
                    Layout.Params.WRAP,
                    Gravity.CENTER
                ).setMargin(0, 24, 0, 0)
            )
            // 文本下方的描述
            if (isContainDes) {
                addCell(
                    textCell(any, R.style.font_caption_regular_2),
                    LinearLayout.Params(
                        248,
                        Layout.Params.WRAP,
                        Gravity.CENTER
                    ).setMargin(0, 16, 0, 0)
                )
            }
        }.setOnFocusChangeListener { cell, _ ->
            cell.findCellByTag(TAG_TITLE)
                .findComponent(TextComponent::class.java)
                .setMarquee(cell.isFocused)
        }.setOnLongPressListener(longPressListener) // 长按事件
            .setOnClickListener(onClickListener)
            .setFocusable(true)
            // 放大
            .setEffects(true)
            .setLayoutParams(Layout.Params(Layout.Params.WRAP, Layout.Params.WRAP))
            .setHolder(any)
    }

    fun getPathCell(name: String, path: String, style: Int): Cell {
        return Cell(LinearLayout(LinearLayout.HORIZONTAL)).apply {
            addCell(Cell(
                ImageComponent().apply {
                    res = R.drawable.ic_icon_arrowleft
                    setShowWithAlpha(true)
                    setCorner(40f)
                    setAntiAlias(true)
                }
            ), LinearLayout.Params(48, 48))
            addCell(
                Cell().apply {
                    addComponent(
                        TextComponent()
                            .setText(name)
                            .setStyle(style)
                    )
                },
                LinearLayout
                    .Params(LinearLayout.Params.WRAP, LinearLayout.Params.WRAP)
                    .setMarginLeft(16)
            )
            addCell(Cell(TAG_RIGHT_ARR).apply {
                ImageComponent().apply {
                    res = R.drawable.ic_icon_arrowright
                    setShowWithAlpha(true)
                    setCorner(40f)
                    setAntiAlias(true)
                }
            }, LinearLayout.Params(48, 48).setMarginLeft(16))

            addCell(
                Cell(TAG_PATH).apply {
                    addComponent(
                        TextComponent()
                            .setText(path)
                            .setStyle(style)
                    )
                },
                LinearLayout
                    .Params(LinearLayout.Params.WRAP, LinearLayout.Params.WRAP)
                    .setMarginLeft(16)
            )
        }
    }

    private fun getButtonCell(text: String, onClickListener: CellEvent.OnClickListener): Cell {
        return Cell()
            .addComponent(
                ColorComponent().setColorRes(R.color.color_bg_pure_1).setCorner(8f).setWithPadding(
                    false
                )
            ).setHolder(text)
            .addComponent(FocusComponent().setColorRes(R.color.color_brand_1))
            .addComponent(TextComponent().setTextSize(28f).setText(text).setStyle(R.style.btn_commonly_medium_default))
            .setFocusable(true)
            .setPadding(32, 18, 32, 18)
            .setOnClickListener(onClickListener)
    }

    fun getTitleCell(name: String, onClickListener: CellEvent.OnClickListener): Cell {
        return Cell(LinearLayout(LinearLayout.HORIZONTAL)).apply {
            addCell(Cell(
                ImageComponent().apply {
                    res = R.drawable.ic_icon_arrowleft
                    setShowWithAlpha(true)
                    setCorner(40f)
                    setAntiAlias(true)
                }
            ), LinearLayout.Params(48, 48, Gravity.CENTER))

            addCell(
                Cell(
                    TextComponent()
                        .setText(name)
                        .setStyle(R.style.font_crosshead_bold_3)
                        .setGravity(Gravity.LEFT_CENTER)
                ),
                LinearLayout
                    .Params(LinearLayout.Params.WRAP, LinearLayout.Params.WRAP, Gravity.CENTER)
                    .setMarginLeft(16)
            )

            addCell(
                Cell(LinearLayout(LinearLayout.HORIZONTAL))
                    .addCell(
                        getButtonCell(
                            Utils.getApp().resources.getString(R.string.refresh),
                            onClickListener
                        ),
                        LinearLayout.Params(
                            LinearLayout.Params.WRAP,
                            LinearLayout.Params.WRAP,
                            Gravity.RIGHT_CENTER
                        )
                    ).requestFocus()
                    .addCell(
                        getButtonCell(
                            Utils.getApp().resources.getString(R.string.manual_connect),
                            onClickListener
                        ),
                        LinearLayout
                            .Params(
                                LinearLayout.Params.WRAP,
                                LinearLayout.Params.WRAP,
                                Gravity.RIGHT_CENTER
                            )
                            .setMargin(65, 0, 0, 0)
                    ),
                FrameLayout
                    .Params(
                        LinearLayout.Params.WRAP,
                        LinearLayout.Params.WRAP,
                        Gravity.RIGHT_CENTER
                    )
            )
        }
    }

    fun getProgressCell(): Cell {
        return Cell(ProgressComponent2().setSize(ProgressComponent.SIZE_L))
    }
}