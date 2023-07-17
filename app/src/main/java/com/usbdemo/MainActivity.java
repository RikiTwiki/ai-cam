package com.usbdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.hcusbsdk.Interface.JavaInterface;
import com.hcusbsdk.Interface.USB_DEVICE_INFO;
import com.hcusbsdk.Interface.USB_DEVICE_REG_RES;
import com.hcusbsdk.Interface.USB_ROI_MAX_TEMPERATURE_SEARCH;
import com.hcusbsdk.Interface.USB_ROI_MAX_TEMPERATURE_SEARCH_RESULT;
import com.hcusbsdk.Interface.USB_USER_LOGIN_INFO;
import com.hcusbsdk.jna.HCUSBSDK;
import com.hcusbsdk.jna.HCUSBSDKByJNA;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private USB_DEVICE_INFO[] m_struDevInfoList = new USB_DEVICE_INFO[JavaInterface.MAX_DEVICE_NUM];    //设备信息列表
    private int m_dwDevCount = 0; //设备个数
    private boolean m_bInit = false; //是否初始化
    private int m_dwLoginDevIndex = 0; //默认登录第一个设备
    private int m_dwCurUserID = JavaInterface.USB_INVALID_USER_ID; //当前登录的设备句柄


    public final String URL = "https://frontrobot.sanarip.org/";

    private boolean m_bLogin = false; //是否登录设备
    private boolean m_bPreview = false; //是否开启预览
    private boolean m_bUpgrade = false; //是否在升级设备

    //分辨率
    private static final String[] m_arrResolutionType = { "Выберите разрешение предварительного просмотра", "240 * 320 (тепловой)", "640 * 480 (фронтальный)", "640 * 360 (сквозной)"};

    //前端配置选项
    private static final String[] m_arrCameraType = { "Выбрать функцию конфигурации внешнего устройства", "Установить параметры видео" };
    private long m_dwCmdCamera = 0; //当前选择的前端配置功能

    //门禁配置选项
    private static final String[] m_arrAcsType = {"Выбрать функцию конфигурации контроля доступа", "Управление зуммером и светом дисплея", "Получить версию прошивки эмитента карты", "Установить протокол карты", "Активировать карту", "Остановить работу карты", "Получить идентификационную информацию",
            "Задать сектор с параметрами аутентификации пароля", "Считать данные блока, указанного картой", "Записать данные блока, указанного картой", "Задать модификацию данных блока управления сектором", "Задать информацию шифрования указанного сектора"};
    private long m_dwCmdAcs = 0; //当前选择的门禁配置功能

    //传显配置选项
    private static final String[] m_arrTransferType = { "Выбор функции конфигурации трансфлектора", "Шифрование устройства", "Экспорт файла журнала", "Установка WDR изображения", "Получение состояния аудиовхода", "Экспорт аудиоданных"};
    private long m_dwCmdTransfer = 0; //当前选择的传显配置功能

    //热成像配置选项
    private static final String[] m_arrThermalType = {"Выбрать функцию конфигурации тепловизора", "Установить параметры видео", "Получить информацию об устройстве", "Перезагрузка устройства", "Восстановить значения по умолчанию",
            "Получить параметры обслуживания оборудования", "Установить параметры обслуживания оборудования", "Получить системное местное время", "Установить системное местное время", "Получить параметры яркости изображения", "Установить параметры яркости изображения",
            "Получить параметры контрастности изображения", "Установить параметры контрастности изображения", "Коррекция фона в один клик", "Экспорт диагностической информации", "Коррекция вручную в один клик", "Получить параметры улучшения изображения",
            "Установить параметры улучшения изображения", "Получить параметры настройки видео", "Установить параметры настройки видео", "Получить основные параметры измерения температуры", "Установить основные параметры измерения температуры",
            "Получить режим измерения температуры", "Установить режим измерения температуры", "Получить параметры правила измерения температуры", "Установить параметры правила измерения температуры", "Получить информацию о версии алгоритма, связанного с тепловидением",
            "Получить параметры потока термографического кода", "Установить параметры потока термографического кода", "Получить параметры термометрической коррекции", "Установить параметры термометрической коррекции", "Получить параметры черного тела", "Установить параметры черного тела",
            "Получить параметры компенсации температуры тела", "Установить параметры компенсации температуры тела", "Получить тепловую карту", "Запрос информации о максимальной температуре региона", "Получить параметры температуры всего экрана", "Установить параметры температуры всего экрана",
            "Экспорт файла калибровки температуры", "Импорт файла калибровки температуры", "Получить правила экспертной температуры", "Установить правила экспертной температуры", "Получить параметры калибровки экспертной температуры",
            "Установить параметры калибровки экспертной температуры", "Запустить калибровку экспертной температуры", "Получить параметры повышения температуры", "Установить параметры повышения температуры", "Установить параметры калибровки экспертной температуры", "Запустить калибровку экспертной температуры",
            "Получить параметры повышения температуры", "Установить параметры повышения температуры", "Получить параметры коррекции температуры окружающей среды", "Установить параметры коррекции температуры окружающей среды"};
    private long m_dwCmdThermal = 0; //当前选择的热成像配置功能

    private boolean stat_temp = true;

    //前端配置类对象
    private Camera m_objCamera = new Camera();
    //门禁配置类对象
    private Acs m_objAcs = new Acs();
    //传显配置类对象
    private Transfer m_objTransfer = new Transfer();
    //热成像配置类对象
    private Thermal m_objThermal = new Thermal();

    //预览类对象
    private PreviewActivity m_objPreview = null;
    //升级类对象
    private Upgrade m_objUpgrade = null;


    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";//可自定义
    private UsbDevice m_usbDevice = null;
    private UsbDeviceConnection m_DevConnect = null;

    private SurfaceView m_pSurfaceView = null;
    private SurfaceHolder m_pHolder = null;

    private boolean m_bWebViewOpened = false;

    // Add a Runnable that calls the 'temp' method every second while preview is active
    final Handler handler = new Handler(Looper.getMainLooper());
    WebView myWebView = (WebView) findViewById(R.id.webview);

    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建MAX_DEVICE_NUM个设备信息对象
        for (int i = 0; i < JavaInterface.MAX_DEVICE_NUM; i++) {
            m_struDevInfoList[i] = new USB_DEVICE_INFO();
        }

        //初始化界面控件
        InitControl();


        final Button btnInit = findViewById(R.id.btn_init);

        btnInit.post(new Runnable() {
            @Override
            public void run() {
                btnInit.performClick();
            }
        });

        m_objPreview = new PreviewActivity(this);
        m_objUpgrade = new Upgrade(this);

        //读写文件权限动态申请  //高版本SDK下AndroidManifest.xml中配置的读写权限不起作用
        CheckPermission();
        //网络权限动态申请     //高版本SDK下AndroidManifest.xml中配置的网络权限不起作用
        CheckNetworkPermission();
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
//        checkForUpdates();



        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    //初始化界面控件
    private boolean InitControl()
    {
        //Button监听
        Button btn_init = (Button) findViewById(R.id.btn_init);
        btn_init.setOnClickListener(new ButtonClickListener());

//        Button btn_enum = (Button) findViewById(R.id.btn_enum);
//        btn_enum.setOnClickListener(new ButtonClickListener());
//
//        Button btn_login = (Button) findViewById(R.id.btn_login);
//        btn_login.setOnClickListener(new ButtonClickListener());
//
//        Button btn_startPreview = (Button) findViewById(R.id.btn_startPreview);
//        btn_startPreview.setOnClickListener(new ButtonClickListener());

//        Button btn_upgrade = (Button) findViewById(R.id.btn_upgrade);
//        btn_upgrade.setOnClickListener(new ButtonClickListener());
//
//        Button btn_upgradeState = (Button) findViewById(R.id.btn_upgradeState);
//        btn_upgradeState.setOnClickListener(new ButtonClickListener());
//
//        Button btn_config_camera = (Button) findViewById(R.id.btn_config_camera);
//        btn_config_camera.setOnClickListener(new ButtonClickListener());
//
//        Button btn_config_acs = (Button) findViewById(R.id.btn_config_acs);
//        btn_config_acs.setOnClickListener(new ButtonClickListener());
//
//        Button btn_config_transfer = (Button) findViewById(R.id.btn_config_transfer);
//        btn_config_transfer.setOnClickListener(new ButtonClickListener());

//        Button btn_config_thermal = (Button) findViewById(R.id.btn_config_thermal);
//        btn_config_thermal.setOnClickListener(new ButtonClickListener());

        //初始化分辨率下拉列表
//        Spinner spinResolutionType = (Spinner) findViewById(R.id.spinner_preview);
        ArrayAdapter<String> adaResolutionType = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, m_arrResolutionType);
        adaResolutionType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinResolutionType.setAdapter(adaResolutionType);
