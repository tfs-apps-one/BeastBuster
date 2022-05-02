package apps.ft.beastbuster;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

//効果音
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.widget.Button;
//タイマースレッド
import java.io.IOException;
import java.security.Policy;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
// デバッグログ
import android.util.Log;
//設定関連
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
// ライト
//import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
//  加速度センサ
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;
//国
import java.util.Locale;
import android.content.res.Resources;
import android.widget.Toast;
//DB
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;


//public class MainActivity extends AppCompatActivity {
public class MainActivity extends AppCompatActivity
        implements SensorEventListener {

    private MediaPlayer bgm;
    private int now_volume;
    private boolean isplaying = false;
    private MediaPlayer countText;			//テキストビュー

    private Timer mainTimer1;					//タイマー用
    private MainTimerTask mainTimerTask1;		//タイマタスククラス
    private Timer mainTimer2;					//タイマー用
    private MainTimerTask mainTimerTask2;		//タイマタスククラス
    private Timer mainTimer3;					//タイマー用
    private MainTimerTask mainTimerTask3;		//タイマタスククラス
    private Handler mHandler = new Handler();   //UI Threadへのpost用ハンドラ

    //    static MySensor mySensor = null;         //テキストビュー
    private Timer emerTimer;					//タイマー用
    private EmerTimerTask emerTimerTask;		//タイマタスククラス
    private Handler eHandler = new Handler();   //UI Threadへのpost用ハンドラ

    //設定関連
    private int sound_volume = 0;
    private int bell_kind = 0;
    private int gun_kind = 0;
    private int thunder_kind = 0;
    private int screen_type = 0;
    private int play_delay = 0;
    private String emergency_kind = "";
    private boolean volume_back = false;
    //ライト関連
    private CameraManager mCameraManager;
    private String mCameraId = null;
    private boolean isOn = false;
    protected final static double RAD2DEG = 180/Math.PI;
    SensorManager sensorManager;
    float[] rotationMatrix = new float[9];
    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];
    float[] attitude = new float[3];
    private boolean emergency_playing = false;
    private int roll_plus = 0;
    private int roll_minus = 0;
    private int pitch_zero = 0;
    private int sec_five = 0;
    //  国設定
    private Locale _local;
    private String _language;
    private String _country;
    // DB
    public MyOpenHelper helper;
    private int db_user_lv = 0; //ユーザーレベル
    private int db_data1 = 0;   //再生回数
    private int db_data2 = 0;
    private int db_data3 = 0;
    private int db_data4 = 0;
    private int db_data5 = 0;

    final int PLAY_INIT_COUNT = 450;    //30分程度

    // 広告
    private AdView mAdview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //  国設定
        _local = Locale.getDefault();
        _language = _local.getLanguage();
        _country = _local.getCountry();

        TextView v = (TextView)findViewById(R.id.textView);
        v.setBackgroundTintList(null);
        if (_language.equals("ja")) {
            v.setText("[PLAY]で通常再生します");
        }
        else if (_language.equals("zh")) {
            v.setText("在[PLAY]播放在正常时间");
        }
        else if (_language.equals("ko")) {
            v.setText("[PLAY]는 보통 때 재생합니다");
        }
        else{
            v.setText("Please press [PLAY]");
        }
        v.setTextColor(Color.parseColor("black"));
        /*
        v.setBackgroundColor(Color.parseColor("white"));
         */
        v.setBackgroundResource(R.drawable.bak_grad);

        //音
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        now_volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        //カメラ初期化
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mCameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                mCameraId = cameraId;
                isOn = enabled;
            }
        }, new Handler());

        //広告
        mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);
    }

    /* DB初期設定 */
    public void AppDBInitRoad() {
        int data = 0;
        int data2 = 0;
        int data3 = 0;

        SQLiteDatabase db = helper.getReadableDatabase();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT");
        sql.append(" level");
        sql.append(" ,data1");
        sql.append(" ,data2");
        sql.append(" ,data3");
        sql.append(" ,data4");
        sql.append(" ,data5");
        sql.append(" FROM appinfo;");
        try {
            Cursor cursor = db.rawQuery(sql.toString(), null);
            //TextViewに表示
            StringBuilder text = new StringBuilder();
            if (cursor.moveToNext()) {
                data = cursor.getInt(0);
                data2 = cursor.getInt(1);
                data3 = cursor.getInt(2);
            }
        } finally {
            db.close();
        }

        db = helper.getWritableDatabase();
        if (data == 0) {
            long ret;
            /* 新規レコード追加 */
            ContentValues insertValues = new ContentValues();
            db_user_lv = 1;
            insertValues.put("level", 1);
            insertValues.put("data1", 0);
            insertValues.put("data2", 0);
            insertValues.put("data3", 0);
            insertValues.put("data4", 0);
            insertValues.put("data5", 0);
            try {
                ret = db.insert("appinfo", null, insertValues);
            } finally {
                db.close();
            }
            if (ret == -1) {
                Toast.makeText(this, "DataBase Create.... ERROR", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "DataBase Create.... OK", Toast.LENGTH_SHORT).show();
            }
        } else {
            db_user_lv = data;
            /*            Toast.makeText(this, "Data Loading...  Access Level:" + user_lv, Toast.LENGTH_SHORT).show();*/
        }
    }
    /* DB更新 */
    public void AppDBUpdated() {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues insertValues = new ContentValues();
        insertValues.put("level", db_user_lv);
        insertValues.put("data1", db_data1);
        insertValues.put("data2", db_data2);
        insertValues.put("data3", db_data3);
        insertValues.put("data4", db_data4);
        insertValues.put("data5", db_data5);

        int ret;
        try {
            ret = db.update("appinfo", insertValues, null, null);
        } finally {
            db.close();
        }
        if (ret == -1){
            Toast.makeText(this, "Saving.... ERROR ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Saving.... OK ", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onStart() {
        super.onStart();

        //センサ初期化
        if (sensorManager == null) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }


        //  設定関連読み込み
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences sharedPreferences = getSharedPreferences("DataStore", MODE_PRIVATE);
        //音量
        String str1 = sharedPreferences.getString("play_volume", "2");
        sound_volume = Integer.parseInt(str1);
        //鈴音
        String str2 = sharedPreferences.getString("bell_kind", "1");
        bell_kind = Integer.parseInt(str2);
        //銃声
        String str3 = sharedPreferences.getString("gun_kind", "1");
        gun_kind = Integer.parseInt(str3);
        //雷鳴
        String str4 = sharedPreferences.getString("thunder_kind", "1");
        thunder_kind = Integer.parseInt(str4);
        //間隔
        String str5 = sharedPreferences.getString("play_delay", "0");
        play_delay = Integer.parseInt(str5);
        if(play_delay == 0) play_delay = 10;
        else                 play_delay *= 1000;
        //緊急音
        emergency_kind = sharedPreferences.getString("emergency_kind", "thunder");
        //音量戻し
        volume_back = sharedPreferences.getBoolean("volume_back", false);
        //画面タイプ
        String str6 = sharedPreferences.getString("screen_type", "1");
        screen_type = Integer.parseInt(str6);

        //センサ監視起動
        this.emerTimer = new Timer();
        //タスククラスインスタンス生成
        this.emerTimerTask = new EmerTimerTask();
        //タイマースケジュール設定＆開始
        this.emerTimer.schedule(emerTimerTask, 500, 1000);

        //DB load
        helper = new MyOpenHelper(this);
        AppDBInitRoad();

/*        if (db_user_lv < 5)
        {
            screen_type = 1;
        }*/
        ImageShow();
    }
    public void ImageShow()
    {
        //ベル
        ImageView view1 = (ImageView) findViewById(R.id.imageView);
        if (screen_type == 2)   view1.setImageResource(R.drawable.bell2);
        else                     view1.setImageResource(R.drawable.bell);

        //銃
        ImageView view2 = (ImageView) findViewById(R.id.imageView2);
        if (screen_type == 2)   view2.setImageResource(R.drawable.gun2);
        else                     view2.setImageResource(R.drawable.gun);

        //雷鳴
        ImageView view3 = (ImageView) findViewById(R.id.imageView3);
        if (screen_type == 2)   view3.setImageResource(R.drawable.thunder2);
        else                     view3.setImageResource(R.drawable.thunder);


        Button btn1 = (Button) findViewById(R.id.btn_bell);
        Button btn2 = (Button) findViewById(R.id.btn_gun);
        Button btn3 = (Button) findViewById(R.id.btn_thunder);
        Button btn4 = (Button) findViewById(R.id.btn_emergency);
        Button btn5 = (Button) findViewById(R.id.btn_tips);

        btn1.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn2.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn3.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn4.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn5.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn5.setBackgroundResource(R.drawable.btn_tips);

        if (this.mainTimer1 == null) {
            btn1.setText("PLAY");
            btn1.setTextColor(Color.parseColor("gray"));
            //画面デザイン　色変更
            /*
            if (screen_type == 2)   btn1.setBackgroundColor(0xfffafafa);
            else                    btn1.setBackgroundColor(0xffc0ffff);
            */
            if (screen_type == 2)   btn1.setBackgroundResource(R.drawable.btn_round2);
            else                    btn1.setBackgroundResource(R.drawable.btn_round);
        }
        else
        {
            btn1.setText("STOP");
            btn1.setTextColor(Color.parseColor("gray"));
            /*btn1.setBackgroundColor(0xffd3d3d3);
             */
            btn1.setBackgroundResource(R.drawable.btn_stop);
        }
        if (this.mainTimer2 == null) {
            btn2.setText("PLAY");
            btn2.setTextColor(Color.parseColor("gray"));
            /*
            if (screen_type == 2)   btn2.setBackgroundColor(0xfffafafa);
            else                    btn2.setBackgroundColor(0xffc0ffff);
            */
            if (screen_type == 2)   btn2.setBackgroundResource(R.drawable.btn_round2);
            else                    btn2.setBackgroundResource(R.drawable.btn_round);
        }
        else
        {
            btn2.setText("STOP");
            btn2.setTextColor(Color.parseColor("gray"));
            /*
            btn2.setBackgroundColor(0xffd3d3d3);
            */
            btn2.setBackgroundResource(R.drawable.btn_stop);
        }
        if (this.mainTimer3 == null) {
            btn3.setText("PLAY");
            btn3.setTextColor(Color.parseColor("gray"));
            /*
            if (screen_type == 2)   btn3.setBackgroundColor(0xfffafafa);
            else                    btn3.setBackgroundColor(0xffc0ffff);
             */
            if (screen_type == 2)   btn3.setBackgroundResource(R.drawable.btn_round2);
            else                    btn3.setBackgroundResource(R.drawable.btn_round);
        }
        else
        {
            btn3.setText("STOP");
            btn3.setTextColor(Color.parseColor("gray"));
            /*
            btn3.setBackgroundColor(0xffd3d3d3);
            */
            btn3.setBackgroundResource(R.drawable.btn_stop);
        }

        /* 緊急ボタン */
        /*
        btn4.setBackgroundColor(0xfff08080);
         */
        btn4.setBackgroundResource(R.drawable.btn_emer);
    }

    /* 効果音スタート */
    public void soundStart(int type, int mode){
        ImageShow();

        Button btn1 = (Button) findViewById(R.id.btn_bell);
        Button btn2 = (Button) findViewById(R.id.btn_gun);
        Button btn3 = (Button) findViewById(R.id.btn_thunder);

        btn1.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn2.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn3.setBackgroundTintList(null);   //マテリアルデザインの無効

        btn1.setText("PLAY");
        btn1.setTextColor(Color.parseColor("gray"));
        /*
        if (screen_type == 2)   btn1.setBackgroundColor(0xfffafafa);
        else                    btn1.setBackgroundColor(0xffc0ffff);
        */
        if (screen_type == 2)   btn1.setBackgroundResource(R.drawable.btn_round2);
        else                    btn1.setBackgroundResource(R.drawable.btn_round);

        btn2.setText("PLAY");
        btn2.setTextColor(Color.parseColor("gray"));
        /*
        if (screen_type == 2)   btn2.setBackgroundColor(0xfffafafa);
        else                    btn2.setBackgroundColor(0xffc0ffff);
         */
        if (screen_type == 2)   btn2.setBackgroundResource(R.drawable.btn_round2);
        else                    btn2.setBackgroundResource(R.drawable.btn_round);

        btn3.setText("PLAY");
        btn3.setTextColor(Color.parseColor("gray"));
        /*
        if (screen_type == 2)   btn3.setBackgroundColor(0xfffafafa);
        else                    btn3.setBackgroundColor(0xffc0ffff);
         */
        if (screen_type == 2)   btn3.setBackgroundResource(R.drawable.btn_round2);
        else                    btn3.setBackgroundResource(R.drawable.btn_round);

        btn1.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn2.setBackgroundTintList(null);   //マテリアルデザインの無効
        btn3.setBackgroundTintList(null);   //マテリアルデザインの無効

        switch (type){
            case 1:
                btn1.setText("STOP");
                btn1.setTextColor(Color.parseColor("gray"));
                /*
                btn1.setBackgroundColor(0xffd3d3d3);
                 */
                btn1.setBackgroundResource(R.drawable.btn_stop);
                break;
            case 2:
                btn2.setText("STOP");
                btn2.setTextColor(Color.parseColor("gray"));
                /*
                btn2.setBackgroundColor(0xffd3d3d3);
                 */
                btn2.setBackgroundResource(R.drawable.btn_stop);
                break;
            case 3:
                btn3.setText("STOP");
                btn3.setTextColor(Color.parseColor("gray"));
                /*
                btn3.setBackgroundColor(0xffd3d3d3);
                 */
                btn3.setBackgroundResource(R.drawable.btn_stop);
                break;
        }

        switch(type) {
            case 1:
                //タイマーインスタンス生成
                this.mainTimer1 = new Timer();
                //タスククラスインスタンス生成
                this.mainTimerTask1 = new MainTimerTask();
                //タイマースケジュール設定＆開始
                this.mainTimer1.schedule(mainTimerTask1, 500, play_delay);
                //ＢＧＭ
                if (bell_kind == 2)         this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.bell_2);
                else if (bell_kind == 3)    this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.bell_3);
                else                        this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.bell_1);

                if (this.mainTimer2 != null) {
                    this.mainTimer2.cancel();
                    this.mainTimer2 = null;
                }
                if (this.mainTimer3 != null) {
                    this.mainTimer3.cancel();
                    this.mainTimer3 = null;
                }
                break;

            case 2:
                //タイマーインスタンス生成
                this.mainTimer2 = new Timer();
                //タスククラスインスタンス生成
                this.mainTimerTask2 = new MainTimerTask();
                //タイマースケジュール設定＆開始
                this.mainTimer2.schedule(mainTimerTask2, 500, play_delay);
                //ＢＧＭ
                if (gun_kind == 2)  this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.gun_2);
                else if (gun_kind == 3)  this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.gun_3);
                else                       this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.gun_1);

                if (this.mainTimer1 != null) {
                    this.mainTimer1.cancel();
                    this.mainTimer1 = null;
                }
                if (this.mainTimer3 != null) {
                    this.mainTimer3.cancel();
                    this.mainTimer3 = null;
                }
                break;

            case 3:
                //タイマーインスタンス生成
                this.mainTimer3 = new Timer();
                //タスククラスインスタンス生成
                this.mainTimerTask3 = new MainTimerTask();
                //タイマースケジュール設定＆開始
                this.mainTimer3.schedule(mainTimerTask3, 500, play_delay);
                //ＢＧＭ
                if (thunder_kind == 2) this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.thunder_2);
                else if (thunder_kind == 3) this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.thunder_3);
                else                            this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.thunder_1);

                if (this.mainTimer1 != null) {
                    this.mainTimer1.cancel();
                    this.mainTimer1 = null;
                }
                if (this.mainTimer2 != null) {
                    this.mainTimer2.cancel();
                    this.mainTimer2 = null;
                }
                break;
        }


        //音量調整
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
       // 音量を設定する
        am.setStreamVolume(AudioManager.STREAM_MUSIC, sound_volume, 0);

