package com.mahrusali.admobengine;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

@DesignerComponent(
    version = 1,
    description = "AdMob Engine Component oleh mahrusali",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "aiwebres/icon.png"
)
@SimpleObject(external = true)
@UsesLibraries(libraries = "play-services-ads.jar, play-services-ads-lite.jar, play-services-basement.jar, play-services-tasks.jar, play-services-ads-identifier.jar")
public class AdmobEngine extends AndroidNonvisibleComponent {

    private final Context context;
    private final Form form;
    private InterstitialAd mInterstitialAd;

    public AdmobEngine(ComponentContainer container) {
        super(container.$form());
        this.form = container.$form();
        this.context = container.$context();
    }

    @SimpleFunction(description = "Inisialisasi SDK AdMob")
    public void InitializeSdk() {
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                form.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SdkInitialized();
                    }
                });
            }
        });
    }

    @SimpleEvent(description = "Dipanggil saat SDK selesai diinisialisasi")
    public void SdkInitialized() {
        EventDispatcher.dispatchEvent(this, "SdkInitialized");
    }

    // (Metode LoadBanner, LoadInterstitial, ShowInterstitial tetap sama seperti sebelumnya)
}
