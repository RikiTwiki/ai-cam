package com.usbdemo;

import android.util.Log;

import com.hcusbsdk.Interface.JavaInterface;
import com.hcusbsdk.Interface.USB_ACTIVATE_CARD_RES;
import com.hcusbsdk.Interface.USB_CARD_ISSUE_VERSION;
import com.hcusbsdk.Interface.USB_CARD_PROTO;
import com.hcusbsdk.Interface.USB_CERTIFICATE_INFO;
import com.hcusbsdk.Interface.USB_M1_BLOCK_ADDR;
import com.hcusbsdk.Interface.USB_M1_PWD_VERIFY_INFO;
import com.hcusbsdk.Interface.USB_WAIT_SECOND;
import com.hcusbsdk.Interface.USB_M1_BLOCK_DATA;
import com.hcusbsdk.Interface.USB_M1_BLOCK_WRITE_DATA;
import com.hcusbsdk.Interface.USB_M1_MODIFY_SCB;
import com.hcusbsdk.Interface.USB_M1_SECTION_ENCRYPT;
import com.hcusbsdk.Interface.USB_M1_SECTION_ENCRYPT_RES;
import com.hcusbsdk.Interface.USB_BEEP_AND_FLICKER;
import com.hcusbsdk.jna.HCUSBSDKByJNA;

public class Acs {

    public boolean Config(int lUserID, long dwCommand)
    {
        boolean bRet = false;
        switch ((int)dwCommand)
        {
            case JavaInterface.USB_SET_BEEP_AND_FLICKER: //蜂鸣器及显示灯控制
                bRet = USB_SetBeepAndFlicker(lUserID);
                break;
            case JavaInterface.USB_GET_CARD_ISSUE_VERSION: //获取发卡器固件版本
                bRet = USB_GetCardIssueVersion(lUserID);
                break;
            case JavaInterface.USB_SET_CARD_PROTO: //设置卡协议
                bRet = USB_SetCardProto(lUserID);
                break;
            case JavaInterface.USB_GET_ACTIVATE_CARD: //激活卡
                bRet = USB_GetActivateCard(lUserID);
                break;
            case JavaInterface.USB_CTRL_STOP_CARD_OPER: //停止卡操作
                bRet = USB_StopCardOperation(lUserID);
                break;
            case JavaInterface.USB_GET_CERTIFICATE_INFO: //获取身份证信息
                bRet = USB_GetCertificateInfo(lUserID);
                break;
            case JavaInterface.USB_SET_M1_PWD_VERIFY:  //设置扇区带密码验证参数
                bRet = USB_SetM1PwdVerify(lUserID);
                break;
            case JavaInterface.USB_GET_M1_READ_BLOCK:  //读卡指定块数据
                bRet = USB_GetReadBlock(lUserID);
                break;
            case JavaInterface.USB_SET_M1_WRITE_BLOCK:  //写卡指定块数据
                bRet = USB_SetWriteBlock(lUserID);
                break;
            case JavaInterface.USB_SET_M1_MODIFY_SCB:  //指定扇区控制块数据修改
                bRet = USB_SetModifyScb(lUserID);
                break;
            case JavaInterface.USB_SET_M1_SECTION_ENCRYPT:  //设置指定扇区加密信息
                bRet = USB_SetSectionEncrypt(lUserID);
                break;
            default:
                Log.i("[USBDemo]", "Config No support! ");
                break;
        }
        return bRet;
    }

    //蜂鸣器及显示灯控制
    private boolean USB_SetBeepAndFlicker(int lUserID)
    {
        USB_BEEP_AND_FLICKER struBeepAndFlicker = new USB_BEEP_AND_FLICKER();
        struBeepAndFlicker.byBeepType = 3; // 蜂鸣类型 0无效，1连续，2慢鸣，3快鸣，4停止
        struBeepAndFlicker.byBeepCount = 2; // 鸣叫次数, （只对慢鸣、快鸣有效，且不能为0）
        struBeepAndFlicker.byFlickerType = 2;  // 闪烁类型 0无效，1连续，2错误，3正确，4停止
        struBeepAndFlicker.byFlickerCount = 1; // 闪烁次数（只对错误、正确有效，且不能为0）

        if(JavaInterface.getInstance().USB_SetBeepAndFlicker(lUserID, struBeepAndFlicker))
        {
            //设置成功
            Log.i("[USBDemo]", "USB_SetBeepAndFlicker Success!" +
                    " byBeepType:" + struBeepAndFlicker.byBeepType +
                    " byBeepCount:" + struBeepAndFlicker.byBeepCount +
                    " byFlickerType:" + struBeepAndFlicker.byFlickerType +
                    " byFlickerCount:" + struBeepAndFlicker.byFlickerCount);
            return true;
        }
        else
        {
            Log.e("[USBDemo]", "USB_SetBeepAndFlicker failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byBeepType:" + struBeepAndFlicker.byBeepType +
                    " byBeepCount:" + struBeepAndFlicker.byBeepCount +
                    " byFlickerType:" + struBeepAndFlicker.byFlickerType +
                    " byFlickerCount:" + struBeepAndFlicker.byFlickerCount);
            return false;
        }
    }

