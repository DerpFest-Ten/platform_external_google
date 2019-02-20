package com.google.android.systemui.elmyra.feedback;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import com.android.internal.utils.ActionHandler;
import com.android.internal.utils.Config.ActionConfig;
import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class HapticClick implements FeedbackEffect {
    private static final AudioAttributes SONIFICATION_AUDIO_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private int mLastGestureStage;
    private final VibrationEffect mProgressVibrationEffect = VibrationEffect.get(5);
    private final VibrationEffect mResolveVibrationEffect = VibrationEffect.get(0);
    private final Vibrator mVibrator;
    private ContentResolver resolver;
    private Context mContext;

    public HapticClick(Context context) {
        mContext = context;
        resolver = context.getContentResolver();
        mVibrator = (Vibrator) context.getSystemService("vibrator");
    }

    private boolean isSqueezeTurnedOff() {
        String actionConfig = Settings.Secure.getStringForUser(resolver,
                Settings.Secure.SQUEEZE_SELECTION_SMART_ACTIONS, UserHandle.USER_CURRENT);
        String action = ActionConfig.getActionFromDelimitedString(mContext, actionConfig,
                ActionHandler.SYSTEMUI_TASK_NO_ACTION);
        return action.equals(ActionHandler.SYSTEMUI_TASK_NO_ACTION);
    }

    @Override
    public void onProgress(float f, int i) {
        if (isSqueezeTurnedOff()) {
            return;
        }
        if (!(mLastGestureStage == 2 || i != 2 || mVibrator == null)) {
            mVibrator.vibrate(mProgressVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES);
        }
        mLastGestureStage = i;
    }

    @Override
	public void onRelease() {
    }

    public void onResolve(DetectionProperties detectionProperties) {
        if (isSqueezeTurnedOff()) {
            return;
        }
        if ((detectionProperties == null || !detectionProperties.isHapticConsumed()) && mVibrator != null) {
            mVibrator.vibrate(mResolveVibrationEffect, SONIFICATION_AUDIO_ATTRIBUTES);
        }
    }
}