//        spinResolutionType.setOnItemSelectedListener(new OnItemSelectedListenerConfig());

        //初始化前端配置下拉列表
//        Spinner spinCameraType = (Spinner) findViewById(R.id.spinner_camera);
        ArrayAdapter<String> adaCameraType = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, m_arrCameraType);
        adaCameraType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinCameraType.setAdapter(adaCameraType);
//        spinCameraType.setOnItemSelectedListener(new OnItemSelectedListenerConfig());

        //初始化门禁配置下拉列表
//        Spinner spinAcsType = (Spinner) findViewById(R.id.spinner_acs);
        ArrayAdapter<String> adaAcsType = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, m_arrAcsType);
        adaAcsType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinAcsType.setAdapter(adaAcsType);
//        spinAcsType.setOnItemSelectedListener(new OnItemSelectedListenerConfig());

        //初始化传显配置下拉列表
//        Spinner spinTransferType = (Spinner) findViewById(R.id.spinner_transfer);
        ArrayAdapter<String> adaTransferType = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, m_arrTransferType);
        adaTransferType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinTransferType.setAdapter(adaTransferType);
//        spinTransferType.setOnItemSelectedListener(new OnItemSelectedListenerConfig());

        //初始化热成像配置下拉列表
//        Spinner spinThermalType = (Spinner) findViewById(R.id.spinner_thermal);
        ArrayAdapter<String> adaThermalType = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, m_arrThermalType);
        adaThermalType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinThermalType.setAdapter(adaThermalType);