    //获取发卡器固件版本
    private boolean USB_GetCardIssueVersion(int lUserID)
    {
        USB_CARD_ISSUE_VERSION struCardIssueVersion = new USB_CARD_ISSUE_VERSION();

        if (JavaInterface.getInstance().USB_GetCardIssueVersion(lUserID, struCardIssueVersion))
        {
            //获取成功
            Log.i("[USBDemo]", "USB_GetCardIssueVersion Success!" +
                    " szDeviceName:" + struCardIssueVersion.szDeviceName +
                    " szSerialNumber:" + struCardIssueVersion.szSerialNumber +
                    " dwSoftwareVersion:" + struCardIssueVersion.dwSoftwareVersion +
                    " wYear:" + struCardIssueVersion.wYear +
                    " byMonth:" + struCardIssueVersion.byMonth +
                    " byDay:" + struCardIssueVersion.byDay +
                    " byLanguage:" + struCardIssueVersion.byLanguage);
            return true;
        } else {
            //获取失败
            Log.e("[USBDemo]", "USB_GetCardIssueVersion failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " szDeviceName:" + struCardIssueVersion.szDeviceName +
                    " szSerialNumber:" + struCardIssueVersion.szSerialNumber +
                    " dwSoftwareVersion:" + struCardIssueVersion.dwSoftwareVersion +
                    " wYear:" + struCardIssueVersion.wYear +
                    " byMonth:" + struCardIssueVersion.byMonth +
                    " byDay:" + struCardIssueVersion.byDay +
                    " byLanguage:" + struCardIssueVersion.byLanguage);
            return false;
        }
    }

    //设置卡协议
    private boolean USB_SetCardProto(int lUserID)
    {
        USB_CARD_PROTO struCardProto = new USB_CARD_PROTO();
        struCardProto.wProto = 1;  //卡协议类型（0-TypeA,1-TypeB,2-typeAB,3-125Khz,255所有）

        if (JavaInterface.getInstance().USB_SetCardProto(lUserID, struCardProto))
        {
            //设置成功
            Log.i("[USBDemo]", "USB_SetCardProto Success!" +
                    " byProto:" + struCardProto.wProto);
            return true;
        } else {
            //设置失败
            Log.e("[USBDemo]", "USB_SetCardProto failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byProto:" + struCardProto.wProto);
            return false;
        }
    }

