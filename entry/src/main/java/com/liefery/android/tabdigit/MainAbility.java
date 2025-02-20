package com.liefery.android.tabdigit;

import com.xenione.digit.TabDigit;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Text;
import ohos.agp.render.render3d.Engine;
import ohos.agp.render.render3d.impl.AgpEngineFactory;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.util.Optional;

public class MainAbility extends Ability implements Runnable {
    TabDigit tabDigit1;
    TaskDispatcher uiTaskDispatcher = getUITaskDispatcher();
    EventRunner eventRunner;
    EventHandler eventHandler;

    private static final HiLogLabel LABEL_LOG = new HiLogLabel(HiLog.LOG_APP, 0x00201, "-MainAbility-");
    Engine ee;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        try {
            tabDigit1 = (TabDigit) findComponentById(ResourceTable.Id_charView1);
            HiLog.warn(LABEL_LOG, "MainAbility: onStart" + tabDigit1);
            assert tabDigit1 != null;
            eventHandler = new EventHandler(EventRunner.getMainEventRunner());
            eventHandler.postTask(this, 1000);
//            uiTaskDispatcher = getUITaskDispatcher();
//            HiLog.warn(LABEL_LOG, "MainAbility: uiTaskDispatcher  "+uiTaskDispatcher);
//            uiTaskDispatcher.delayDispatch(this,1000);
//            HiLog.warn(LABEL_LOG, "MainAbilitySlice: onStart");
        } catch (Exception ex) {
            HiLog.warn(LABEL_LOG, "MainAbilitySlice: ex " + ex);
            for (StackTraceElement st : ex.getStackTrace()) {
                HiLog.warn(LABEL_LOG, "" + st);

            }
        }
    }

    @Override
    public void run() {
        HiLog.warn(LABEL_LOG, "MainAbility: run");
        tabDigit1.start();
//        uiTaskDispatcher.delayDispatch(this,1000);
//        eventHandler.postTask(this, 1000);

    }
}