/*        if (bgm.isPlaying() == false) {
            bgm.setLooping(true);
            bgm.start();
            isplaying = true;
        }*/

        TextView v = (TextView) findViewById(R.id.textView);
        v.setBackgroundTintList(null);
        if (mode == 1) {
            if (_language.equals("ja")) {
                v.setText("緊急時の再生中です");
            }
            else if (_language.equals("zh")) {
                v.setText("它是紧急的播放期间");
            }
            else if (_language.equals("ko")) {
                v.setText("비상 재생 중입니다");
            }
            else {
                v.setText("Playing in emergency");
            }
            v.setTextColor(Color.parseColor("red"));
            /*
            v.setBackgroundColor(Color.parseColor("white"));
             */
        }
        else{
            light_OFF();
            if (_language.equals("ja")) {
                v.setText("通常再生中です");
//                v.setText("通常再生中です\n(繰り返し連続再生)");
            }
            else if (_language.equals("zh")) {
                v.setText("它是在正常的时间正在播放");
//                v.setText("它是在正常的时间正在播放\n(重复连续播放)");
            }
            else if (_language.equals("ko")) {
                v.setText("보통 때 재생 중입니다");
//                v.setText("보통 때 재생 중입니다\n(반복 재생)");
            }
            else
            {
                v.setText("Normal playback is in progress");
            }
            v.setTextColor(Color.parseColor("blue"));
            /*
            v.setBackgroundColor(Color.parseColor("white"));
             */
        }
        v.setBackgroundResource(R.drawable.bak_grad);
    }

    /* 効果音ストップ */
    public void soundStop(int type){
        ImageShow();

        switch (type){
            case 1:
                Button btn1 = (Button) findViewById(R.id.btn_bell);
                btn1.setBackgroundTintList(null);
                btn1.setText("PLAY");
                btn1.setTextColor(Color.parseColor("gray"));
                /*
                if (screen_type == 2)   btn1.setBackgroundColor(0xfffafafa);
                else                    btn1.setBackgroundColor(0xffc0ffff);
                 */
                if (screen_type == 2)   btn1.setBackgroundResource(R.drawable.btn_round2);
                else                    btn1.setBackgroundResource(R.drawable.btn_round);
                break;
            case 2:
                Button btn2 = (Button) findViewById(R.id.btn_gun);
                btn2.setBackgroundTintList(null);
                btn2.setText("PLAY");
                btn2.setTextColor(Color.parseColor("gray"));
                /*
                if (screen_type == 2)   btn2.setBackgroundColor(0xfffafafa);
                else                    btn2.setBackgroundColor(0xffc0ffff);
                 */
                if (screen_type == 2)   btn2.setBackgroundResource(R.drawable.btn_round2);
                else                    btn2.setBackgroundResource(R.drawable.btn_round);
                break;
            case 3:
                Button btn3 = (Button) findViewById(R.id.btn_thunder);
                btn3.setBackgroundTintList(null);
                btn3.setText("PLAY");
                btn3.setTextColor(Color.parseColor("gray"));
                /*
                if (screen_type == 2)   btn3.setBackgroundColor(0xfffafafa);
                else                    btn3.setBackgroundColor(0xffc0ffff);
                */
                if (screen_type == 2)   btn3.setBackgroundResource(R.drawable.btn_round2);
                else                    btn3.setBackgroundResource(R.drawable.btn_round);
                break;
        }

        if (this.mainTimer1 != null) {
            this.mainTimer1.cancel();
            this.mainTimer1 = null;
        }
        if (this.mainTimer2 != null) {
            this.mainTimer2.cancel();
            this.mainTimer2 = null;
        }
        if (this.mainTimer3 != null) {
            this.mainTimer3.cancel();
            this.mainTimer3 = null;
        }

        this.light_OFF();

        TextView v = (TextView)findViewById(R.id.textView);
        v.setBackgroundTintList(null);
        if (_language.equals("ja")) {
            v.setText("再生を中止しました");
        }
        else if (_language.equals("zh")) {
            v.setText("中止播放");
        }
        else if (_language.equals("ko")) {
            v.setText("재생을 중지했습니다");
        }
        else{
            v.setText("Playback was canceled");
        }
        v.setTextColor(Color.parseColor("white"));
        /*
        v.setBackgroundColor(Color.parseColor("gray"));
         */
        v.setBackgroundResource(R.drawable.bak_grad2);
    }

    //  「ベル」ボタン
    public void onBell(View view){
        if (this.mainTimer1 != null)  soundStop(1);
        else                            soundStart(1, 0);
    }
    //  「銃」ボタン
    public void onGun(View view){
        if (this.mainTimer2 != null)  soundStop(2);
        else                            soundStart(2, 0);
    }
    //  「雷」ボタン
    public void onThunder(View view){
        if (this.mainTimer3 != null)  soundStop(3);
        else                            soundStart(3, 0);
    }
    //  「緊急」ボタン
    public void onEmergency(View view){
        pitch_zero = 0;
        roll_minus = 0;
        roll_plus = 0;
        emergency_Start();
    }

    // 緊急時の再生処理
    public void emergency_Start()
    {
        if (emergency_playing == false) {
            if (emergency_kind.equals("none") == true)
            {
                return;
            }
            emergency_playing = true;
            if (emergency_kind.equals("bell") == true){
                soundStop(1);
                soundStart(1, 1);
            }
            else if (emergency_kind.equals("gun")== true){
                soundStop(2);
                soundStart(2, 1);
            }
            else if (emergency_kind.equals("thunder") == true){
                soundStop(3);
                soundStart(3, 1);
            }
            light_ON();
        }
    }

    // TIPS処理
    public void onTips(View view) {
        // 再生中
        if (this.mainTimer1 != null || this.mainTimer2 != null ||
            this.mainTimer3 != null || emergency_playing == true ){
            /* 何もしない */
        }
        // 全て停止中
        else{
            PresentPopup();
        }
    }

    // プレゼント処理
    public void PresentPopup(){
        AlertDialog.Builder guide = new AlertDialog.Builder(this);
        TextView vmessage = new TextView(this);
        int level = 0;
        String pop_message = "";
        String btn_yes = "";
        String btn_no = "";

        //ユーザーレベル算出

        if (_language.equals("ja")) {

            pop_message += "\n\n 動画を視聴してポイントをGETしますか？" +
                    "\n\n（ポイントをGETするとアプリ機能が追加します）" +
                    "\n　１回視聴：アプリ起動時の自動ON" +
                    "\n　２回視聴：画面タイプ「色：灰」追加"+
                    "\n　３回視聴：画面タイプ「色：橙」追加"+
                    "\n 　現在のポイント「"+db_data1+"」→「"+level+"」"+"\n \n\n\n";

            btn_yes += "視聴";
            btn_no += "中止";
        }
        else{
            pop_message += "\n\n \n" +
                    "Do you want to watch the video and get POINTS ?" +
                    "\n\n\n App function will be added when you get POINTS." +
                    "\nExample: Automatic ON function, screen type."+
                    "\n\n POINTS「"+db_data1+"」→「"+level+"」"+"\n \n\n\n";

            btn_yes += "YES";
            btn_no += "N O";
        }

        //メッセージ
        vmessage.setText(pop_message);
        vmessage.setBackgroundColor(Color.DKGRAY);
        vmessage.setTextColor(Color.WHITE);

        //タイトル
        guide.setTitle("TIPS");
        guide.setIcon(R.drawable.present);
        guide.setView(vmessage);

        guide.setPositiveButton(btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /*
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                }
                */
                //test_make
//                    db_data1++;
            }
        });
        guide.setNegativeButton(btn_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ImageShow();
            }
        });

        guide.create();
        guide.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent1 = new android.content.Intent(this, BbSetActivity.class);
            startActivity(intent1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values.clone(); break;
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values.clone(); break;
        }
        if(geomagnetic != null && gravity != null) {
            SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic);
            SensorManager.getOrientation(rotationMatrix, attitude);

            int roll;
            int pitch;
            pitch = (int) (attitude[1] * RAD2DEG);
            roll = (int) (attitude[2] * RAD2DEG);
            Log.v("回転", "roll[0]=" +(int) (attitude[0] * RAD2DEG) + " roll[1] ="+(int) (attitude[1] * RAD2DEG) + " roll[2] = "+roll + " pit=" + pitch_zero + " rp=" +roll_plus + " rm="+roll_minus);
            if (emergency_playing == false) {
                if (roll> 55 && roll <80) {
                    roll_plus += 1;
                }
                if (roll< -55 && roll >-80) {
                    roll_minus += 1;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    /**
     * タイマータスク派生クラス
     * run()に定周期で処理したい内容を記述
     *
     */
    public class MainTimerTask extends TimerTask {
        @Override
        public void run() {
            //ここに定周期で実行したい処理を記述します
            mHandler.post( new Runnable() {
                public void run() {
                    //BGMタイマー起動
                    countText.start();
                }
            });
        }
    }

    /**
     * タイマータスク派生クラス
     * run()に定周期で処理したい内容を記述
     *
     */
    public class EmerTimerTask extends TimerTask {
        @Override
        public void run() {
            //ここに定周期で実行したい処理を記述します
            eHandler.post(new Runnable() {
                public void run() {
                    sec_five += 1;
                    if (sec_five <= 3) {    // ３秒以内にイベントを捉えた場合に限り
                        if (roll_plus >= 4 && roll_minus >= 4) {
//                        if (pitch_zero >= 5 && roll_plus >= 3 && roll_minus >= 3) {
                            pitch_zero = 0;
                            roll_minus = 0;
                            roll_plus = 0;
                            emergency_Start();
                        }
                    }
                    else
                    {
                        pitch_zero = 0;
                        roll_minus = 0;
                        roll_plus = 0;
                        sec_five = 0;
                    }
                }
            });
        }
    }

    /*
     *   ライトＯＮ
     * */
    public void light_ON() {
        if(mCameraId == null){
            return;
        }
        try {
            mCameraManager.setTorchMode(mCameraId, true);
        } catch (CameraAccessException e) {
            //エラー処理
            e.printStackTrace();
        }
    }
    /*
     *   ライトＯＦＦ
     * */
    public void light_OFF() {

        pitch_zero = 0;
        roll_plus = 0;
        roll_minus = 0;
        emergency_playing = false;

        if(mCameraId == null){
            return;
        }
        try {
            mCameraManager.setTorchMode(mCameraId, false);
        } catch (CameraAccessException e) {
            //エラー処理
            e.printStackTrace();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v("LifeCycle", "------------------------------>onResume");

        //センサ関連
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v("LifeCycle", "------------------------------>onPause");
    }

    @Override
    public void onRestart(){
        super.onRestart();
        Log.v("LifeCycle", "------------------------------>onRestart");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.v("LifeCycle", "------------------------------>onStop");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v("LifeCycle", "------------------------------>onDestroy");

        //センサ関連
/*        if(sensorManager != null) {
            sensorManager.unregisterListener(this);
        }*/
        //カメラ
        if (mCameraManager != null)
        {
            mCameraManager = null;
        }

        /* 音量の戻しの処理 */
        if (volume_back == true) {
//      if (volume_back == true && db_user_lv >= 5) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, now_volume, 0);
            am = null;
        }
    }

    //  戻るボタン
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            // 戻るボタンの処理
            // ダイアログ表示など特定の処理を行いたい場合はここに記述
            // 親クラスのdispatchKeyEvent()を呼び出さずにtrueを返す
            if (this.mainTimer1 == null && this.mainTimer2 == null && this.mainTimer3 == null) {
                /* そのまま終了へ */
            }
            else {
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                if (_language.equals("ja")) {
                    ad.setTitle("[戻る]は操作無効です");
                    ad.setMessage("\n\n再生を停止した後\n操作が有効になります\n\n\n\n\n");
                } else if (_language.equals("zh")) {
                    ad.setTitle("\n\n停止播放后\n手术将是有效的");
                    ad.setMessage("\n\n按[HOME]按钮\n\n\n\n\n");
                } else if (_language.equals("ko")) {
                    ad.setTitle("뒤로 버튼 조작 유효하지 않습니다");
                    ad.setMessage("\n\n재생을 정지 한 뒤\n조작이 활성화됩니다\n\n\n\n\n");
                } else {
                    ad.setTitle("Invalid operation");
                    ad.setMessage("\n\nAfter stopping playback.\nThe operation will be effective.\n\n\n\n\n");
                }
                ad.setPositiveButton("ＯＫ", null);
                ad.show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}