    //激活卡
    private boolean USB_GetActivateCard(int lUserID)
    {
        //输入参数
        USB_WAIT_SECOND struWaitSecond = new USB_WAIT_SECOND();
        struWaitSecond.byWait = 0;  // 1Byte操作等待时间（0-一直执行直到有卡响应，其他对应1S单位）

        //输出参数
        USB_ACTIVATE_CARD_RES struActivateCardRes = new USB_ACTIVATE_CARD_RES();

        if (JavaInterface.getInstance().USB_GetActivateCard(lUserID, struWaitSecond, struActivateCardRes))
        {
            //获取成功
            Log.i("[USBDemo]", "USB_GetActivateCard Success!" +
                    " byWait:" + struWaitSecond.byWait +
                    " byCardType:" + struActivateCardRes.byCardType +
                    " bySerialLen:" + struActivateCardRes.bySerialLen +
                    " bySerial1:" + Integer.toHexString(struActivateCardRes.dwSerial[0]) +
                    " bySerial2:" + Integer.toHexString(struActivateCardRes.dwSerial[1]) +
                    " bySerial3:" + Integer.toHexString(struActivateCardRes.dwSerial[2]) +
                    " bySerial4:" + Integer.toHexString(struActivateCardRes.dwSerial[3]) +
                    " bySerial5:" + Integer.toHexString(struActivateCardRes.dwSerial[4]) +
                    " bySerial6:" + Integer.toHexString(struActivateCardRes.dwSerial[5]) +
                    " bySerial7:" + Integer.toHexString(struActivateCardRes.dwSerial[6]) +
                    " bySerial8:" + Integer.toHexString(struActivateCardRes.dwSerial[7]) +
                    " bySerial9:" + Integer.toHexString(struActivateCardRes.dwSerial[8]) +
                    " bySerial10:" + Integer.toHexString(struActivateCardRes.dwSerial[9]) +
                    " bySelectVerifyLen:" + struActivateCardRes.bySelectVerifyLen +
                    " bySelectVerify[0]:" + struActivateCardRes.bySelectVerify[0] +
                    " bySelectVerify[1]:" + struActivateCardRes.bySelectVerify[1] +
                    " bySelectVerify[2]:" + struActivateCardRes.bySelectVerify[2]);
            return true;
        } else {
            //获取失败
            Log.e("[USBDemo]", "USB_GetActivateCard failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byWait:" + struWaitSecond.byWait +
                    " byCardType:" + struActivateCardRes.byCardType +
                    " bySerial1:" + Integer.toHexString(struActivateCardRes.dwSerial[0]) +
                    " bySerial2:" + Integer.toHexString(struActivateCardRes.dwSerial[1]) +
                    " bySerial3:" + Integer.toHexString(struActivateCardRes.dwSerial[2]) +
                    " bySerial4:" + Integer.toHexString(struActivateCardRes.dwSerial[3]) +
                    " bySerial5:" + Integer.toHexString(struActivateCardRes.dwSerial[4]) +
                    " bySerial6:" + Integer.toHexString(struActivateCardRes.dwSerial[5]) +
                    " bySerial7:" + Integer.toHexString(struActivateCardRes.dwSerial[6]) +
                    " bySerial8:" + Integer.toHexString(struActivateCardRes.dwSerial[7]) +
                    " bySerial9:" + Integer.toHexString(struActivateCardRes.dwSerial[8]) +
                    " bySerial10:" + Integer.toHexString(struActivateCardRes.dwSerial[9]) +
                    " bySelectVerifyLen:" + struActivateCardRes.bySelectVerifyLen +
                    " bySelectVerify[0]:" + struActivateCardRes.bySelectVerify[0] +
                    " bySelectVerify[1]:" + struActivateCardRes.bySelectVerify[1] +
                    " bySelectVerify[2]:" + struActivateCardRes.bySelectVerify[2]);
            return false;
        }
    }

