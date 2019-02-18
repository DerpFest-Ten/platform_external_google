package com.google.android.systemui.elmyra.feedback;

import static android.provider.Settings.Secure.NAVIGATION_BAR_MODE;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.content.ContentResolver;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.navigation.Navigator;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;
import java.util.ArrayList;
import java.util.List;

public abstract class NavigationBarEffect implements FeedbackEffect {
    private final Context mContext;
    private final List<FeedbackEffect> mFeedbackEffects = new ArrayList();

    public NavigationBarEffect(Context context) {
        mContext = context;
    }

    private void refreshFeedbackEffects() {
        StatusBar statusBar = (StatusBar) SysUiServiceProvider.getComponent(mContext, StatusBar.class);
        Navigator navigationBarView;
        if (statusBar != null) {
            navigationBarView = statusBar.getNavigationBarView();
            if (navigationBarView == null || navigationBarView.isFullGestureMode() || !isUsingStockNav()) {
                mFeedbackEffects.clear();
                return;
            }
        } else {
            mFeedbackEffects.clear();
            return;
        }
        if (!validateFeedbackEffects(mFeedbackEffects)) {
            mFeedbackEffects.clear();
        }
        if (mFeedbackEffects.isEmpty()) {
            mFeedbackEffects.addAll(findFeedbackEffects(navigationBarView));
        }
    }

    protected abstract List<FeedbackEffect> findFeedbackEffects(Navigator navigationBarView);

    protected boolean isActiveFeedbackEffect(FeedbackEffect feedbackEffect) {
        return true;
    }

    @Override
	public void onProgress(float f, int i) {
        refreshFeedbackEffects();
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < mFeedbackEffects.size()) {
                FeedbackEffect feedbackEffect = mFeedbackEffects.get(i3);
                if (isActiveFeedbackEffect(feedbackEffect)) {
                    feedbackEffect.onProgress(f, i);
                }
                i2 = i3 + 1;
            } else {
                return;
            }
        }
    }

    @Override
	public void onRelease() {
        refreshFeedbackEffects();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < mFeedbackEffects.size()) {
                mFeedbackEffects.get(i2).onRelease();
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public void onResolve(DetectionProperties detectionProperties) {
        refreshFeedbackEffects();
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < mFeedbackEffects.size()) {
                mFeedbackEffects.get(i2).onResolve(detectionProperties);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    protected abstract boolean validateFeedbackEffects(List<FeedbackEffect> list);

    private boolean isUsingStockNav() {
        return Settings.Secure.getIntForUser(mContext.getContentResolver(),
               Settings.Secure.NAVIGATION_BAR_MODE, 0, UserHandle.USER_CURRENT) == 0;
    }
}
