package com.xgimi.filemanager.popwin;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.PopupWindow;

import com.blankj.utilcode.util.ScreenUtils;
import com.xgimi.filemanager.R;
import com.xgimi.filemanager.listerners.OnMenuClickListener;
import com.xgimi.filemanager.listerners.XgimiMenuListener;
import com.xgimi.gimiskin.sdk.SkinEngine;


/**
 * author : joker.peng
 * e-mail : joker.peng@xgimi.com
 * date   : 2020/7/31 10:56
 * desc   : 菜单弹窗
 */
public class XgimiMenuPopWindow extends PopupWindow {

    private View mAnchorView;
    private Menu mCurrentMenu;
    private boolean isShowing = false;
    private OnMenuClickListener mMenuClickListener;
    private Context mContext;
    private Bitmap shotBitmap;
    private int wh[];
    private Handler mHandler = new Handler();
    private View mMenuLayout;
    private boolean forceDismiss = false;


    public XgimiMenuPopWindow(Context context) {
        super(context);
    }

    public XgimiMenuPopWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XgimiMenuPopWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        mAnchorView = getWrapperActivity(mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        mMenuLayout = LayoutInflater.from(mContext).inflate(R.layout.menu_layout, null, false);
        mMenuLayout.setBackgroundResource(SkinEngine.INSTANCE.getColor(R.color.color_bg_pure_1));

    }

    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        mMenuClickListener = onMenuClickListener;
    }


    public boolean isMenuShowing() {
        return isShowing;
    }

    public void show() {
        if (isShowing) {
            return;
        }
        updateBlurBack();
        showAsDropDown(mAnchorView, 0, -ScreenUtils.getScreenHeight());
        isShowing = true;
        if (mXgimiMenuListener != null) {
            mXgimiMenuListener.onMenuStartShowing();
        }
    }

    public void hide() {
        forceDismiss = true;
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        dismiss();
    }

    public void destroy() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        if (shotBitmap != null) {
            shotBitmap.recycle();
        }
    }

    private Thread mThread;

    private void updateBlurBack() {
//        wh = new int[2];
//        BlurBgView.getScreenWidthAndHeight(wh);
//        mThread = new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                shotBitmap = BlurBgView.getScreenshot(wh[0], wh[1]);
//                final Bitmap blurB = BlurBgView.doBlur(mContext,
//                        Bitmap.createBitmap(shotBitmap, 0, 0, getWidth(), wh[1]), 22, 0.2f);
//                if (mHandler != null) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mMenuLayout.setBackground(ConvertUtils.bitmap2Drawable(blurB));
//                        }
//                    });
//                }
//            }
//        };
//        mThread.start();
    }

    private XgimiMenuListener mXgimiMenuListener;

    public void setXgimiMenuListener(XgimiMenuListener listener) {
        mXgimiMenuListener = listener;
    }

    @Override
    public void dismiss() {
//        if (!forceDismiss && mCurrentMenu != null && mCurrentMenu.parentMenu != null && mCurrentMenu.subMenus != null) {
//            setMenus(mCurrentMenu.parentMenu, 1);
//            return;
//        }
//        forceDismiss = false;
//        isShowing = false;
//        mRecyclerView.removeAllViews();
//        mCurrentMenu.clear();
//        if (mXgimiMenuListener != null) {
//            mXgimiMenuListener.onMenuStartHiding();
//        }
//        super.dismiss();
    }


//    @Override
//    public void onMenuClick(View view, Menu menu) {
//        if (menu.subMenus != null && !menu.subMenus.isEmpty()) {
//            menu.parentMenu = mCurrentMenu;
//            menu.selectPosition = 0;
//            setMenus(menu, 0);
//        } else if (mMenuClickListener != null) {
//            mMenuClickListener.onMenuClick(view, menu);
//        }
//    }
//
//    @Override
//    public void onMenuFocusChange(View view, Menu menu, int position, boolean hasFocus) {
//        if (hasFocus && mCurrentMenu != null) {
//            mCurrentMenu.selectPosition = position;
//        }
//    }

//    @Override
//    public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
        // int selectPosition = mCurrentMenu != null ? mCurrentMenu.selectPosition : 0;
        // if (viewHolder.getAdapterPosition() == selectPosition) {
        //     if (viewHolder.itemView.getVisibility() != View.VISIBLE) {
        //         mCurrentMenu.selectPosition += 1;
        //     } else {
        //         viewHolder.itemView.requestFocus();
        //         ((MenuItemView) viewHolder.itemView).setStatus(true);
        //     }
        // }
//    }

    /**
     * 获取当前 context 对象的 WrapActivity
     *
     * @param context
     * @return
     */
    private Activity getWrapperActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getWrapperActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }
}
