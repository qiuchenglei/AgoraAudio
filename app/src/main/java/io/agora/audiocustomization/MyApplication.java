package io.agora.audiocustomization;

import android.app.Application;
import android.content.Intent;

import java.io.File;
import java.util.ArrayList;

import io.agora.audiocustomization.manager.AgoraManager;
import io.agora.audiocustomization.service.RecordService;
import io.agora.audiocustomization.util.FileUtil;
import io.agora.audiocustomization.util.SpUtils;

/**
 * Created by ChengleiQiu on 2018/1/17.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        configRawFiles();

        startService(new Intent(this, RecordService.class));

    }

    private void configRawFiles() {
        if (!(boolean) SpUtils.get(SpUtils.IS_RAW_FILES_READY, false)) {
            ArrayList<String> defMusicList = new ArrayList<>();
            ArrayList<String> defPrivateParameterList = new ArrayList<>();
            defMusicList.add(FileUtil.getUnZipMusicDir() + File.separator + "omg.mp3");
            defMusicList.add(FileUtil.getUnZipMusicDir() + File.separator + "Du Du Lu.mp3");
            defMusicList.add(FileUtil.getUnZipMusicDir() + File.separator + "If I Die Young.mp3");
            defMusicList.add(FileUtil.getUnZipMusicDir() + File.separator + "Beethoven Virus.mp3");
            defPrivateParameterList.add("{\"che.audio.start_debug_recording\":\"noname\"}");
            SpUtils.putStrListValue(SpUtils.MUSIC_EDIT_URI_LIST, defMusicList);
            SpUtils.putStrListValue(SpUtils.PRIVATE_PARAMETER_LIST, defPrivateParameterList);
            new Thread() {
                @Override
                public void run() {
                    FileUtil.copyRawFilesToSDCardAndUnzip();
                }
            }.start();
        }
    }

    public static MyApplication getInstance() {
        return instance;
    }

}
