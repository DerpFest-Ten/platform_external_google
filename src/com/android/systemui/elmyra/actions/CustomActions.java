package com.google.android.systemui.elmyra.actions;

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;

import com.android.internal.utils.ActionHandler;
import com.android.internal.utils.Config.ActionConfig;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistManager;

import com.google.android.systemui.elmyra.sensors.GestureSensor.DetectionProperties;

public class CustomActions extends Action {
    protected AssistManager mAssistManager;

    public CustomActions(Context context) {
        super(context, null);
        mAssistManager = Dependency.get(AssistManager.class);
    }

    @Override
	public boolean isAvailable() {
        return true;
    }

    public void onTrigger(DetectionProperties detectionProperties) {
        final ContentResolver resolver = getContext().getContentResolver();
        String actionConfig = Settings.Secure.getStringForUser(resolver,
                Settings.Secure.SQUEEZE_SELECTION_SMART_ACTIONS, UserHandle.USER_CURRENT);
        String action = ActionConfig.getActionFromDelimitedString(getContext(), actionConfig,
                ActionHandler.SYSTEMUI_TASK_NO_ACTION);
        ActionHandler.performTask(getContext(), action);
    }
}
