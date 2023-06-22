package com.usbdemo;

import android.util.Log;

import com.hcusbsdk.Interface.JavaInterface;
import com.hcusbsdk.Interface.USB_AUDIO_STATUS;
import com.hcusbsdk.Interface.USB_IMAGE_WDR;
import com.hcusbsdk.jna.HCUSBSDK;

public class Transfer {
    private int m_lLogDataHandle;
    private int m_lAudioDataHandle;
    private int m_lEncryptDataHandle;

    public boolean Config(int lUserID, long dwCommand)
    {
        boolean bRet = false;
        switch ((int)dwCommand)
        {
            case JavaInterface.USB_SET_SYSTEM_ENCRYPT_DATA: //设备加密
                bRet = USB_UploadEncryptData(lUserID);
                break;
            case JavaInterface.USB_GET_SYSTEM_LOG_DATA: //日志文件导出
                bRet = USB_DownloadLogData(lUserID);
                break;
            case JavaInterface.USB_SET_IMAGE_WDR:
                bRet = USB_SetImageWDR(lUserID);
                break;
            case JavaInterface.USB_GET_AUDIO_IN_STATUS:
                bRet = USB_GetAudioInStatus(lUserID);
                break;
            case JavaInterface.USB_GET_AUDIO_DUMP_DATA: //音频数据导出
                bRet = USB_DownloadAudioData(lUserID);
                break;
            default:
                Log.i("[USBDemo]", "Config No support! ");
                break;
        }
        return bRet;
    }

    //设备加密
    private boolean USB_UploadEncryptData(int lUserID)
    {
        String sFileName = new String("/mnt/sdcard/EncryptData.dav");
        m_lEncryptDataHandle = JavaInterface.getInstance().USB_UploadEncryptData(lUserID, sFileName);
        if (m_lEncryptDataHandle > 0)
        {
            Log.i("[USBDemo]","USB_UploadEncryptData Success! Handle:" + m_lEncryptDataHandle +
                    " FileName:" + sFileName);
            return true;
        }
        else
        {
            Log.e("[USBDemo]","USB_UploadEncryptData failed! error:" + HCUSBSDK.getInstance().USB_GetLastError() +
                    " FileName:" + sFileName);
            return false;
        }
    }

    //日志文件导出
    private boolean USB_DownloadLogData(int lUserID)
    {
        String sFileName = new String("/mnt/sdcard/LogData.txt");
        m_lLogDataHandle = JavaInterface.getInstance().USB_DownloadLogData(lUserID, sFileName);
        if (m_lLogDataHandle > 0)
        {
            Log.i("[USBDemo]","USB_DownloadLogData Success! Handle:" + m_lLogDataHandle +
                    " FileName:" + sFileName);
            return true;
        }
        else
        {
            Log.e("[USBDemo]","USB_DownloadLogData failed! error:" + HCUSBSDK.getInstance().USB_GetLastError() +
                    " FileName:" + sFileName);
            return false;
        }
    }

    //设置图像WDR
    private boolean USB_SetImageWDR(int lUserID)
    {
        USB_IMAGE_WDR struImageWDR = new USB_IMAGE_WDR();
        struImageWDR.byEnabled = 1;
        struImageWDR.byMode = 1;
        struImageWDR.byLevel = 1;
        if (JavaInterface.getInstance().USB_SetImageWDR(lUserID, struImageWDR))
        {
            //设置成功
            Log.i("[USBDemo]", "USB_SetImageWDR Success!" +
                    " byEnabled:" + struImageWDR.byEnabled + " byMode:" + struImageWDR.byMode + " byLevel:" + struImageWDR.byLevel);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetImageWDR failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //获取音频输入状态
    private boolean USB_GetAudioInStatus(int lUserID)
    {
        USB_AUDIO_STATUS struAudioStatus = new USB_AUDIO_STATUS();
        struAudioStatus.byChannelID = 0;
        if (JavaInterface.getInstance().USB_GetAudioInStatus(lUserID, struAudioStatus))
        {
            //获取成功
            Log.i("[USBDemo]", "USB_GetAudioInStatus Success!" +
                    " byChannelID:" + struAudioStatus.byChannelID + " byConnectStatus:" + struAudioStatus.byConnectStatus);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetAudioInStatus failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //音频数据导出
    private boolean USB_DownloadAudioData(int lUserID)
    {
        String sFileName = new String("/mnt/sdcard/AudioData.mp3");
        m_lAudioDataHandle = JavaInterface.getInstance().USB_DownloadAudioData(lUserID, sFileName);
        if (m_lAudioDataHandle > 0)
        {
            Log.i("[USBDemo]","USB_DownloadAudioData Success! Handle:" + m_lAudioDataHandle +
                    " FileName:" + sFileName);
            return true;
        }
        else
        {
            Log.e("[USBDemo]","USB_DownloadAudioData failed! error:" + HCUSBSDK.getInstance().USB_GetLastError() +
                    " FileName:" + sFileName);
            return false;
        }
    }


}
