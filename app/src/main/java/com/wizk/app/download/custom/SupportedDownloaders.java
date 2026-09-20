package com.wizk.app.download.custom;

import java.util.ArrayList;
import java.util.List;

public final class SupportedDownloaders {

    // Ngăn việc khởi tạo thể hiện của lớp tiện ích
    private SupportedDownloaders() {
    }

    // Lấy danh sách các ứng dụng tải xuống bên thứ ba được hỗ trợ sẵn
    public static List<DownloaderInfo> getSupportedDownloaders() {
        List<DownloaderInfo> list = new ArrayList<>();

        list.add(new DownloaderInfo("system"));

        list.add(new DownloaderInfo(
                "com.dv.adm.pay",
                "com.dv.get.Main",
                new String[]{"com.dv.get.AEditor", "com.dv.adm.AEditor"},
                "ADM+"
        ));

        list.add(new DownloaderInfo(
                "com.dv.adm",
                "com.dv.get.Main",
                new String[]{"com.dv.get.AEditor", "com.dv.adm.AEditor"},
                "ADM"
        ));

        list.add(new DownloaderInfo(
                "idm.internet.download.manager.plus",
                "idm.internet.download.manager.MainActivity",
                new String[]{
                        "idm.internet.download.manager.Downloader",
                        "idm.internet.download.manager.UrlHandlerDownloader"
                },
                "1DM+"
        ));

        list.add(new DownloaderInfo(
                "idm.internet.download.manager",
                "idm.internet.download.manager.MainActivity",
                new String[]{
                        "idm.internet.download.manager.Downloader",
                        "idm.internet.download.manager.UrlHandlerDownloader"
                },
                "1DM"
        ));

        list.add(new DownloaderInfo(
                "idm.internet.download.manager.adm.lite",
                "idm.internet.download.manager.MainActivity",
                new String[]{
                        "idm.internet.download.manager.Downloader",
                        "idm.internet.download.manager.UrlHandlerDownloader"
                },
                "1DM Lite"
        ));

        list.add(new DownloaderInfo(
                "org.freedownloadmanager.fdm",
                "org.freedownloadmanager.fdm.MyActivity",
                "org.freedownloadmanager.fdm.SendActivity",
                "FDM"
        ));

        list.add(new DownloaderInfo(
                "com.dv.get",
                "com.dv.get.Main",
                "com.dv.get.AEditor",
                "DVGet"
        ));

        list.add(new DownloaderInfo(
                "com.tachibana.downloader",
                "com.tachibana.downloader.ui.main.MainActivity",
                "com.tachibana.downloader.ui.adddownload.AddDownloadActivity",
                "Download Navi"
        ));

        list.add(new DownloaderInfo(
                "com.gianlu.aria2app",
                "com.gianlu.aria2app.main.MainActivity",
                "com.gianlu.aria2app.LoadingActivity",
                "Aria2App"
        ));

        list.add(new DownloaderInfo(
                "com.gopeed",
                "com.gopeed.MainActivity",
                "com.gopeed.MainActivity",
                "Gopeed"
        ));

        list.add(new DownloaderInfo(
                "com.gopeed.gopeed",
                "com.gopeed.gopeed.MainActivity",
                "com.gopeed.gopeed.MainActivity",
                "Gopeed"
        ));

        list.add(new DownloaderInfo(
                "com.abdownloadmanager",
                "com.abdownloadmanager.android.ui.MainActivity",
                "com.abdownloadmanager.android.pages.add.AddDownloadActivity",
                "AB DM"
        ));

        list.add(new DownloaderInfo(
                "com.fluxdown.app",
                "com.fluxdown.app.MainActivity",
                "com.fluxdown.app.MainActivity",
                "FluxDown"
        ));

        return list;
    }
}
