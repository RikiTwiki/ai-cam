package com.usbdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.hcusbsdk.Interface.FStreamCallBack;
import com.hcusbsdk.Interface.JavaInterface;
import com.hcusbsdk.Interface.USB_FRAME_INFO;
import com.hcusbsdk.Interface.USB_ROI_MAX_TEMPERATURE_SEARCH;
import com.hcusbsdk.Interface.USB_ROI_MAX_TEMPERATURE_SEARCH_RESULT;
import com.hcusbsdk.Interface.USB_STREAM_CALLBACK_PARAM;
import com.hcusbsdk.Interface.USB_VIDEO_PARAM;
import com.hcusbsdk.jna.HCUSBSDKByJNA;
import com.sun.jna.Pointer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class PreviewActivity extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder m_hHolder;
    private int m_dwUserID = JavaInterface.USB_INVALID_USER_ID; //设备句柄
    private int m_dwHandle = JavaInterface.USB_INVALID_CHANNEL; //预览句柄
    private SurfaceHolder m_pHolder = null;
    //surfaceview分辨率
    private int m_dwScreenWidth = 240;
    private int m_dwScreenHeight = 320;
    //码流分辨率
    private int m_dwStreamWidth = 240;
    private int m_dwStreamHeight = 320;
    private boolean m_bPreviewStatus = false; //预览状态： true-正在预览

    public PreviewActivity(MainActivity demoActivity) {
        super((Context) demoActivity);
        m_hHolder = this.getHolder();
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    //设置设备句柄
    public void SetUserID(int iUserID) {
        m_dwUserID = iUserID;
        Log.i("[USBDemo]", "SetUserID Success! m_dwUserID:" + m_dwUserID);
    }

    //设置窗口大小
    public void SetScreenResolution(int width, int height) {
        m_dwScreenWidth = width;
        m_dwScreenHeight = height;
    }

    //设置码流分辨率
    public void SetStreamResolution(int width, int height) {
        m_dwStreamWidth = width;
        m_dwStreamHeight = height;
    }

    //获取预览状态
    public boolean GetPreviewStatus() {
        Log.i("[USBDemo]", "GetPreviewStatus Success! m_bPreviewStatus: " + m_bPreviewStatus);
        return m_bPreviewStatus;
    }

    //开始预览
    public boolean StartPreview(SurfaceHolder pHolder) {

        //获取预览状态
        if (m_bPreviewStatus) {
            //正在预览
            Log.i("[USBDemo]", "预览中!");
            return true;
        }
        m_hHolder = pHolder;

        //第一步，设置视频参数
        USB_VIDEO_PARAM struVideoParam = new USB_VIDEO_PARAM();
        struVideoParam.dwVideoFormat = JavaInterface.USB_STREAM_MJPEG; //Mjpeg码流
        struVideoParam.dwWidth = m_dwStreamWidth; //宽
        struVideoParam.dwHeight = m_dwStreamHeight; //高
        struVideoParam.dwFramerate = 30; //帧率
        struVideoParam.dwBitrate = 0; //用不到
        struVideoParam.dwParamType = 0; //用不到
        struVideoParam.dwValue = 0; //用不到

        if (JavaInterface.getInstance().USB_SetVideoParam(m_dwUserID, struVideoParam)) {
            //登录成功
            Log.i("[USBDemo]", "USB_SetVideoParam Success! " +
                    " dwVideoFormat:" + struVideoParam.dwVideoFormat +
                    " dwWidth:" + struVideoParam.dwWidth +
                    " dwHeight:" + struVideoParam.dwHeight +
                    " dwFramerate:" + struVideoParam.dwFramerate +
                    " dwBitrate:" + struVideoParam.dwBitrate +
                    " dwParamType:" + struVideoParam.dwParamType +
                    " dwValue:" + struVideoParam.dwValue);
        } else {
            Log.e("[USBDemo]", "USB_SetVideoParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwVideoFormat:" + struVideoParam.dwVideoFormat +
                    " dwWidth:" + struVideoParam.dwWidth +
                    " dwHeight:" + struVideoParam.dwHeight +
                    " dwFramerate:" + struVideoParam.dwFramerate +
                    " dwBitrate:" + struVideoParam.dwBitrate +
                    " dwParamType:" + struVideoParam.dwParamType +
                    " dwValue:" + struVideoParam.dwValue);
            return false;
        }

        //第二步，开启码流回调
        USB_STREAM_CALLBACK_PARAM struStreamCBParam = new USB_STREAM_CALLBACK_PARAM();
        struStreamCBParam.dwStreamType = JavaInterface.USB_STREAM_MJPEG; //Mjpeg裸码流
        struStreamCBParam.fnStreamCallBack = m_fnStreamCallBack; //回调函数

        m_dwHandle = JavaInterface.getInstance().USB_StartStreamCallback(m_dwUserID, struStreamCBParam);
        if (m_dwHandle != JavaInterface.USB_INVALID_CHANNEL) {
            //开启码流回调成功
            Log.i("[USBDemo]", "USB_StartStreamCallback Success! ");
        } else {
            Log.e("[USBDemo]", "USB_StartStreamCallback failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }


        m_bPreviewStatus = true;
        return true;
    }

    //回调函数
    public FStreamCallBack m_fnStreamCallBack = new FnStreamCallBack();

    public class FnStreamCallBack implements FStreamCallBack
    {
        @Override
        public void invoke(int handle, USB_FRAME_INFO struFrameInfo) {
            DrawPicture(struFrameInfo.pBuf, struFrameInfo.dwBufSize);
        }
    };

    //停止预览
    public boolean StopPreview()
    {
        //获取预览状态
        if (!m_bPreviewStatus)
        {
            //未开启预览
            Log.i("[USBDemo]", "未开启预览!");
            return true;
        }

        if (JavaInterface.getInstance().USB_StopChannel(m_dwUserID, m_dwHandle))
        {
            //停止成功
            Log.i("[USBDemo]", "USB_StopChannel Success! " +
                    " m_dwUserID:" + m_dwUserID +
                    " m_dwHandle:" + m_dwHandle);
            m_bPreviewStatus = false;
            return true;
        } else {
            Log.e("[USBDemo]", "USB_StopChannel failed! error:" + JavaInterface.getInstance().USB_GetLastError()+
                    " m_dwUserID:" + m_dwUserID +
                    " m_dwHandle:" + m_dwHandle);
            return false;
        }
    }

    //将一张mjpeg图片绘制到surfaceview上
    private void DrawPicture(byte[] data, int length)
    {
        Canvas canvas = null;
        canvas = m_hHolder.lockCanvas();//获取目标画图区域，无参数表示锁定的是全部绘图区
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, length);
        Bitmap birmapScale = ScaleBitmap(bitmap, m_dwScreenWidth, m_dwScreenHeight);
        Rect rect = new Rect(0,0,m_dwScreenWidth,m_dwScreenHeight);
        if (birmapScale != null)
        {
            canvas.drawBitmap(birmapScale, null, rect, null);
        }
        m_hHolder.unlockCanvasAndPost(canvas); //解除锁定并显示
    }

    //缩放图片
    private Bitmap ScaleBitmap(Bitmap origin, int newWidth, int newHeight)
    {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    private double temp (int lUserID)
    {
        USB_ROI_MAX_TEMPERATURE_SEARCH struROITemperatureSearch = new USB_ROI_MAX_TEMPERATURE_SEARCH();
        struROITemperatureSearch.byJpegPicEnabled = 1;
        struROITemperatureSearch.byMaxTemperatureOverlay = 1;
        struROITemperatureSearch.byRegionsOverlay = 1;
        struROITemperatureSearch.byROIRegionNum = 1;
        struROITemperatureSearch.struThermalROIRegion[0].byROIRegionID = 1;
        struROITemperatureSearch.struThermalROIRegion[0].byROIRegionEnabled = 1;
        struROITemperatureSearch.struThermalROIRegion[0].dwROIRegionX = 250;
        struROITemperatureSearch.struThermalROIRegion[0].dwROIRegionY = 250;
        struROITemperatureSearch.struThermalROIRegion[0].dwROIRegionHeight =250;
        struROITemperatureSearch.struThermalROIRegion[0].dwROIRegionWidth = 250;
        struROITemperatureSearch.struThermalROIRegion[0].dwDistance = 50;

        USB_ROI_MAX_TEMPERATURE_SEARCH_RESULT struROITemperatureSearchResult = new USB_ROI_MAX_TEMPERATURE_SEARCH_RESULT();
        struROITemperatureSearchResult.dwJpegPicLen = JavaInterface.MAX_JEPG_DATA_SIZE;

        if (JavaInterface.getInstance().USB_GetROITemperatureSearch(lUserID, struROITemperatureSearch, struROITemperatureSearchResult))
        {
            //配置成功
            Log.i("[USBDemo]", "struROITemperatureSearchResult Success! " +
                    " dwMaxP2PTemperature:" + struROITemperatureSearchResult.dwMaxP2PTemperature +
                    " dwVisibleP2PMaxTemperaturePointX:" + struROITemperatureSearchResult.dwVisibleP2PMaxTemperaturePointX +
                    " dwVisibleP2PMaxTemperaturePointY:" + struROITemperatureSearchResult.dwVisibleP2PMaxTemperaturePointY +
                    " dwThermalP2PMaxTemperaturePointX:" + struROITemperatureSearchResult.dwThermalP2PMaxTemperaturePointX +
                    " dwThermalP2PMaxTemperaturePointY:" + struROITemperatureSearchResult.dwThermalP2PMaxTemperaturePointY +
                    " byROIRegionNum:" + struROITemperatureSearchResult.byROIRegionNum +
                    " dwJpegPicLen:" + struROITemperatureSearchResult.dwJpegPicLen);
            for (int i = 0; i < struROITemperatureSearchResult.byROIRegionNum; i++)
            {
                Log.i("[USBDemo]", ""+ i +
                        " byROIRegionID:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].byROIRegionID +
                        " dwMaxROIRegionTemperature:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwMaxROIRegionTemperature +
                        " dwVisibleROIRegionMaxTemperaturePointX:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwVisibleROIRegionMaxTemperaturePointX +
                        " dwVisibleROIRegionMaxTemperaturePointY:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwVisibleROIRegionMaxTemperaturePointY +
                        " dwThermalROIRegionMaxTemperaturePointX:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwThermalROIRegionMaxTemperaturePointX +
                        " dwThermalROIRegionMaxTemperaturePointY:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwThermalROIRegionMaxTemperaturePointY);
            }

            //jpeg
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            FileOutputStream file = null;
            try {
                file = new FileOutputStream("/mnt/sdcard/sdklog/" + date + "_ROI.jpg");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                file.write(struROITemperatureSearchResult.byJpegPic, 0, struROITemperatureSearchResult.dwJpegPicLen);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            Log.e("[USBDemo]", "struROITemperatureSearchResult failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwMaxP2PTemperature:" + struROITemperatureSearchResult.dwMaxP2PTemperature +
                    " dwVisibleP2PMaxTemperaturePointX:" + struROITemperatureSearchResult.dwVisibleP2PMaxTemperaturePointX +
                    " dwVisibleP2PMaxTemperaturePointY:" + struROITemperatureSearchResult.dwVisibleP2PMaxTemperaturePointY +
                    " dwThermalP2PMaxTemperaturePointX:" + struROITemperatureSearchResult.dwThermalP2PMaxTemperaturePointX +
                    " dwThermalP2PMaxTemperaturePointY:" + struROITemperatureSearchResult.dwThermalP2PMaxTemperaturePointY +
                    " byROIRegionNum:" + struROITemperatureSearchResult.byROIRegionNum +
                    " dwJpegPicLen:" + struROITemperatureSearchResult.dwJpegPicLen);

            for (int i = 0; i < struROITemperatureSearchResult.byROIRegionNum; i++)
            {
                Log.e("[USBDemo]", ""+ i +
                        " byROIRegionID:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].byROIRegionID +
                        " dwMaxROIRegionTemperature:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwMaxROIRegionTemperature +
                        " dwVisibleROIRegionMaxTemperaturePointX:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwVisibleROIRegionMaxTemperaturePointX +
                        " dwVisibleROIRegionMaxTemperaturePointY:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwVisibleROIRegionMaxTemperaturePointY +
                        " dwThermalROIRegionMaxTemperaturePointX:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwThermalROIRegionMaxTemperaturePointX +
                        " dwThermalROIRegionMaxTemperaturePointY:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwThermalROIRegionMaxTemperaturePointY);
            }
        }
        Toast.makeText(this.getContext(), "temp:" + struROITemperatureSearchResult.dwMaxP2PTemperature, Toast.LENGTH_SHORT).show();
        return struROITemperatureSearchResult.dwMaxP2PTemperature;
    }
}
