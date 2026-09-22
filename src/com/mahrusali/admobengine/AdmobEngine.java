package com.mahrusali.admobengine;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.androidmanifest.MetaDataElement;
import com.google.appinventor.components.annotations.androidmanifest.UsesManifests;
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
    description = "AdMob Engine Extension dengan Manifest Injector oleh mahrusali",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "aiwebres/icon.png"
)
@SimpleObject(external = true)

// Injeksi App ID AdMob langsung ke AndroidManifest.xml
@UsesManifests(
    metaDataElements = {
        @MetaDataElement(
            name = "com.google.android.gms.ads.APPLICATION_ID",
            value = "ca-app-pub-3940256099942544~3347511713" // GANTI DENGAN APP ID ADMOB KAMU
        )
    }
)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")
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

    @SimpleEvent(description = "Dipanggil saat AdMob SDK selesai diinisialisasi")
    public void SdkInitialized() {
        EventDispatcher.dispatchEvent(this, "SdkInitialized");
    }

    @SimpleFunction(description = "Memuat dan menampilkan Banner Ad ke dalam Layout Container")
    public void LoadBanner(final AndroidViewComponent container, final String adUnitId) {
        form.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = container.getView();
                if (view instanceof LinearLayout) {
                    LinearLayout layout = (LinearLayout) view;
                    layout.removeAllViews();

                    AdView adView = new AdView(context);
                    adView.setAdSize(AdSize.BANNER);
                    adView.setAdUnitId(adUnitId);

                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            BannerLoaded();
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            BannerFailedToLoad(adError.getMessage());
                        }
                    });

                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView.loadAd(adRequest);
                    layout.addView(adView);
                } else {
                    BannerFailedToLoad("Container harus berupa Layout / Arrangement Component!");
                }
            }
        });
    }

    @SimpleEvent(description = "Dipanggil saat iklan Banner berhasil dimuat")
    public void BannerLoaded() {
        EventDispatcher.dispatchEvent(this, "BannerLoaded");
    }

    @SimpleEvent(description = "Dipanggil saat iklan Banner gagal dimuat")
    public void BannerFailedToLoad(String error) {
        EventDispatcher.dispatchEvent(this, "BannerFailedToLoad", error);
    }

    @SimpleFunction(description = "Memuat iklan Interstitial")
    public void LoadInterstitial(final String adUnitId) {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        mInterstitialAd = null;
                        form.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                InterstitialDismissed();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                        mInterstitialAd = null;
                        final String errMsg = adError.getMessage();
                        form.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                InterstitialFailedToLoad(errMsg);
                            }
                        });
                    }
                });

                form.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InterstitialLoaded();
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(final LoadAdError loadAdError) {
                mInterstitialAd = null;
                form.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InterstitialFailedToLoad(loadAdError.getMessage());
                    }
                });
            }
        });
    }

    @SimpleFunction(description = "Menampilkan iklan Interstitial yang sudah dimuat")
    public void ShowInterstitial() {
        form.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show((Activity) form);
                } else {
                    InterstitialFailedToLoad("Iklan Interstitial belum siap atau gagal dimuat.");
                }
            }
        });
    }

    @SimpleEvent(description = "Dipanggil saat iklan Interstitial berhasil dimuat")
    public void InterstitialLoaded() {
        EventDispatcher.dispatchEvent(this, "InterstitialLoaded");
    }

    @SimpleEvent(description = "Dipanggil saat iklan Interstitial gagal dimuat")
    public void InterstitialFailedToLoad(String error) {
        EventDispatcher.dispatchEvent(this, "InterstitialFailedToLoad", error);
    }

    @SimpleEvent(description = "Dipanggil saat iklan Interstitial ditutup oleh pengguna")
    public void InterstitialDismissed() {
        EventDispatcher.dispatchEvent(this, "InterstitialDismissed");
    }
}