//        spinThermalType.setOnItemSelectedListener(new OnItemSelectedListenerConfig());

        //surfaceview
        m_pSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        m_pHolder = m_pSurfaceView.getHolder(); //得到surfaceView的holder，类似于surfaceView的控制器
        //把输送给surfaceView的视频画面，直接显示到屏幕上,不要维持它自身的缓冲区
        m_pHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        m_pHolder.addCallback(this);
        return true;
    }

    //初始化相关
    @SuppressLint("SdCardPath")
    private boolean InitUsbSdk() {
        CheckPermission(Manifest.permission.CAMERA);
        //初始化USBSDK
        if (JavaInterface.getInstance().USB_Init()) {
            Log.i("[USBDemo]", "USB_Init Success!");
        } else {
            Log.e("[USBDemo]", "USB_Init Failed!");
            Toast.makeText(this, "USB_Init Failed!", Toast.LENGTH_SHORT).show();
            return false;
        }

        //获取USBSDK版本
        String version = String.format("%08x", JavaInterface.getInstance().USB_GetSDKVersion());
        Log.i("[USBDemo]", "USB_GetSDKVersion :" + version);
        Toast.makeText(this, "USB_GetSDKVersion :" + version, Toast.LENGTH_SHORT).show();

        //开启USBSDK日志，参数说明见使用手册接口说明
        if (JavaInterface.getInstance().USB_SetLogToFile(JavaInterface.INFO_LEVEL, new String("/mnt/sdcard/sdklog/"), 1)) {
            Log.i("[USBDemo]", "USB_SetLogToFile Success!");
        } else {
            Log.e("[USBDemo]", "USB_SetLogToFile failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "USB_SetLogToFile failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }

        m_bInit = true;
        return true;
    }

    //清理USBSDK资源
    private void CleanupUsbSdk() {
        if (JavaInterface.getInstance().USB_Cleanup()) {
            Log.i("[USBDemo]", "USB_Cleanup Success!");
        } else {
            Log.e("[USBDemo]", "USB_Cleanup Failed!");
            Toast.makeText(this, "USB_Init Failed!", Toast.LENGTH_SHORT).show();
        }
        m_bInit = false;
    }

    //获取设备信息
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean GetDeviceInfo() {
        //获取设备个数，第一次调用会申请设备权限，获取FD失败，用户确认权限后重新枚举，才能获取FD
        m_dwDevCount = JavaInterface.getInstance().USB_GetDeviceCount(this);
        m_dwDevCount = JavaInterface.getInstance().USB_GetDeviceCount(this);
        if (m_dwDevCount > 0) {
            Log.i("[USBDemo]", "USB_GetDeviceCount Device count is :" + m_dwDevCount);
            Toast.makeText(this, "USB_GetDeviceCount Device count is :" + m_dwDevCount, Toast.LENGTH_SHORT).show();
        }
        else if(m_dwDevCount == 0)
        {
            Log.i("[USBDemo]", "USB_GetDeviceCount Device count is :" + m_dwDevCount);
            Toast.makeText(this, "USB_GetDeviceCount Device count is :" + m_dwDevCount, Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            Log.e("[USBDemo]", "USB_GetDeviceCount failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "USB_GetDeviceCount failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }

        //获取设备信息
        if (JavaInterface.getInstance().USB_EnumDevices(m_dwDevCount, m_struDevInfoList)) {
            //打印设备信息
            for (int i = 0; i < m_dwDevCount; i++) {
                Log.i("[USBDemo]", "USB_EnumDevices Device info is dwIndex:" + m_struDevInfoList[i].dwIndex +
                        " dwVID:" + m_struDevInfoList[i].dwVID +
                        " dwPID:" + m_struDevInfoList[i].dwPID +
                        " szManufacturer:" + m_struDevInfoList[i].szManufacturer +
                        " szDeviceName:" + m_struDevInfoList[i].szDeviceName +
                        " szSerialNumber:" + m_struDevInfoList[i].szSerialNumber +
                        " byHaveAudio:" + m_struDevInfoList[i].byHaveAudio);
            }
        } else {
            Log.e("[USBDemo]", "USB_EnumDevices failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "USB_EnumDevices failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //系统没有root过，通过demo层获取设备Fd
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean LoginDeviceWithFd() {

        //GetDevFd();  //获取设备描述符fd
        USB_USER_LOGIN_INFO struUserLoginInfo = new USB_USER_LOGIN_INFO();
        struUserLoginInfo.dwTimeout = 5000;
        struUserLoginInfo.dwDevIndex = m_struDevInfoList[m_dwLoginDevIndex].dwIndex;
        struUserLoginInfo.dwVID = m_struDevInfoList[m_dwLoginDevIndex].dwVID;
        struUserLoginInfo.dwPID = m_struDevInfoList[m_dwLoginDevIndex].dwPID;
        struUserLoginInfo.dwFd = m_struDevInfoList[m_dwLoginDevIndex].dwFd;
        struUserLoginInfo.byLoginMode = 1;
        struUserLoginInfo.szUserName = "admin"; //如果是门禁设备，需要输入用户名和密码
        struUserLoginInfo.szPassword = "12345"; //如果是门禁设备，需要输入用户名和密码

        USB_DEVICE_REG_RES struDeviceRegRes = new USB_DEVICE_REG_RES();

        //获取设备信息
        m_dwCurUserID = JavaInterface.getInstance().USB_Login(struUserLoginInfo, struDeviceRegRes);
        if (m_dwCurUserID != JavaInterface.USB_INVALID_USER_ID) {
            //登录成功
            Log.i("[USBDemo]", "LoginDeviceWithFd Success! iUserID:" + m_dwCurUserID +
                    " dwDevIndex:" + struUserLoginInfo.dwDevIndex +
                    " dwVID:" + struUserLoginInfo.dwVID +
                    " dwPID:" + struUserLoginInfo.dwPID +
                    " dwFd:" + struUserLoginInfo.dwFd);
            Toast.makeText(this, "LoginDeviceWithFd Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("[USBDemo]", "LoginDeviceWithFd failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwDevIndex:" + struUserLoginInfo.dwDevIndex +
                    " dwVID:" + struUserLoginInfo.dwVID +
                    " dwPID:" + struUserLoginInfo.dwPID +
                    " dwFd:" + struUserLoginInfo.dwFd);
            Toast.makeText(this, "LoginDeviceWithFd failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //注销设备
    private boolean LogoutDevice() {
        if (JavaInterface.getInstance().USB_Logout(m_dwCurUserID)) {
            //登录成功
            Log.i("[USBDemo]", "USB_Logout Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "USB_Logout Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            m_dwCurUserID = JavaInterface.USB_INVALID_USER_ID;
            return true;
        } else {
            Log.e("[USBDemo]", "USB_Logout failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "USB_Logout failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //升级设备
    private boolean UpgradeDevice() {
        m_objUpgrade.SetUserID(m_dwCurUserID);

        if (m_objUpgrade.StartUpgrade()) {
            Log.i("[USBDemo]", "StartUpgrade Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "StartUpgrade Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "StartUpgrade failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "StartUpgrade failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //获取升级状态
    private boolean GetUpgradeState() {
        if (m_objUpgrade.GetUpgradeState()) {
            Log.i("[USBDemo]", "GetUpgradeState Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "GetUpgradeState Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "GetUpgradeState failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "GetUpgradeState failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //关闭升级
    private boolean CloseUpgrade() {
        if (m_objUpgrade.StopUpgrade()) {
            Log.i("[USBDemo]", "StopUpgrade Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "StopUpgrade Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "StopUpgrade failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "StopUpgrade failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //开始预览
    private boolean StartPreview() {

        // Check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);  // 1 is your custom request code
        }

        m_objPreview.SetUserID(m_dwCurUserID);//确定预览的设备

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        m_objPreview.SetScreenResolution(metric.widthPixels, metric.heightPixels);

        ImageView myImageView = (ImageView) findViewById(R.id.myImageView);
        myImageView.setImageResource(R.drawable.gts);  // show the cat picture
        myImageView.setVisibility(View.VISIBLE);  // make sure the image is visible

        if (m_objPreview.StartPreview(m_pHolder)) {
            //预览成功
            Log.i("[USBDemo]", "StartPreview Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "StartPreview Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();






            myWebView.getSettings().setJavaScriptEnabled(true);
            myWebView.getSettings().setDomStorageEnabled(true);
            myWebView.getSettings().setUseWideViewPort(true);
            myWebView.getSettings().setLoadWithOverviewMode(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false); // This line will allow audio autoplay
            }
            myWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                }
            });

            CookieManager.getInstance().setAcceptCookie(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true);
            }
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (m_bPreview) { // Assuming m_bPreview indicates if preview is active

                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

                        double temp = temp(m_dwCurUserID);
                        if (temp > 38.2 && !m_bWebViewOpened) {

                            getWindow().getDecorView().setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN);

                            m_bWebViewOpened = true;
                            myImageView.setVisibility(View.GONE);
                            runOnUiThread(() -> {

                                myWebView.setVisibility(View.VISIBLE);
                                myImageView.setVisibility(View.GONE); // hide the cat picture
                                myWebView.loadUrl(URL);
                                myWebView.setWebViewClient(new WebViewClient() {

                                    @Override
                                    public void onPageFinished(WebView view, String url) {
                                        super.onPageFinished(view, url);

                                        view.loadUrl(
                                                "javascript:(function() { " +
                                                        "var elements = document.getElementsByClassName('Home_control__e-xkD container');" +
                                                        "for (var i = 0; i < elements.length; i++) {" +
                                                        "elements[i].click();" +
                                                        "}" +
                                                        "})()"
                                        );
                                    }
                                });

                                getWindow().getDecorView().setSystemUiVisibility(
                                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_FULLSCREEN);

                            });
                            // Create an instance of AudioManager
//                            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                            // Check if the speaker is currently free
//                            if (!audioManager.isMusicActive()) {
//                                Log.e("[USBDemo]", "Speaker is not free");
//                                startListening();
//                            }
                        } else if (temp < 34.2 && m_bWebViewOpened) {
                            m_bWebViewOpened = false;
                            runOnUiThread(() -> {

                                getWindow().getDecorView().setSystemUiVisibility(
                                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_FULLSCREEN);

                                myImageView.setVisibility(View.GONE); // show the cat picture
                            });
                        }
                        handler.postDelayed(this, 5000); // Call every 5 seconds
                    }
                }
            };
            handler.post(runnable);


            return true;
        } else {
            Log.e("[USBDemo]", "StartPreview failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "StartPreview failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }



    //停止预览
    private boolean StopPreview() {
        if (m_objPreview.StopPreview()) {
            //关闭成功
            Log.i("[USBDemo]", "StopPreview Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "StopPreview Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "StopPreview failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "StopPreview failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //前端参数配置
    private boolean CameraConfig(){
        if (m_objCamera.Config(m_dwCurUserID, m_dwCmdCamera))
        {
            //配置成功
            Log.i("[USBDemo]", "CameraConfig Success! m_dwCmdCamera:" + m_dwCmdCamera);
            Toast.makeText(this, "CameraConfig Success! m_dwCmdCamera:" + m_dwCmdCamera, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            //配置失败
            Log.e("[USBDemo]", "CameraConfig failed! m_dwCmdCamera:" + m_dwCmdCamera +
                    " error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "CameraConfig failed! m_dwCmdCamera:" + m_dwCmdCamera + " error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //门禁参数配置
    private boolean AcsConfig(){
        if (m_objAcs.Config(m_dwCurUserID, m_dwCmdAcs))
        {
            //配置成功
            Log.i("[USBDemo]", "AcsConfig Success! m_dwCmdAcs:" + m_dwCmdAcs);
            Toast.makeText(this, "AcsConfig Success! m_dwCmdAcs:" + m_dwCmdAcs, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            //配置失败
            Log.e("[USBDemo]", "AcsConfig failed! m_dwCmdAcs:" + m_dwCmdAcs +
                    " error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "AcsConfig failed! m_dwCmdAcs:" + m_dwCmdAcs + " error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //传显参数配置
    private boolean TransferConfig(){
        if (m_objTransfer.Config(m_dwCurUserID, m_dwCmdTransfer))
        {
            //配置成功
            Log.i("[USBDemo]", "TransferConfig Success! m_dwCmdTransfer:" + m_dwCmdTransfer);
            Toast.makeText(this, "TransferConfig Success! m_dwCmdTransfer:" + m_dwCmdTransfer, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            //配置失败
            Log.e("[USBDemo]", "TransferConfig failed! m_dwCmdTransfer:" + m_dwCmdTransfer +
                    " error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "TransferConfig failed! m_dwCmdTransfer:" + m_dwCmdTransfer + " error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //热成像参数配置
    private boolean ThermalConfig() {
        if (m_objThermal.Config(m_dwCurUserID, m_dwCmdThermal))
        {
            //配置成功
            Log.i("[USBDemo]", "ThermalConfig Success! m_dwCmdThermal:" + m_dwCmdThermal);
            Toast.makeText(this, "ThermalConfig Success! m_dwCmdThermal:" + m_dwCmdThermal, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            //配置失败
            Log.e("[USBDemo]", "ThermalConfig failed! m_dwCmdThermal:" + m_dwCmdThermal +
                    " error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "ThermalConfig failed! m_dwCmdThermal:" + m_dwCmdThermal + " error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //按钮监听函数
    private class ButtonClickListener implements View.OnClickListener {
        @SuppressLint("NewApi")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_init:
                    if (!m_bInit)
                    {
                        //初始化USBSDK
                        InitUsbSdk();
                        GetDeviceInfo();
                        LoginDeviceWithFd();
                        m_bLogin = true;
                        StartPreview();
                        m_bPreview = true;
                        m_dwCmdThermal = JavaInterface.USB_SET_THERMOMETRY_BASIC_PARAM;
                        ThermalConfig();
                        Button btn_init = (Button) findViewById(R.id.btn_init);
                        btn_init.setText("Обратная инициализация");
                    }else
                    {
                        //清理USBSDK资源
                        CleanupUsbSdk();
                        Button btn_init = (Button) findViewById(R.id.btn_init);
                        btn_init.setText("Инициализация");
                    }
                    break;
//                case R.id.btn_enum:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    //枚举设备信息
//                    GetDeviceInfo();
//                    break;
//                case R.id.btn_login:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    if (!m_bLogin)
//                    {
//                        //登录设备
//                        if(LoginDeviceWithFd())
//                        {
//                            m_bLogin = true;
//                            Button btn_login = (Button) findViewById(R.id.btn_login);
//                            btn_login.setText("Аннулирование");
//                        }
//                    }
//                    else
//                    {
//                        //注销设备
//                        if(LogoutDevice())
//                        {
//                            m_bLogin = false;
//                            Button btn_login = (Button) findViewById(R.id.btn_login);
//                            btn_login.setText("Вход в систему");
//                        }
//                    }
//                    break;
//                case R.id.btn_startPreview:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    if (!m_bPreview) {
//                        //开始预览
//                        if(StartPreview())
//                        {
//                            Button btn_startPreview = (Button) findViewById(R.id.btn_startPreview);
//                            btn_startPreview.setText("Остановить предварительный просмотр");
//                            m_bPreview = true;
//                        }
//                    } else {
//                        //关闭预览
//                        if(StopPreview())
//                        {
//                            Button btn_startPreview = (Button) findViewById(R.id.btn_startPreview);
//                            btn_startPreview.setText("Начать предварительный просмотр");
//                            m_bPreview = false;
//                        }
//                    }
//                    break;
//                case R.id.btn_upgrade:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    if (!m_bUpgrade)
//                    {
//                        //升级
//                        if (UpgradeDevice())
//                        {
//                            m_bUpgrade = true;
//                            Button btn_upgrade = (Button) findViewById(R.id.btn_upgrade);
//                            btn_upgrade.setText("Остановить обновление");
//                        }
//                    }
//                    else
//                    {
//                        //停止升级
//                        if (CloseUpgrade())
//                        {
//                            m_bUpgrade = false;
//                            Button btn_upgrade = (Button) findViewById(R.id.btn_upgrade);
//                            btn_upgrade.setText("Начало модернизации");
//                        }
//                    }
//                    break;
//                case R.id.btn_upgradeState:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    //获取升级状态
//                    GetUpgradeState();
//                    break;
//                case R.id.btn_config_camera:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    //前端参数配置
//                    CameraConfig();
//                    break;
//                case R.id.btn_config_acs:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    //门禁参数配置
//                    AcsConfig();
//                    break;
//                case R.id.btn_config_transfer:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    //传显参数配置
//                    TransferConfig();
//                    break;
//                case R.id.btn_config_thermal:
//                    if (!m_bInit) {
//                        Log.i("[USBDemo]", "No Init");
//                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    //热成像参数配置
//                    ThermalConfig();
//                    break;
                default:
                    break;
            }
        }
    }

    //Spinner事件
    private class OnItemSelectedListenerConfig implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//            if (parent.getId() == R.id.spinner_preview) {
//                Spinner spin = (Spinner)findViewById(R.id.spinner_preview);
//                long iIndex = spin.getSelectedItemId();
//                switch ((int)iIndex)
//                {
//                    case 0:
//                        break;
//                    case 1:
//                        m_objPreview.SetStreamResolution(240, 320);
//                        break;
//                    case 2:
//                        m_objPreview.SetStreamResolution(640, 480);
//                        break;
//                    case 3:
//                        m_objPreview.SetStreamResolution(640, 360);
//                        break;
//                    default:
//                        break;
//                }
//            }
//            else if (parent.getId() == R.id.spinner_camera) {
//                Spinner spin = (Spinner)findViewById(R.id.spinner_camera);
//                String szName = spin.getSelectedItem().toString();
//                switch (szName)
//                {
//                    case "设置视频参数":
//                        m_dwCmdCamera = JavaInterface.USB_SET_VIDEO_PARAM;
//                        break;
//                    default:
//                        break;
//                }
//                Log.i("[USBDemo]", "选择的前端配置是   m_dwCmdCamera:" + m_dwCmdCamera);
//            }
//            else if (parent.getId() == R.id.spinner_acs) {
//                Spinner spin = (Spinner)findViewById(R.id.spinner_acs);
//                String szName = spin.getSelectedItem().toString();
//                switch (szName)
//                {
//                    case "Управление зуммером и светом дисплея":
//                        m_dwCmdAcs = JavaInterface.USB_SET_BEEP_AND_FLICKER;
//                        break;
//                    case "Получить версию прошивки эмитента карты":
//                        m_dwCmdAcs = JavaInterface.USB_GET_CARD_ISSUE_VERSION;
//                        break;
//                    case "Установить протокол карты":
//                        m_dwCmdAcs = JavaInterface.USB_SET_CARD_PROTO;
//                        break;
//                    case "Активировать карту":
//                        m_dwCmdAcs = JavaInterface.USB_GET_ACTIVATE_CARD;
//                        break;
//                    case "Остановить работу карты":
//                        m_dwCmdAcs = JavaInterface.USB_CTRL_STOP_CARD_OPER;
//                        break;
//                    case "Получить идентификационную информацию":
//                        m_dwCmdAcs = JavaInterface.USB_GET_CERTIFICATE_INFO;
//                        break;
//                    case "Задать сектор с параметрами аутентификации пароля":
//                        m_dwCmdAcs = JavaInterface.USB_SET_M1_PWD_VERIFY;
//                        break;
//                    case "Считать данные блока, указанного картой":
//                        m_dwCmdAcs = JavaInterface.USB_GET_M1_READ_BLOCK;
//                        break;
//                    case "Записать данные блока, указанного картой":
//                        m_dwCmdAcs = JavaInterface.USB_SET_M1_WRITE_BLOCK;
//                        break;
//                    case "Задать модификацию данных блока управления сектором":
//                        m_dwCmdAcs = JavaInterface.USB_SET_M1_MODIFY_SCB;
//                        break;
//                    case "Задать информацию шифрования указанного сектора":
//                        m_dwCmdAcs = JavaInterface.USB_SET_M1_SECTION_ENCRYPT;
//                        break;
//                    default:
//                        break;
//                }
//                Log.i("[USBDemo]", "选择的门禁配置是   m_dwCmdAcs:" + m_dwCmdAcs);
//            }
//            else if (parent.getId() == R.id.spinner_transfer) {
//                Spinner spin = (Spinner)findViewById(R.id.spinner_transfer);
//                String szName = spin.getSelectedItem().toString();
//                switch (szName)
//                {
//                    case "Шифрование устройства":
//                        m_dwCmdTransfer = JavaInterface.USB_SET_SYSTEM_ENCRYPT_DATA;
//                        break;
//                    case "Экспорт файла журнала":
//                        m_dwCmdTransfer = JavaInterface.USB_GET_SYSTEM_LOG_DATA;
//                        break;
//                    case "Установка WDR изображения":
//                        m_dwCmdTransfer = JavaInterface.USB_SET_IMAGE_WDR;
//                        break;
//                    case "Получение состояния аудиовхода":
//                        m_dwCmdTransfer = JavaInterface.USB_GET_AUDIO_IN_STATUS;
//                        break;
//                    case "Экспорт аудиоданных":
//                        m_dwCmdTransfer = JavaInterface.USB_GET_AUDIO_DUMP_DATA;
//                        break;
//                    default:
//                        break;
//                }
//                Log.i("[USBDemo]", "选择的传显配置是   m_dwCmdTransfer:" + m_dwCmdTransfer);
//            }
//            if (parent.getId() == R.id.spinner_thermal) {
//                Spinner spin = (Spinner)findViewById(R.id.spinner_thermal);
//                String szName = spin.getSelectedItem().toString();
//                switch (szName)
//                {
//                    case "Установить параметры видео":
//                        m_dwCmdThermal = JavaInterface.USB_SET_VIDEO_PARAM;
//                        break;
//                    case "Получить информацию об устройстве":
//                        m_dwCmdThermal = JavaInterface.USB_GET_SYSTEM_DEVICE_INFO;
//                        break;
//                    case "Перезагрузка устройства":
//                        m_dwCmdThermal = JavaInterface.USB_SET_SYSTEM_REBOOT;
//                        break;
//                    case "Восстановить значения по умолчанию":
//                        m_dwCmdThermal = JavaInterface.USB_SET_SYSTEM_RESET;
//                        break;
//                    case "Получить параметры обслуживания оборудования":
//                        m_dwCmdThermal = JavaInterface.USB_GET_SYSTEM_HARDWARE_SERVER;
//                        break;
//                    case "Установить параметры обслуживания оборудования":
//                        m_dwCmdThermal = JavaInterface.USB_SET_SYSTEM_HARDWARE_SERVER;
//                        break;
//                    case "Получить системное местное время":
//                        m_dwCmdThermal = JavaInterface.USB_GET_SYSTEM_LOCALTIME;
//                        break;
//                    case "Установить системное местное время":
//                        m_dwCmdThermal = JavaInterface.USB_SET_SYSTEM_LOCALTIME;
//                        break;
//                    case "Получить параметры яркости изображения":
//                        m_dwCmdThermal = JavaInterface.USB_GET_IMAGE_BRIGHTNESS;
//                        break;
//                    case "Установить параметры яркости изображения":
//                        m_dwCmdThermal = JavaInterface.USB_SET_IMAGE_BRIGHTNESS;
//                        break;
//                    case "Получить параметры контрастности изображения":
//                        m_dwCmdThermal = JavaInterface.USB_GET_IMAGE_CONTRAST;
//                        break;
//                    case "Установить параметры контрастности изображения":
//                        m_dwCmdThermal = JavaInterface.USB_SET_IMAGE_CONTRAST;
//                        break;
//                    case "Коррекция фона в один клик":
//                        m_dwCmdThermal = JavaInterface.USB_SET_IMAGE_BACKGROUND_CORRECT;
//                        break;
//                    case "Экспорт диагностической информации":
//                        m_dwCmdThermal = JavaInterface.USB_GET_SYSTEM_DIAGNOSED_DATA;
//                        break;
//                    case "Коррекция вручную в один клик":
//                        m_dwCmdThermal = JavaInterface.USB_SET_IMAGE_MANUAL_CORRECT;
//                        break;
//                    case "Получить параметры улучшения изображения":
//                        m_dwCmdThermal = JavaInterface.USB_GET_IMAGE_ENHANCEMENT;
//                        break;
//                    case "Установить параметры улучшения изображения":
//                        m_dwCmdThermal = JavaInterface.USB_SET_IMAGE_ENHANCEMENT;
//                        break;
//                    case "Получить параметры настройки видео":
//                        m_dwCmdThermal = JavaInterface.USB_GET_IMAGE_VIDEO_ADJUST;
//                        break;
//                    case "Установить параметры настройки видео":
//                        m_dwCmdThermal = JavaInterface.USB_SET_IMAGE_VIDEO_ADJUST;
//                        break;
//                    case "Получить основные параметры измерения температуры":
//                        m_dwCmdThermal = JavaInterface.USB_GET_THERMOMETRY_BASIC_PARAM;
//                        break;
//                    case "Установить основные параметры измерения температуры":
//                        m_dwCmdThermal = JavaInterface.USB_SET_THERMOMETRY_BASIC_PARAM;
//                        break;
//                    case "Получить режим измерения температуры":
//                        m_dwCmdThermal = JavaInterface.USB_GET_THERMOMETRY_MODE;
//                        break;
//                    case "Установить режим измерения температуры":
//                        m_dwCmdThermal = JavaInterface.USB_SET_THERMOMETRY_MODE;
//                        break;
//                    case "Получить параметры правила измерения температуры":
//                        m_dwCmdThermal = JavaInterface.USB_GET_THERMOMETRY_REGIONS;
//                        break;
//                    case "Установить параметры правила измерения температуры":
//                        m_dwCmdThermal = JavaInterface.USB_SET_THERMOMETRY_REGIONS;
//                        break;
//                    case "Получить информацию о версии алгоритма, связанного с тепловидением":
//                        m_dwCmdThermal = JavaInterface.USB_GET_THERMAL_ALG_VERSION;
//                        break;
//                    case "Получить параметры потока термографического кода":
//                        m_dwCmdThermal = JavaInterface.USB_GET_THERMAL_STREAM_PARAM;
//                        break;
//                    case "Установить параметры потока термографического кода":
//                        m_dwCmdThermal = JavaInterface.USB_SET_THERMAL_STREAM_PARAM;
//                        break;
//                    case "Получить параметры термометрической коррекции":
//                        m_dwCmdThermal = JavaInterface.USB_GET_TEMPERATURE_CORRECT;
//                        break;
//                    case "Установить параметры термометрической коррекции":
//                        m_dwCmdThermal = JavaInterface.USB_SET_TEMPERATURE_CORRECT;
//                        break;
//                    case "Получить параметры черного тела":
//                        m_dwCmdThermal = JavaInterface.USB_GET_BLACK_BODY;
//                        break;
//                    case "Установить параметры черного тела":
//                        m_dwCmdThermal = JavaInterface.USB_SET_BLACK_BODY;
//                        break;
//                    case "Получить параметры компенсации температуры тела":
//                        m_dwCmdThermal = JavaInterface.USB_GET_BODYTEMP_COMPENSATION;
//                        break;
//                    case "Установить параметры компенсации температуры тела":
//                        m_dwCmdThermal = JavaInterface.USB_SET_BODYTEMP_COMPENSATION;
//                        break;
//                    case "Получить тепловую карту":
//                        m_dwCmdThermal = JavaInterface.USB_GET_JPEGPIC_WITH_APPENDDATA;
//                        break;
//                    case "Запрос информации о максимальной температуре региона":
//                        m_dwCmdThermal = JavaInterface.USB_GET_ROI_MAX_TEMPERATURE_SEARCH;
//                        break;
//                    case "Получить параметры температуры всего экрана":
//                        m_dwCmdThermal = JavaInterface.USB_GET_P2P_PARAM;
//                        break;
//                    case "Установить параметры температуры всего экрана":
//                        m_dwCmdThermal = JavaInterface.USB_SET_P2P_PARAM;
//                        break;
//                    case "Экспорт файла калибровки температуры":
//                        m_dwCmdThermal = JavaInterface.USB_GET_THERMOMETRY_CALIBRATION_FILE;
//                        break;
//                    case "Импорт файла калибровки температуры":
//                        m_dwCmdThermal = JavaInterface.USB_SET_THERMOMETRY_CALIBRATION_FILE;
//                        break;
//                    case "Получить правила экспертной температуры":
//                        m_dwCmdThermal = JavaInterface.USB_GET_THERMOMETRY_EXPERT_REGIONS;
//                        break;
//                    case "Установить правила экспертной температуры":
//                        m_dwCmdThermal = JavaInterface.USB_SET_THERMOMETRY_EXPERT_REGIONS;
//                        break;
//                    case "Получить параметры калибровки экспертной температуры":
//                        m_dwCmdThermal = JavaInterface.USB_GET_EXPERT_CORRECTION_PARAM;
//                        break;
//                    case "Установить параметры калибровки экспертной температуры":
//                        m_dwCmdThermal = JavaInterface.USB_SET_EXPERT_CORRECTION_PARAM;
//                        break;
//                    case "Запустить калибровку экспертной температуры":
//                        m_dwCmdThermal = JavaInterface.USB_START_EXPERT_CORRECTION;
//                        break;
//                    case "Получить параметры повышения температуры":
//                        m_dwCmdThermal = JavaInterface.USB_GET_THERMOMETRY_RISE_SETTINGS;
//                        break;
//                    case "Установить параметры повышения температуры":
//                        m_dwCmdThermal = JavaInterface.USB_SET_THERMOMETRY_RISE_SETTINGS;
//                        break;
//                    case "Получить параметры коррекции температуры окружающей среды":
//                        m_dwCmdThermal = JavaInterface.USB_GET_ENVIROTEMPERATURE_CORRECT;
//                        break;
//                    case "Установите параметры калибровки температуры окружающей среды":
//                        m_dwCmdThermal = JavaInterface.USB_SET_ENVIROTEMPERATURE_CORRECT;
//                        break;
//                    default:
//                        break;
//                }
//                Log.i("[USBDemo]", "选择的热成像配置是   m_dwCmdThermal:" + m_dwCmdThermal);
//            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private void ExecShell(String cmd)
    {
        try {
            Process p=Runtime.getRuntime().exec(new String[]{"su","-c",cmd});
            BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
            String readLine=br.readLine();
            while(readLine!=null) {
                System.out.println(readLine);
                readLine=br.readLine();
            }
            if(br!=null){
                br.close();
            }
            p.destroy();
            p=null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //读写文件权限动态申请
    public void CheckPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                Log.i("[USBDemo]", "未获得读写权限");
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    Log.i("[USBDemo]", "用户永久拒绝权限申请");
                }
                else
                {
                    Log.i("[USBDemo]", "申请权限");
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
            }
            Log.i("[USBDemo]", "已获得读写权限");
        } else {
            Log.i("[USBDemo]", "无需动态申请");
        }
    }

    //指定权限动态申请
    public void CheckPermission(String sPermission)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, sPermission) != PackageManager.PERMISSION_GRANTED)
            {
                Log.i("[USBDemo]", "未获得权限");
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, sPermission))
                {
                    Log.i("[USBDemo]", "用户永久拒绝权限申请");
                }
                else
                {
                    Log.i("[USBDemo]", "申请权限");
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, new String[]{sPermission}, 100);
                }
            }
            Log.i("[USBDemo]", "已获得权限");
        } else {
            Log.i("[USBDemo]", "无需动态申请");
        }
    }

    //网络权限动态申请
    public void CheckNetworkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            {
                Log.i("[USBDemo]", "未获得网络权限");
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET))
                {
                    Log.i("[USBDemo]", "用户永久拒绝权限申请");
                }
                else
                {
                    Log.i("[USBDemo]", "申请权限");
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 100);
                }
            }
            Log.i("[USBDemo]", "已获得网络权限");
        } else {
            Log.i("[USBDemo]", "无需动态申请");
        }
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

    private double temp (int lUserID)
    {
//        m_objThermal.Config(m_dwCurUserID, 2031);
//        m_objThermal.Config(m_dwCurUserID, 2041);

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
        struROITemperatureSearch.struThermalROIRegion[0].dwDistance = 30;

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
        double maxTemp = struROITemperatureSearchResult.dwMaxP2PTemperature / 10.0;
        return maxTemp;
    }


    private void checkForUpdates() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Replace with your server URL
                    URL url = new URL("http://0.0.0.0:8000/version.json");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String json = reader.readLine();
                    reader.close();

                    // Parse JSON data
                    JSONObject obj = new JSONObject(json);
                    double versionCode = obj.getInt("versionCode");
                    String versionName = obj.getString("versionName");
                    String apkFileUrl = obj.getString("apkFile");

                    // Replace with your current version code
                    if (versionCode > 1.1) {
                        // A new version is available
                        // Now download and install the new version
                        downloadAndInstallApk(apkFileUrl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void downloadAndInstallApk(String apkFileUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apkFileUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "app-v7a-debug.apk");
                    if (outputFile.exists()) {
                        outputFile.delete();
                    }

                    FileOutputStream fos = new FileOutputStream(outputFile);
                    InputStream is = connection.getInputStream();

                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                    }

                    fos.flush();
                    fos.close();
                    is.close();

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void startListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null) {
                        for (String result : matches) {
                            if (result.toLowerCase().contains("нурай") || result.toLowerCase().contains("нугай") || result.toLowerCase().contains("нухай") || result.toLowerCase().contains("нулай") || result.toLowerCase().contains("нуурай") || result.toLowerCase().contains("нур ай") || result.toLowerCase().contains("ну рай") || result.toLowerCase().contains("нураай") || result.toLowerCase().contains("нураи") || result.toLowerCase().contains("урай") || result.toLowerCase().contains("привет нурай") || result.toLowerCase().contains("хей нурай") || result.toLowerCase().contains("нуай") || result.toLowerCase().contains("нура") || result.toLowerCase().contains("нурайка")) {
                                myWebView.setWebViewClient(new WebViewClient() {

                                    @Override
                                    public void onPageFinished(WebView view, String url) {
                                        super.onPageFinished(view, url);

                                        view.loadUrl(
                                                "javascript:(function() { " +
                                                        "var elements = document.getElementsByClassName('SpeechRecognation_speechRecognation__ZeBqn container');" +
                                                        "for (var i = 0; i < elements.length; i++) {" +
                                                        "elements[i].click();" +
                                                        "}" +
                                                        "})()"
                                        );
                                    }
                                });
                                break;
                            }
                        }
                    }
                    // Restart listening when current listening ends
                    startListening();
                }

                @Override
                public void onEndOfSpeech() {
                    // Restart listening when current listening ends
                    startListening();
                }

                // Other methods of RecognitionListener are left for brevity

                @Override
                public void onReadyForSpeech(Bundle params) {}
                @Override
                public void onBeginningOfSpeech() {}
                @Override
                public void onRmsChanged(float rmsdB) {}
                @Override
                public void onBufferReceived(byte[] buffer) {}
                @Override
                public void onError(int error) {
                    // Start listening again if there's an error.
                    startListening();
                }
                @Override
                public void onPartialResults(Bundle partialResults) {}
                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU"); // Add this line
        speechRecognizer.startListening(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }


}