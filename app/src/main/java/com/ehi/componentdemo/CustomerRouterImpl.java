package com.ehi.componentdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ehi.base.InterceptorConfig;
import com.ehi.base.ModuleConfig;
import com.ehi.component.anno.EHiRouterAnno;
import com.ehi.component.impl.EHiRouterRequest;

/**
 * 自定义路由实现的范例
 */
public class CustomerRouterImpl {

    /**
     * 自定义实现跳转到打电话的界面,并且自动完成打电话权限的申请
     *
     * @param request
     * @return
     */
    @Nullable
    @EHiRouterAnno(
            host = ModuleConfig.System.NAME,
            value = ModuleConfig.System.CALL_PHONE,
            interceptorNames = InterceptorConfig.HELP_CALLPHOEPERMISION
    )
    public static Intent callPhoneIntent(@NonNull EHiRouterRequest request) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "15857913627"));
        return intent;
    }

    /**
     * 系统 App 详情
     *
     * @param request
     * @return
     */
    @EHiRouterAnno(
            host = ModuleConfig.System.NAME,
            value = ModuleConfig.System.SYSTEM_APP_DETAIL
    )
    public static void appDetail(@NonNull EHiRouterRequest request) {
        Activity act = request.getActivity();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + request.getRawContext().getPackageName()));
        if (request.requestCode == null) {
            if (act == null) {
                request.fragment.startActivity(intent);
            } else {
                act.startActivity(intent);
            }
        } else {
            if (act == null) {
                request.fragment.startActivityForResult(intent, request.requestCode);
            } else {
                act.startActivityForResult(intent, request.requestCode);
            }
        }
    }

}
