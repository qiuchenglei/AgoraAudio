package io.agora.audiocustomization.activity;

import android.support.v7.app.AppCompatActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.agora.audiocustomization.manager.AgoraManager;
import io.agora.rtc.RtcEngine;

/**
 * Created by ChengleiQiu on 2018/1/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected final String TAG = getClass().getSimpleName();
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected RtcEngine agoraRtcEngine() {
        return AgoraManager.instance().getRtcEngine();
    }

}
