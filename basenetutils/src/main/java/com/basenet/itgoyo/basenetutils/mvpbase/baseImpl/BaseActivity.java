package com.basenet.itgoyo.basenetutils.mvpbase.baseImpl;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.basenet.itgoyo.basenetutils.R;
import com.basenet.itgoyo.basenetutils.mvpbase.BasePresenter;
import com.basenet.itgoyo.basenetutils.mvpbase.BaseView;
import com.basenet.itgoyo.basenetutils.utils.ActivityManager;
import com.gyf.barlibrary.ImmersionBar;

/*
 * 项目名:    BaseLib
 * 包名       com.zhon.baselib.mvpbase.baseImpl
 * 文件名:    BaseActivity
 * 创建者:    ZJB
 * 创建时间:  2017/9/7 on 14:17
 * 描述:     TODO 基类Activity
 */
public abstract class BaseActivity<P extends BasePresenter> extends AppCompatActivity
        implements BaseView {

    protected P presenter;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
//        StatusBarUtil.setStatusBar(this);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setTranslucentStatus(true);
//        }
//
//        SystemBarTintManager tintManager = new SystemBarTintManager(this);
//        tintManager.setStatusBarTintEnabled(true);
//        //设置状态栏颜色

//        tintManager.setStatusBarTintResource(R.color.statusbar_bg);

        ImmersionBar.with(this).statusBarColor(R.color.write_bg).statusBarDarkFont(true,0.2f)
                .fitsSystemWindows(true)
                .init();

        ActivityManager.getAppInstance().addActivity(this);//将当前activity添加进入管理栈
        presenter = initPresenter();
    }


    @Override
    protected void onDestroy() {
        ActivityManager.getAppInstance().removeActivity(this);//将当前activity移除管理栈
        if (presenter != null) {
            presenter.detach();//在presenter中解绑释放view
            presenter = null;
        }
        super.onDestroy();
    }



    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 在子类中初始化对应的presenter
     *
     * @return 相应的presenter
     */
    public abstract P initPresenter();


    @Override
    public void dismissLoadingDialog() {

    }

    @Override
    public void showLoadingDialog(String msg) {

    }


    protected void exitApp() {
        ActivityManager.getAppInstance().removeActivity(this);//将当前activity移除管理栈
        if (presenter != null) {
            presenter.detach();//在presenter中解绑释放view
            presenter = null;
        }
    }


}
