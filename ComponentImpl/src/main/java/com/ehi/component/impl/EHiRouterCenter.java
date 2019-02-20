package com.ehi.component.impl;

import android.net.Uri;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ehi.component.ComponentUtil;
import com.ehi.component.error.TargetActivityNotFoundException;
import com.ehi.component.router.IComponentHostRouter;
import com.ehi.component.router.IComponentModuleRouter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@hide}
 *
 * @hide
 */
public class EHiRouterCenter implements IComponentModuleRouter {

    private static final String TAG = "EHiRouterCenter";

    private static volatile EHiRouterCenter instance;

    private static Map<String, IComponentHostRouter> routerMap = new HashMap<>();

    private EHiRouterCenter() {
    }

    public static EHiRouterCenter getInstance() {
        if (instance == null) {
            synchronized (EHiRouterCenter.class) {
                if (instance == null) {
                    instance = new EHiRouterCenter();
                }
            }
        }
        return instance;
    }

    @Override
    @MainThread
    public void openUri(@NonNull EHiRouterRequest routerRequest) throws Exception {
        // 理论上,这里可以通过 host 直接拿到对应的 IComponentHostRouter 对象,然后调用 openUri 完成功能
        // 但是为了兼容渐进式组件化,没法一步就把某一个模块的代码都抽离出来,所以存在某一个 IComponentHostRouter
        // 中存在多个 host 的路由数据
        for (Map.Entry<String, IComponentHostRouter> entry : routerMap.entrySet()) {
            if (entry.getValue().isMatchUri(routerRequest.uri)) {
                entry.getValue().openUri(routerRequest);
                return;
            }
        }
        throw new TargetActivityNotFoundException(routerRequest.uri == null ? "" : routerRequest.uri.toString());
    }

    @Override
    public synchronized boolean isMatchUri(@NonNull Uri uri) {
        // 循环的理由同 openUri 方法
        for (Map.Entry<String, IComponentHostRouter> entry : routerMap.entrySet()) {
            final IComponentHostRouter router = entry.getValue();
            if (router != null && router.isMatchUri(uri)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public synchronized List<EHiRouterInterceptor> interceptors(@NonNull Uri uri) {
        for (Map.Entry<String, IComponentHostRouter> entry : routerMap.entrySet()){
            // 每一个子路由
            final IComponentHostRouter router = entry.getValue();
            if (router != null && router.isMatchUri(uri)){
                return router.interceptors(uri);
            }
        }
        return null;
    }

    @Override
    public void register(@NonNull IComponentHostRouter router) {
        if (router == null) {
            return;
        }
        routerMap.put(router.getHost(), router);
    }

    @Override
    public void register(@NonNull String host) {
        IComponentHostRouter router = findUiRouter(host);
        register(router);
    }

    @Override
    public void unregister(IComponentHostRouter router) {
        routerMap.remove(router.getHost());
    }

    @Override
    public void unregister(@NonNull String host) {
        routerMap.remove(host);
    }

    /**
     * {@hide}
     *
     * @param host
     * @return
     * @hide
     */
    @Nullable
    public IComponentHostRouter findUiRouter(String host) {
        final String className = ComponentUtil.genHostRouterClassName(host);
        try {
            Class<?> clazz = Class.forName(className);
            return (IComponentHostRouter) clazz.newInstance();
        } catch (ClassNotFoundException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }
        return null;
    }

}