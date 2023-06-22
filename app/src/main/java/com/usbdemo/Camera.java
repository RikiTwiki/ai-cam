package com.usbdemo;

import android.util.Log;

import com.hcusbsdk.Interface.JavaInterface;
import com.hcusbsdk.Interface.USB_AUDIO_STATUS;
import com.hcusbsdk.Interface.USB_IMAGE_WDR;
import com.hcusbsdk.Interface.USB_VIDEO_PARAM;
import com.hcusbsdk.jna.HCUSBSDK;

public class Camera {

    public boolean Config(int lUserID, long dwCommand)
    {
        boolean bRet = false;
        switch ((int)dwCommand)
        {
            case JavaInterface.USB_SET_VIDEO_PARAM: //设置视频参数
                bRet = SetVideoParam(lUserID);
                break;
            default:
                Log.i("[USBDemo]", "Config No support! ");
                break;
        }
        return bRet;
    }

    //设置视频参数
    private boolean SetVideoParam(int lUserID)
    {
        USB_VIDEO_PARAM struVideoParam = new USB_VIDEO_PARAM();
        struVideoParam.dwVideoFormat = JavaInterface.USB_STREAM_MJPEG; //Mjpeg码流
        struVideoParam.dwWidth = 640; //宽
        struVideoParam.dwHeight = 480; //高
        struVideoParam.dwFramerate = 30; //帧率
        struVideoParam.dwBitrate = 0; //用不到
        struVideoParam.dwParamType = 0; //用不到
        struVideoParam.dwValue = 0; //用不到

        if (JavaInterface.getInstance().USB_SetVideoParam(lUserID, struVideoParam))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetVideoParam Success! " +
                    " dwVideoFormat:" + struVideoParam.dwVideoFormat +
                    " dwWidth:" + struVideoParam.dwWidth +
                    " dwHeight:" + struVideoParam.dwHeight +
                    " dwFramerate:" + struVideoParam.dwFramerate +
                    " dwBitrate:" + struVideoParam.dwBitrate +
                    " dwParamType:" + struVideoParam.dwParamType +
                    " dwValue:" + struVideoParam.dwValue);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetVideoParam failed! error:" + JavaInterface.getInstance().USB_GetLastError()+
                    " dwVideoFormat:" + struVideoParam.dwVideoFormat +
                    " dwWidth:" + struVideoParam.dwWidth +
                    " dwHeight:" + struVideoParam.dwHeight +
                    " dwFramerate:" + struVideoParam.dwFramerate +
                    " dwBitrate:" + struVideoParam.dwBitrate +
                    " dwParamType:" + struVideoParam.dwParamType +
                    " dwValue:" + struVideoParam.dwValue);
            return false;
        }
    }


}
