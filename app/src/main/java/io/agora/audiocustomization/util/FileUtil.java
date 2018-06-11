package io.agora.audiocustomization.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.agora.audiocustomization.MyApplication;

public class FileUtil {

    private static final int BUFFER = 4096;

    private static final String[] RAW_FILES = {"music.zip"};

    public static File getMusicFilsDir() {
        return MyApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
    }

    public static String getUnZipMusicDir() {
        return getMusicFilsDir() + File.separator + "music";
    }

    public static File getRecordFilsDir() {
        return MyApplication.getInstance().getExternalFilesDir("record");
    }

    public static File getLogFilesDir() {
        return MyApplication.getInstance().getExternalFilesDir("logs");
    }

    public static File[] getAppMp3Files() {
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".mp3"))
                    return true;
                return false;
            }
        };
        File musicDir = new File(getMusicFilsDir(), "music");
        return musicDir.listFiles(filenameFilter);
    }

    public static void copyRawFilesToSDCardAndUnzip() {
        AssetManager assetManager = MyApplication.getInstance().getResources().getAssets();
        File path = new File(getMusicFilsDir().getPath() + File.separator + "temp" + File.separator);
        path.mkdirs();
        for (String name : RAW_FILES) {
            FileOutputStream dos = null;
            InputStream is = null;
            try {
                dos = new FileOutputStream(new File(path, name));
                is = assetManager.open(name);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    dos.write(buffer, 0, len);
                }
                is.close();
                dos.close();

                if (name.endsWith(".zip")) {
                    unzipFile(path + File.separator + name, getMusicFilsDir().getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception a) {
                    }
                }
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (Exception a) {
                    }
                }
            }
        }

//        removeUnusedAssets();
    }

//    private static void removeUnusedAssets() {
//        Context ctx = MyApplication.getInstance();
//        String[] mixing = ctx.getResources().getStringArray(R.array.music_list_for_mix);
//        String[] effecting = ctx.getResources().getStringArray(R.array.music_list_for_effect);
//
//        ArrayList<String> musicList = new ArrayList<>();
//        musicList.addAll(Arrays.asList(mixing));
//        musicList.addAll(Arrays.asList(effecting));
//
//        File musicPath = new File(AppUtil.APP_DIRECTORY + File.separator + "music");
//        if (musicPath.exists()) {
//            File[] items = musicPath.listFiles();
//            if (items != null && items.length > 0) {
//                for (File item : items) {
//                    if (!musicList.contains(item.getName())) {
//                        item.delete();
//                    }
//                }
//            }
//        }
//    }

    public static void unzipFile(String zipFile, String destPath) {
        if (!destPath.endsWith(File.separator)) {
            destPath += File.separator;
        }
        FileOutputStream fos;
        ZipInputStream zipIn;
        ZipEntry zipEntry;
        File file;
        int buffer;
        byte buf[] = new byte[BUFFER];
        try {
            zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                file = new File(destPath + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    fos = new FileOutputStream(file);
                    while ((buffer = zipIn.read(buf)) > 0) {
                        fos.write(buf, 0, buffer);
                    }
                    fos.close();
                }
                zipIn.closeEntry();
            }
            //SUCCESS
            SpUtils.put(SpUtils.IS_RAW_FILES_READY, true);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
