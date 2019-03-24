package com.google.android.systemui.elmyra.actions;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;

import java.util.HashSet;
import java.util.Set;

import com.android.internal.utils.ActionHandler;
import com.android.internal.utils.Config.ActionConfig;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistManager;

import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class CustomActions extends Action {
    protected ContentResolver mResolver;
    protected PowerManager mPm;

    static final Set<String> sCanTurnOnScreenActions = new HashSet<String>();
    static {
        sCanTurnOnScreenActions.add(ActionHandler.SYSTEMUI_TASK_ASSIST);
        sCanTurnOnScreenActions.add(ActionHandler.SYSTEMUI_TASK_CAMERA);
        sCanTurnOnScreenActions.add(ActionHandler.SYSTEMUI_TASK_ASSISTANT_SOUND_SEARCH);
    }

    static final Set<String> sActionsCanExecuteWhenScreenOff = new HashSet<String>();
    static {
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_NO_ACTION);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_TORCH);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_BT);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_WIFI);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_WIFIAP);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_MEDIA_PREVIOUS);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_MEDIA_NEXT);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_MEDIA_PLAY_PAUSE);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_SOUNDMODE_VIB_SILENT);
        sActionsCanExecuteWhenScreenOff.add(ActionHandler.SYSTEMUI_TASK_WAKE_DEVICE);
    }

    public CustomActions(Context context) {
        super(context, null);
        mResolver = context.getContentResolver();
        mPm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    @Override
	public boolean isAvailable() {
        return true;
    }

    public void onTrigger(DetectionProperties detectionProperties) {
        String action = getActionForGesture(detectionProperties.isLongSqueeze());
        boolean isScreenOn = mPm.isScreenOn();
        if (!isScreenOn) {
            if (actionCanTurnOnScreen(action)) {
                ActionHandler.performTask(getContext(), ActionHandler.SYSTEMUI_TASK_WAKE_DEVICE);
                ActionHandler.performTask(getContext(), action);
                return;
            }
            if (canActionExecuteWhileScreenOff(action)) {
                ActionHandler.performTask(getContext(), action);
                return;
            }
        } else {
            ActionHandler.performTask(getContext(), action);
        }
    }

    String getActionForGesture(boolean longSqueeze) {
        String actionConfig = null;
        if (longSqueeze) {
            actionConfig = Settings.Secure.getStringForUser(mResolver,
                    Settings.Secure.LONG_SQUEEZE_SELECTION_SMART_ACTIONS, UserHandle.USER_CURRENT);
        } else {
            actionConfig = Settings.Secure.getStringForUser(mResolver,
                    Settings.Secure.SQUEEZE_SELECTION_SMART_ACTIONS, UserHandle.USER_CURRENT);
        }
        return ActionConfig.getActionFromDelimitedString(getContext(), actionConfig,
                ActionHandler.SYSTEMUI_TASK_NO_ACTION);
    }

    // special actions that need to turn on screen and presumably execute from keyguard
    private static boolean actionCanTurnOnScreen(String action) {
        // let apps/shortcuts launch. User will need to clear keyguard before executing
        if (!action.startsWith(ActionHandler.SYSTEM_PREFIX)) {
            return true;
        }
        return sCanTurnOnScreenActions.contains(action);
    }

    // actions that can execute when screen is off
    private static boolean canActionExecuteWhileScreenOff(String action) {
        // don't launch apps if screen is off
        if (!action.startsWith(ActionHandler.SYSTEM_PREFIX)) {
            return false;
        }
        return sActionsCanExecuteWhenScreenOff.contains(action);
    }
}