    //停止卡操作
    public boolean USB_StopCardOperation(int lUserID)
    {
        if (JavaInterface.getInstance().USB_StopCardOperation(lUserID))
        {
            Log.i("[USBDemo]","USB_StopCardOperation Success!");
            return true;
        }
        else
        {
            Log.e("[USBDemo]","USB_StopCardOperation failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //获取身份证信息
    private boolean USB_GetCertificateInfo(int lUserID)
    {
        //输出参数
        USB_CERTIFICATE_INFO struCertificateInfo = new USB_CERTIFICATE_INFO();

        if (JavaInterface.getInstance().USB_GetCertificateInfo(lUserID, struCertificateInfo))
        {
            //获取成功
            Log.i("[USBDemo]", "USB_GetCertificateInfo Success!" +
                    " wWordInfoSize:" + struCertificateInfo.wWordInfoSize +
                    " wPicInfoSize:" + struCertificateInfo.wPicInfoSize +
                    " wFingerPrintInfoSize:" + struCertificateInfo.wFingerPrintInfoSize +
                    " byCertificateType:" + struCertificateInfo.byCertificateType +
                    " byWordInfo:" + new String(struCertificateInfo.byWordInfo));
            return true;
        } else {
            //获取失败
            Log.e("[USBDemo]", "USB_GetCertificateInfo failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " wWordInfoSize:" + struCertificateInfo.wWordInfoSize +
                    " wPicInfoSize:" + struCertificateInfo.wPicInfoSize +
                    " wFingerPrintInfoSize:" + struCertificateInfo.wFingerPrintInfoSize +
                    " byCertificateType:" + struCertificateInfo.byCertificateType +
                    " byWordInfo:" + new String(struCertificateInfo.byWordInfo));
            return false;
        }
    }

    //设置扇区带密码验证参数
    private boolean USB_SetM1PwdVerify(int lUserID)
    {
        USB_M1_PWD_VERIFY_INFO struPwdVerifyInfo = new USB_M1_PWD_VERIFY_INFO();
        struPwdVerifyInfo.byPasswordType = 1; //密码类别（0-KeyA, 1-KeyB）
        struPwdVerifyInfo.bySectionNum = 2; //要验证密码的扇区号
        struPwdVerifyInfo.byPassword[0] = (byte)0xff;  //6Byte密码
        struPwdVerifyInfo.byPassword[1] = (byte)0xff;
        struPwdVerifyInfo.byPassword[2] = (byte)0xff;
        struPwdVerifyInfo.byPassword[3] = (byte)0xff;
        struPwdVerifyInfo.byPassword[4] = (byte)0xff;
        struPwdVerifyInfo.byPassword[5] = (byte)0xff;

        if(JavaInterface.getInstance().USB_SetM1PwdVerify(lUserID, struPwdVerifyInfo))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetM1PwdVerify Success! " +
                    " byPasswordType:" + struPwdVerifyInfo.byPasswordType +
                    " bySectionNum:" + struPwdVerifyInfo.bySectionNum +
                    " byPassword:" + new String(struPwdVerifyInfo.byPassword));
            return true;
        }
        else
        {
            Log.e("[USBDemo]", "USB_SetM1PwdVerify failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byPasswordType:" + struPwdVerifyInfo.byPasswordType +
                    " bySectionNum:" + struPwdVerifyInfo.bySectionNum +
                    " byPassword:" + new String(struPwdVerifyInfo.byPassword));
            return false;
        }
    }

    //读卡指定块数据
    private boolean USB_GetReadBlock(int lUserID)
    {
        USB_M1_BLOCK_ADDR struBlockAddr = new USB_M1_BLOCK_ADDR();
        struBlockAddr.wAddr = 8;  //2Byte块地址

        USB_M1_BLOCK_DATA struBlockData = new USB_M1_BLOCK_DATA();

        if(JavaInterface.getInstance().USB_GetReadBlock(lUserID, struBlockAddr, struBlockData))
        {
            //获取成功
            Log.i("[USBDemo]", "USB_GetReadBlock Success!" +
                    " byData:" + new String(struBlockData.byData));
            return true;
        }
        else
        {
            //获取失败
            Log.e("[USBDemo]", "USB_GetReadBlock failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byData:" + new String(struBlockData.byData));
            return false;
        }
    }

    //写卡指定块数据
    private boolean USB_SetWriteBlock(int lUserID)
    {
        USB_M1_BLOCK_WRITE_DATA struBlockWriteData = new USB_M1_BLOCK_WRITE_DATA();
        struBlockWriteData.wAddr = 8;  //2Byte块地址
        struBlockWriteData.byDataLen = 3;  //数据长度（0-16）
        struBlockWriteData.byData[0] = '1';
        struBlockWriteData.byData[1] = '2';
        struBlockWriteData.byData[2] = '3';

        if(JavaInterface.getInstance().USB_SetWriteBlock(lUserID, struBlockWriteData))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetWriteBlock Success! " +
                    " wAddr:" + struBlockWriteData.wAddr +
                    " byDataLen:" + struBlockWriteData.byDataLen +
                    " byData:" + new String(struBlockWriteData.byData));
            return true;
        }
        else
        {
            Log.e("[USBDemo]", "USB_SetWriteBlock failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " wAddr:" + struBlockWriteData.wAddr +
                    " byDataLen:" + struBlockWriteData.byDataLen +
                    " byData:" + new String(struBlockWriteData.byData));
            return false;
        }
    }

    //指定扇区控制块数据修改
    private boolean USB_SetModifyScb(int lUserID)
    {
        USB_M1_MODIFY_SCB struModifyScb = new USB_M1_MODIFY_SCB();
        struModifyScb.bySectionNum = 2; //1Byte扇区号
        struModifyScb.byPasswordA[0] = (byte)0x00;    //6Byte 密码A
        struModifyScb.byPasswordA[1] = (byte)0x00;
        struModifyScb.byPasswordA[2] = (byte)0x00;
        struModifyScb.byPasswordA[3] = (byte)0x00;
        struModifyScb.byPasswordA[4] = (byte)0x00;
        struModifyScb.byPasswordA[5] = (byte)0x00;
        struModifyScb.byCtrlBits[0] = (byte)0x00;    //4Byte控制位
        struModifyScb.byCtrlBits[1] = (byte)0x00;
        struModifyScb.byCtrlBits[2] = (byte)0x00;
        struModifyScb.byCtrlBits[3] = (byte)0x00;
        struModifyScb.byPasswordB[0] = (byte)0xff;   //6Byte 密码B*/
        struModifyScb.byPasswordB[1] = (byte)0xff;
        struModifyScb.byPasswordB[2] = (byte)0xff;
        struModifyScb.byPasswordB[3] = (byte)0xff;
        struModifyScb.byPasswordB[4] = (byte)0xff;
        struModifyScb.byPasswordB[5] = (byte)0xff;

        if(JavaInterface.getInstance().USB_SetModifyScb(lUserID, struModifyScb))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetModifyScb Success! " +
                    " bySectionNum:" + struModifyScb.bySectionNum +
                    " byPasswordA:" + new String(struModifyScb.byPasswordA) +
                    " byCtrlBits:" + new String(struModifyScb.byCtrlBits) +
                    " byPasswordB:" + new String(struModifyScb.byPasswordB));
            return true;
        }
        else
        {
            Log.e("[USBDemo]", "USB_SetModifyScb failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " bySectionNum:" + struModifyScb.bySectionNum +
                    " byPasswordA:" + new String(struModifyScb.byPasswordA) +
                    " byCtrlBits:" + new String(struModifyScb.byCtrlBits) +
                    " byPasswordB:" + new String(struModifyScb.byPasswordB));
            return false;
        }
    }

    //设置指定扇区加密信息
    private boolean USB_SetSectionEncrypt(int lUserID)
    {
        USB_M1_SECTION_ENCRYPT struSectionEncrypt = new USB_M1_SECTION_ENCRYPT();
        struSectionEncrypt.bySectionID = 15;  //扇区ID
        struSectionEncrypt.byKeyType = 1;  //验证密钥类型，0-海康密钥，1-其它正常密钥
        struSectionEncrypt.byKeyAContent[0] = (byte)0xff;   //新密钥A具体参数，新密钥类型为1时有效
        struSectionEncrypt.byKeyAContent[1] = (byte)0xff;
        struSectionEncrypt.byKeyAContent[2] = (byte)0xff;
        struSectionEncrypt.byKeyAContent[3] = (byte)0xff;
        struSectionEncrypt.byKeyAContent[4] = (byte)0xff;
        struSectionEncrypt.byKeyAContent[5] = (byte)0xff;
        struSectionEncrypt.byNewKeyType = 0;    //新密钥类型，0-海康密钥，1-其它正常密钥
        struSectionEncrypt.byCtrlBits[0] = (byte)0xff;   //控制位，新密钥类型为1时有效
        struSectionEncrypt.byCtrlBits[1] = (byte)0x07;
        struSectionEncrypt.byCtrlBits[2] = (byte)0x80;
        struSectionEncrypt.byCtrlBits[3] = (byte)0x69;

        USB_M1_SECTION_ENCRYPT_RES struSectionEncryptRes = new USB_M1_SECTION_ENCRYPT_RES();

        if(JavaInterface.getInstance().USB_SetSectionEncrypt(lUserID, struSectionEncrypt, struSectionEncryptRes))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetSectionEncrypt Success! " +
                    " bySectionID:" + struSectionEncrypt.bySectionID +
                    " byKeyType:" + struSectionEncrypt.byKeyType +
                    " byNewKeyType:" + struSectionEncrypt.byNewKeyType +
                    " byKeyAContent:" + new String(struSectionEncrypt.byKeyAContent) +
                    " byNewKeyAContent:" + new String(struSectionEncrypt.byNewKeyAContent) +
                    " byCtrlBits:" + new String(struSectionEncrypt.byCtrlBits) +
                    " byNewKeyBContent:" + new String(struSectionEncrypt.byNewKeyBContent) +
                    " byStatus:" + struSectionEncryptRes.byStatus); //成功返回0，失败时返回1-代表验证密钥失败，2-设置新密钥失败
            return true;
        }
        else
        {
            Log.e("[USBDemo]", "USB_SetSectionEncrypt failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " bySectionID:" + struSectionEncrypt.bySectionID +
                    " byKeyType:" + struSectionEncrypt.byKeyType +
                    " byNewKeyType:" + struSectionEncrypt.byNewKeyType +
                    " byKeyAContent:" + new String(struSectionEncrypt.byKeyAContent) +
                    " byNewKeyAContent:" + new String(struSectionEncrypt.byNewKeyAContent) +
                    " byCtrlBits:" + new String(struSectionEncrypt.byCtrlBits) +
                    " byNewKeyBContent:" + new String(struSectionEncrypt.byNewKeyBContent));
            return false;
        }
    }


}
