package com.taf.auto.jira.app.ui.update;

import com.taf.auto.jira.xray.ScenarioBundle;

/**
 * Created by AF04261 on 1/5/2017.
 */
final class UploadFeatureError {
    final ScenarioBundle item;
    final String msg;

    UploadFeatureError(ScenarioBundle item, String msg) {
        this.item = item;
        this.msg = msg;
    }
}
