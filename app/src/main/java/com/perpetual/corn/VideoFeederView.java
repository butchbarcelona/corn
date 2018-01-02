package com.perpetual.corn;

import android.app.Service;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.perpetual.corn.dji.PresentableView;
import com.perpetual.corn.dji.VideoFeedView;

import java.util.List;

import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.keysdk.AirLinkKey;
import dji.keysdk.KeyManager;
import dji.keysdk.ProductKey;
import dji.keysdk.callback.SetCallback;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Class that manage live video feed from DJI products to the mobile device.
 * Also give the example of "getPrimaryVideoFeed" and "getSecondaryVideoFeed".
 */
public class VideoFeederView extends LinearLayout
    implements View.OnClickListener, PresentableView, CompoundButton.OnCheckedChangeListener {


    private VideoFeedView primaryVideoFeed;
    private VideoFeeder.PhysicalSourceListener sourceListener;
    private AirLinkKey extEnabledKey;
    private AirLinkKey lbBandwidthKey;
    private AirLinkKey hdmiBandwidthKey;
    private AirLinkKey mainCameraBandwidthKey;
    private SetCallback setBandwidthCallback;
    private SetCallback setExtEnableCallback;
    private View primaryCoverView;
    private String cameraListStr;

    public VideoFeederView(Context context) {
        super(context);
        init(context);
    }

    public VideoFeederView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoFeederView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setClickable(true);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_video_feeder, this, true);

        initAllKeys();
        initUI();
        initCallbacks();
        setUpListeners();

    }

    private void initUI() {

        primaryVideoFeed = (VideoFeedView) findViewById(R.id.primary_video_feed);
        primaryCoverView = findViewById(R.id.primary_cover_view);
        primaryVideoFeed.setCoverView(primaryCoverView);
        disableAllButtons();
        initEXTSwitch();
    }

    private void initAllKeys() {
        extEnabledKey = AirLinkKey.createLightbridgeLinkKey(AirLinkKey.IS_EXT_VIDEO_INPUT_PORT_ENABLED);
        lbBandwidthKey = AirLinkKey.createLightbridgeLinkKey(AirLinkKey.BANDWIDTH_ALLOCATION_FOR_LB_VIDEO_INPUT_PORT);
        hdmiBandwidthKey =
            AirLinkKey.createLightbridgeLinkKey(AirLinkKey.BANDWIDTH_ALLOCATION_FOR_HDMI_VIDEO_INPUT_PORT);
        mainCameraBandwidthKey = AirLinkKey.createLightbridgeLinkKey(AirLinkKey.BANDWIDTH_ALLOCATION_FOR_LEFT_CAMERA);

    }

    private void initCallbacks() {
        setBandwidthCallback = new SetCallback() {
            @Override
            public void onSuccess() {
                Log.d("DJICORN","Set key value successfully");
                if (primaryVideoFeed != null) {
                    primaryVideoFeed.changeSourceResetKeyFrame();
                }
            }

            @Override
            public void onFailure(@NonNull DJIError error) {
                Log.d("DJICORN","Failed to set: " + error.getDescription());
            }
        };

        setExtEnableCallback = new SetCallback() {
            @Override
            public void onSuccess() {
                updateExtSwitchValue(null);
            }

            @Override
            public void onFailure(@NonNull DJIError error) {
                updateExtSwitchValue(null);
            }
        };
    }

    private void initEXTSwitch() {
        updateExtSwitchValue(null);
    }

    private void updateExtSwitchValue(Object value) {
        if (value == null && KeyManager.getInstance() != null) {
            value = KeyManager.getInstance().getValue(extEnabledKey);
        }
        final Object switchValue = value;
        if (switchValue != null) {
            VideoFeederView.this.post(new Runnable() {
                @Override
                public void run() {
                    enableExtButtons((Boolean) switchValue);
                }
            });
        }
    }

    private void enableExtButtons(boolean isExtEnabled) {

    }

    private void disableAllButtons() {

    }

    private void setUpListeners() {
        sourceListener = new VideoFeeder.PhysicalSourceListener() {
            @Override
            public void onChange(VideoFeeder.VideoFeed videoFeed, VideoFeeder.PhysicalSource newPhysicalSource) {
                if (videoFeed == VideoFeeder.getInstance().getPrimaryVideoFeed()) {
                    String newText = "Primary Source: " + newPhysicalSource.toString();
                   Log.d("DJICORN", " " + newText);
                }
                if (videoFeed == VideoFeeder.getInstance().getSecondaryVideoFeed()) {
                   Log.d("DJICORN", "" + " Secondary Source: " + newPhysicalSource.toString());
                }
            }
        };

        setVideoFeederListeners(true);
    }

    private void tearDownListeners() {
        setVideoFeederListeners(false);
    }

    private void setVideoFeederListeners(boolean isOpen) {
        if (VideoFeeder.getInstance() == null) return;

        final BaseProduct product = DJISDKManager.getInstance().getProduct();
        updateM210Buttons();
        if (product != null) {

            if (isOpen) {
                primaryVideoFeed.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
                String newText = "Primary Source: " + VideoFeeder.getInstance().getPrimaryVideoFeed().getVideoSource().name();
               Log.d("DJICORN"," "  + " " +  newText);

               /* if (isMultiStreamPlatform()) {
                    fpvVideoFeed.registerLiveVideo(VideoFeeder.getInstance().getSecondaryVideoFeed(), false);
                    String newTextFpv = "Secondary Source: " + VideoFeeder.getInstance().getSecondaryVideoFeed().getVideoSource().name();
                   Log.d("DJICORN",fpvVideoFeedTitle + " " + newTextFpv);
                }*/
                VideoFeeder.getInstance().addPhysicalSourceListener(sourceListener);
            } else {
                VideoFeeder.getInstance().removePhysicalSourceListener(sourceListener);
                VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(null);
                if (isMultiStreamPlatform()) {
                    VideoFeeder.getInstance().getSecondaryVideoFeed().setCallback(null);
                }
            }
        }
    }

    private void updateM210Buttons() {
        if (isM210TwoCameraConnected()) {

            VideoFeederView.this.post(new Runnable() {
                @Override
                public void run() {
                    disableAllButtons();

                }
            });
        }
    }

    @Override
    public void onClick(View view) {


    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }



    private boolean isM210TwoCameraConnected() {

        Object model = null;
        if (KeyManager.getInstance() != null) {
            model = KeyManager.getInstance().getValue(ProductKey.create(ProductKey.MODEL_NAME));
        }
        if (model != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null && (product instanceof Aircraft)) {
                List<Camera> cameraList = ((Aircraft) product).getCameras();
                if (cameraList != null) {
                    if (cameraListStr == null) {
                        cameraListStr = new String();
                    }
                    cameraListStr = "";
                    for (int i = 0; i < cameraList.size(); i++) {
                        Camera camera = cameraList.get(i);
                        cameraListStr += "Camera "
                            + i
                            + " is "
                            + camera.getDisplayName()
                            + " is connected "
                            + camera.isConnected()
                            + " camera component index is "
                            + +camera.getIndex()
                            + "\n";
                    }
                }
                if ((model == Model.MATRICE_210 || model == Model.MATRICE_210_RTK)) {
                    return (cameraList != null
                        && cameraList.size() == 2
                        && cameraList.get(0).isConnected()
                        && cameraList.get(1).isConnected());
                }
            }
        }

        return false;
    }

    private boolean isMultiStreamPlatform() {
        Model model = DJISDKManager.getInstance().getProduct().getModel();
        return model != null && (model == Model.INSPIRE_2
            || model == Model.MATRICE_200
            || model == Model.MATRICE_210
            || model == Model.MATRICE_210_RTK
            || model == Model.MATRICE_600
            || model == Model.MATRICE_600_PRO
            || model == Model.A3
            || model == Model.N3);
    }

    @Override
    public int getDescription() {
        return R.string.component_listview_video_feeder;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        tearDownListeners();
        super.onDetachedFromWindow();
    }

    @NonNull
    @Override
    public String getHint() {
        return this.getClass().getSimpleName() + ".java";
    }
}
