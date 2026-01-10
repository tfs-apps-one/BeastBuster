package apps.ft.beastbuster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import java.util.Random;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

//効果音
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.widget.AdapterView;
import android.widget.Button;
//タイマースレッド
import java.io.IOException;
import java.security.Policy;
import java.util.Calendar;
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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;
//国
import java.util.Locale;
import android.content.res.Resources;
import android.widget.Toast;
//DB
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ToggleButton;


//public class MainActivity extends AppCompatActivity {
/*public class MainActivity extends AppCompatActivity
        implements SensorEventListener, RewardedVideoAdListener {*/
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
    private int play_random_delay = 0;
    private boolean isRandomMode = false;
    private String emergency_kind = "";
    private boolean isEmergencyMode = false;
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
    private int db_data2 = 0;   //未使用　リワード視聴日付
    private int db_data3 = 0;   //オプション音（爆竹音）
    private int db_data4 = 0;   //評価ポップアップ
    private int db_data5 = 0;
    private int db_normal = 0;
    private int db_emergency = 0;
    private int db_interval = 0;
    private int db_volume1 = 0;
    private int db_volume2 = 0;
    private int db_light1 = 0;
    private int db_light2 = 0;
    private int db_data6 = 0;
    private int db_data7 = 0;
    private int db_data8 = 0;
    private int db_data9 = 0;
    private int db_data10 = 0;

//TODO:test_make
    final int PLAY_INIT_COUNT = 450;    //30分程度
//    final int PLAY_INIT_COUNT = 5;      //30分程度
    final int PLAY_1800 = 1800;         //2H
    final int PLAY_4500 = 4500;         //5H
    final int PLAY_9000 = 9000;         //10H
    final int PLAY_PLUS = 450;          //30分加算
    private int playcount = 0;  //繰り返し再生回数

    // 広告
    private AdView mAdview;
    // リワード広告
    public RewardedAd rewardedAd;

    //評価ポップアップ
    private int ReviewCount = 1;
 //TODO:test_make
    private int REVIEW_POP = 7; //評価ポップアップ

    // テストID 動画リワード
//TODO:test_make ※※本物を使うこと！！
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    // テストID(APPは本物でOK)
    //private static final String APP_ID = "ca-app-pub-4924620089567925~2701724509";
    // 本物
//    private static final String AD_UNIT_ID = "ca-app-pub-4924620089567925/8788880266";
    // 本物
    //private static final String APP_ID = "ca-app-pub-4924620089567925~2701724509";

//TODO:test_make ※※本番を使うこと！！
    //インタースティシャル広告
    private InterstitialAd mInterstitialAd;
    //本番ID
//    private static final String AD_INTER_UNIT_ID = "ca-app-pub-4924620089567925/3067846578"; // 実際のIDに変更
    //テストID
    private static final String AD_INTER_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    private int SOUND_USED_MAX = 12;


    private int set_interval;
    private Spinner sp_sound1;      //通常音選択
    private Spinner sp_sound2;      //SOS音選択
    private Spinner sp_light2;      //SOSライト選択
    private Spinner sp_interval;    //再生間隔
    private SeekBar seek_volume1;    //通常音量

    private ToggleButton toggle_normal;      //通常音状態（ON/OFF）
    private ToggleButton toggle_emergency;   //異常音状態（ON/OFF）

    final private int INTERVAL_0 = 0;
    final private int INTERVAL_1 = 1000;
    final private int INTERVAL_3 = 3000;
    final private int INTERVAL_5 = 5000;
    final private int INTERVAL_7 = 7000;
    final private int INTERVAL_10 = 10000;
    final private int INTERVAL_15 = 15000;
    final private int INTERVAL_20 = 20000;
    final private int INTERVAL_30 = 30000;
    final private int INTERVAL_RANDUM = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //  国設定
        _local = Locale.getDefault();
        _language = _local.getLanguage();
        _country = _local.getCountry();

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
        MobileAds.initialize(this, initializationStatus -> {
            mAdview = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdview.loadAd(adRequest);
        });

        //動画リワード
        loadRewardedAd();

        //インタースティシャル広告
        loadInterstitialAd();

        //各種イベント登録
        toggleSelect();
        seekSelect();
        spinnerSelect();
        screen_display();
    }

    /************************************************************
        インタースティシャル広告をロード
     ************************************************************/
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, AD_INTER_UNIT_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.d("AdMob", "インタースティシャル広告がロードされました");

                // 広告のコールバックを設定（閉じた後の動作）
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d("AdMob", "広告が閉じられました");
                        mInterstitialAd = null; // 再ロードの準備
                        loadInterstitialAd(); // 次の広告をロード
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                        Log.d("AdMob", "広告の表示に失敗しました: " + adError.getMessage());
                        mInterstitialAd = null; // 再ロードの準備
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("AdMob", "インタースティシャル広告のロードに失敗: " + loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }
    // インタースティシャル広告を表示
    private void showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        } else {
            Log.d("AdMob", "インタースティシャル広告はまだロードされていません");
            loadInterstitialAd(); // すぐに次の広告をロード
        }
    }
    private void fullAdDisplay() {
        if (db_data2 > SOUND_USED_MAX) {
            db_data2 = 0;
        }
        db_data2--;
        if (db_data2 < 0) {
            db_data2 = SOUND_USED_MAX;
        }
        if (db_data2 == 1) {
            //全面広告表示
            showInterstitialAd();
        }
        if (db_data2 == 2 || db_data2 == 3) {
            Context context = getApplicationContext();
            if (_language.equals("ja")) {
                Toast.makeText(context, "しばらく使用すると全面広告が表示されます....", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "After a while, Full-page ad appears....", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /************************************************************
         リワード広告処理
     ************************************************************/
    private void loadRewardedAd() {
        RewardedAd.load(this,
                AD_UNIT_ID,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd Ad) {
                        rewardedAd = Ad;
                        Context context = getApplicationContext();
                        if (_language.equals("ja")) {
                            Toast.makeText(context, "報酬動画準備OK !!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(context, "Movie OK !!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
//                        Log.d("TAG", "The rewarded ad wasn't loaded yet.");
                    }
                });
    }
    public void RdShow(){
        if (rewardedAd != null) {
            Activity activityContext = MainActivity.this;
            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
//                    Log.d("TAG", "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    RdPresent();
                }
            });
        } else {
//            Log.d("TAG", "The rewarded ad wasn't ready yet.");
        }
    }

    public void RdPresent() {
//  public void onRewarded(RewardItem reward) {
        // Reward the user.

        // 再生回数のセット
        int tmp_data = db_data1;
        switch (tmp_data){
            case PLAY_INIT_COUNT:   db_data1 = PLAY_1800;   break;
            case PLAY_1800:         db_data1 = PLAY_4500;   break;
            case PLAY_4500:         db_data1 = PLAY_9000;   break;

            default:                db_data1 = tmp_data + PLAY_PLUS;
                                    db_data3 = 1;   //爆竹と花火を有効
                                    break;
        }

        // 動画視聴の日付
        //db_data2 = getNowDate();

        //ユーザーレベルアップ
        if (_language.equals("ja")) {
            Toast.makeText(this, "連続回数UP!：" + (tmp_data) + "  → " + (db_data1), Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "COUNT UP!：" + (tmp_data) + "  → " + (db_data1), Toast.LENGTH_SHORT).show();
        }
        AppDBUpdated();
        ImageShow();
        loadRewardedAd();   //リワード動画再生の準備
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
        if(play_delay == 0){
            play_delay = 10; isRandomMode = false;
        }
        else if(play_delay < 99){
            play_delay *= 1000; isRandomMode = false;
        }
        else{
            play_delay = 10; isRandomMode = true;
        }

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
        
        //TODO:
        if (db_data1 == 0) {
            db_data1 = PLAY_INIT_COUNT;
        }

        ImageShow();

        //評価ポップアップ処理
        if (db_data4 <= REVIEW_POP){
            if (ReviewCount != 0){
                db_data4++;
                ReviewCount = 0;
            }
        }
        ShowRatingPopup();
    }
    public void ImageShow()
    {

        /*
        ProgressBar prog = (ProgressBar) findViewById(R.id.progress);
        prog.setMin(0);
        prog.setMax(100);
        prog.setProgress(temp_prog);
         */
    }

    /* 効果音スタート */
    public void soundStart(int type, int mode){

        //繰り返し再生回数をゼロにリセット
        playcount = 0;
        int tmp_gun_type = 0;

        if (mode == 0){
            isEmergencyMode = false;
        }
        else{
            isEmergencyMode = true;
        }

        ImageShow();
        play_random_delay = 0;

        switch(type) {
            case 1:
                //タイマーインスタンス生成
                this.mainTimer1 = new Timer();
                //タスククラスインスタンス生成
                this.mainTimerTask1 = new MainTimerTask();
                //タイマースケジュール設定＆開始
                //ＢＧＭ
                soundSelect(db_normal);
                set_interval = soundInterval(db_interval);
                if (set_interval == INTERVAL_RANDUM){
                    isRandomMode = true;
                    set_interval = 100;
                }
                else {
                    isRandomMode = false;
                    if (set_interval < INTERVAL_1) {
                        set_interval = 100;
                    }
                }
                this.mainTimer1.schedule(mainTimerTask1, 500, set_interval);

                if (this.mainTimer2 != null) {
                    this.mainTimer2.cancel();
                    this.mainTimer2 = null;
                    fullAdDisplay();
                }
                if (this.mainTimer3 != null) {
                    this.mainTimer3.cancel();
                    this.mainTimer3 = null;
                    fullAdDisplay();
                }
                break;

            case 2:
                //タイマーインスタンス生成
                this.mainTimer2 = new Timer();
                //タスククラスインスタンス生成
                this.mainTimerTask2 = new MainTimerTask();
                //タイマースケジュール設定＆開始
                if (isEmergencyMode == true)    this.mainTimer2.schedule(mainTimerTask2, 500, 100);
                else                            this.mainTimer2.schedule(mainTimerTask2, 500, play_delay);

                //ＢＧＭ
                tmp_gun_type = gun_kind;
                if (db_data3 < 1 && (gun_kind == 4 || gun_kind == 5 || gun_kind == 6)){
                    tmp_gun_type = 1;   // 動画閲覧しないと設定反映されない
                }
                if (this.mainTimer1 != null) {
                    this.mainTimer1.cancel();
                    this.mainTimer1 = null;
                    fullAdDisplay();
                }
                if (this.mainTimer3 != null) {
                    this.mainTimer3.cancel();
                    this.mainTimer3 = null;
                    fullAdDisplay();
                }
                break;

            case 3:
                //タイマーインスタンス生成
                this.mainTimer3 = new Timer();
                //タスククラスインスタンス生成
                this.mainTimerTask3 = new MainTimerTask();
                //タイマースケジュール設定＆開始
                this.mainTimer3.schedule(mainTimerTask3, 500, 100);
                //ＢＧＭ
                soundSelect(db_emergency);
                isRandomMode = false;
                if (this.mainTimer1 != null) {
                    this.mainTimer1.cancel();
                    this.mainTimer1 = null;
                    fullAdDisplay();
                }
                if (this.mainTimer2 != null) {
                    this.mainTimer2.cancel();
                    this.mainTimer2 = null;
                    fullAdDisplay();
                }
                break;
        }


        //音量調整
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
       // 音量を設定する
        int tmp_volume = db_volume1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            tmp_volume = db_volume1 * 2;  //30段階になったため
        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, tmp_volume, 0);

        if (mode == 1) {
        }
        else{
            light_OFF();
        }

        // 注意
        String mess = "";
        if (_language.equals("ja")) {
            mess = "他のアプリを開くと【連続再生】が停止する場合があります";
        }
        else{
            mess = "Continuous playback may stop if you open another app.";
        }
        Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();

    }

    /* 効果音ストップ */
    public void soundStop(int type){
        ImageShow();
        play_random_delay = 0;
        if (this.mainTimer1 != null) {
            this.mainTimer1.cancel();
            this.mainTimer1 = null;
            fullAdDisplay();
        }
        if (this.mainTimer2 != null) {
            this.mainTimer2.cancel();
            this.mainTimer2 = null;
            fullAdDisplay();
        }
        if (this.mainTimer3 != null) {
            this.mainTimer3.cancel();
            this.mainTimer3 = null;
            fullAdDisplay();
        }

        this.light_OFF();
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
            //何もしない
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

            pop_message += "\n\n広告動画を視聴して報酬を得ますか？\n「連続再生」の回数がＵＰします。" +
                    "\n初期値450回は「鈴音」30分間の\n連続再生に相当します。" +
                    "\n\n\n1回視聴：1800回に増加( 2h 相当)" +
                    "\n2回視聴：4500回に増加( 5h 相当)"+
                    "\n3回視聴：9000回に増加(10h 相当)"+
                    "\n4回以上は「爆竹／花火／狼」の再生有効\n連続回数を450回ずつ増加。"+
                    "\n\n\n※現在の連続再生回数 : "+db_data1+"回"+"\n \n\n\n";

            btn_yes += "視聴";
            btn_no += "中止";
        }
        else{
            pop_message += "\n\n \n" +
                    "Do you want to watch the video and increase the continuous playback COUNT ?" +
                    "\n\n\nPlay the bell for 30 minutes with [ COUNT 450 ]" +
                    "\n\n\nWatch once     [ COUNT 1800 ]" +
                    "\nWatch twice    [ COUNT 4500 ]"+
                    "\nWatch 3 times [ COUNT 9000 ]"+
                    "\n4 times or more will increase by [ COUNT 450 ] and Firecrackers and fireworks playback enabled."+
                    "\n\n\nCurrent COUNT  [ "+db_data1+" ]"+"\n\n\n\n";

            btn_yes += "YES";
            btn_no += "N O";
        }

        //メッセージ
        vmessage.setText(pop_message);
        vmessage.setBackgroundColor(Color.DKGRAY);
        vmessage.setTextColor(Color.WHITE);
        vmessage.setGravity(Gravity.CENTER);
        vmessage.setTextSize(16);

        //タイトル
        guide.setTitle("TIPS");
        guide.setIcon(R.drawable.present);
        guide.setView(vmessage);

        guide.setPositiveButton(btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RdShow();
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


    public void TimeUpPopup(){
        AlertDialog.Builder guide = new AlertDialog.Builder(this);
        TextView vmessage = new TextView(this);
        int level = 0;
        String pop_title = "";
        String pop_message = "";
        String btn_yes = "";
        String btn_no = "";

        //ユーザーレベル算出

        if (_language.equals("ja")) {

            pop_title += "連続再生を停止しました";
            pop_message += "\n\n" +
                    "継続する場合は「PLAY」を押して下さい" +
                    "\n\n\n連続再生回数を増やす場合は「TIPS」を確認下さい" +
                    "\n\n\n※現在の連続再生回数 : "+db_data1+"回"+"\n\n\n\n";

            btn_yes += "確認";
        }
        else{
            pop_title += "Continuous playback has stopped !!";
            pop_message += "\n\n" +
                    "Press [PLAY] to continue." +
                    "\n\n\nPlease check [TIPS] to increase the continuous playback COUNT." +
                    "\n\n\nCurrent COUNT  [ "+db_data1+" ]"+"\n\n\n\n";

            btn_yes += "O K";
        }

        //メッセージ
        vmessage.setText(pop_message);
        vmessage.setBackgroundColor(Color.DKGRAY);
        vmessage.setTextColor(Color.WHITE);
        vmessage.setTextSize(17);

        //タイトル
        guide.setTitle(pop_title);
        guide.setIcon(R.drawable.timeup);
        guide.setView(vmessage);

        guide.setPositiveButton(btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ImageShow();
            }
        });

        guide.create();
        guide.show();
    }

    /**
     *  DB（データベース）関連の処理
     *
     */
    /* DB初期設定 */
    public void AppDBInitRoad() {
        int data = 0;
        int data1 = 0;
        int data2 = 0;
        int data3 = 0;
        int data4 = 0;
        int data5 = 0;
        int normal = 0;
        int emergency = 0;
        int interval = 0;
        int volume1 = 0;
        int volume2 = 0;
        int light1 = 0;
        int light2 = 0;
        int data6 = 0;
        int data7 = 0;
        int data8 = 0;
        int data9 = 0;
        int data10 = 0;

        SQLiteDatabase db = helper.getReadableDatabase();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT");
        sql.append(" level");
        sql.append(" ,data1");
        sql.append(" ,data2");
        sql.append(" ,data3");
        sql.append(" ,data4");
        sql.append(" ,data5");
        sql.append(" ,normal");
        sql.append(" ,emergency");
        sql.append(" ,interval");
        sql.append(" ,volume1");
        sql.append(" ,volume2");
        sql.append(" ,light1");
        sql.append(" ,light2");
        sql.append(" ,data6");
        sql.append(" ,data7");
        sql.append(" ,data8");
        sql.append(" ,data9");
        sql.append(" ,data10");
        sql.append(" FROM appinfo;");
        try {
            Cursor cursor = db.rawQuery(sql.toString(), null);
            //TextViewに表示
            StringBuilder text = new StringBuilder();
            if (cursor.moveToNext()) {
                data = cursor.getInt(0);
                data1 = cursor.getInt(1);
                data2 = cursor.getInt(2);
                data3 = cursor.getInt(3);
                data4 = cursor.getInt(4);
                data5 = cursor.getInt(5);
                normal = cursor.getInt(6);
                emergency = cursor.getInt(7);
                interval = cursor.getInt(8);
                volume1 = cursor.getInt(9);
                volume2 = cursor.getInt(10);
                light1 = cursor.getInt(11);
                light2 = cursor.getInt(12);
                data6 = cursor.getInt(13);
                data7 = cursor.getInt(14);
                data8 = cursor.getInt(15);
                data9 = cursor.getInt(16);
                data10 = cursor.getInt(17);
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
            insertValues.put("normal", 0);
            insertValues.put("emergency", 0);
            insertValues.put("interval", 0);
            insertValues.put("volume1", 3);
            insertValues.put("volume2", 0);
            insertValues.put("light1", 0);
            insertValues.put("light2", 0);
            insertValues.put("data6", 0);
            insertValues.put("data7", 0);
            insertValues.put("data8", 0);
            insertValues.put("data9", 0);
            insertValues.put("data10", 0);
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
            db_data1 = data1;
            db_data2 = data2;
            db_data3 = data3;
            db_data4 = data4;
            db_data5 = data5;
            db_normal = normal;
            db_emergency = emergency;
            db_interval = interval;
            db_volume1 = volume1;
            db_volume2 = volume2;
            db_light1 = light1;
            db_light2 = light2;
            db_data6 = data6;
            db_data7 = data7;
            db_data8 = data8;
            db_data9 = data9;
            db_data10 = data10;
            Toast.makeText(this, "Data Loading...  Access Level:" + db_user_lv, Toast.LENGTH_SHORT).show();
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
        insertValues.put("normal", db_normal);
        insertValues.put("emergency", db_emergency);
        insertValues.put("interval", db_interval);
        insertValues.put("volume1", db_volume1);
        insertValues.put("volume2", db_volume2);
        insertValues.put("light1", db_light1);
        insertValues.put("light2", db_light2);
        insertValues.put("data6", db_data6);
        insertValues.put("data7", db_data7);
        insertValues.put("data8", db_data8);
        insertValues.put("data9", db_data9);
        insertValues.put("data10", db_data10);
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
     * 日付取得処理
     *
     */
    public int getNowDate() {
        Calendar cal = Calendar.getInstance();
        int temp_date = 0;
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        temp_date += year*10000;
        temp_date += month*100;
        temp_date += day;
//        Toast.makeText(this, "date="+temp_date, Toast.LENGTH_SHORT).show();
        return temp_date;
    }

    /**
     * タイマータスク派生クラス
     * run()に定周期で処理したい内容を記述
     *
     */
    public class MainTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (countText != null && countText.isPlaying() == false) {
                    if (isRandomMode == true && play_random_delay > 0) {
                        play_random_delay -= 100;
                        Thread.sleep(100);
                        if (play_random_delay <= 0) {
                            play_random_delay = 0;
                        }
                        else{
                            return;
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //ここに定周期で実行したい処理を記述します
            mHandler.post(new Runnable() {
                public void run() {

                    //BGMタイマー起動
                    if (countText != null && countText.isPlaying() == false) {
                        if (isRandomMode == true && play_random_delay > 0){
                            //ランダムタイムアップまで待つ;
                        }
                        else {
                            playcount++;
                            if (playcount <= db_data1) {
                                countText.start();
                                if (isRandomMode == true && isEmergencyMode == false) {
                                    play_random_delay = (new Random().nextInt(15) + 8) * 1000;
                                }
                                ImageShow();
                            } else {
                                playcount = 0;
                                soundStop(1);
                                soundStop(2);
                                soundStop(3);
                                ImageShow();
                                // タイムアップのダイアログ表示
                                // TODO:
                                TimeUpPopup();
                            }
                        }
                    }
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
        //動画
        //mRewardedVideoAd.resume(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v("LifeCycle", "------------------------------>onPause");
        //  DB更新
        AppDBUpdated();
        //mRewardedVideoAd.pause(this);
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
        //  DB更新
        AppDBUpdated();
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

        //  DB更新
        AppDBUpdated();
        //動画
        //mRewardedVideoAd.destroy(this);
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


    private void ShowRatingPopup() {

        String str_ttl = "";
        String str_mess = "";
        String str_btn_ok = "";
        String str_btn_ng = "";

        //アプリを起動して 7回目の時
        if (db_data4 != REVIEW_POP){
            return;
        }
        else {
            db_data4++; //ポップアップを１回表示にするため、ここでカウントする
        }
        if (_language.equals("ja")) {
            str_ttl = "★☆アプリ評価のお願い☆★";
            str_mess = "\nいつもご利用ありがとうございます\n" +
                    "\nたくさん利用して頂いている貴方にお願いです。アプリを評価してもらませんか？ 評価して頂けると励みになります。" +
                    "\n\n(この通知は今回限りです)" +
                    "\n\n\n";
            str_btn_ok = "評価する";
            str_btn_ng = "　後で　";
        }else{
            str_ttl = " Please rate the app ";
            str_mess = "\nThank you for using it all the time.\n" +
                    "\nThank you to all of you who are using it a lot. Would you like to rate the app? It would be encouraging if you would rate us." +
                    "\n\n(This notification is only for this time)" +
                    "\n\n\n";
            str_btn_ok = "review";
            str_btn_ng = "later";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(str_ttl);
        builder.setMessage(str_mess);
        builder.setPositiveButton(str_btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RedirectToPlayStoreForRating();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(str_btn_ng, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void ShowRatingPopupNG() {
        String str_ttl = "";
        String str_mess = "";
        String str_btn = "";

        if (_language.equals("ja")) {
            str_ttl = "接続に失敗しました";
            str_mess = "\n評価サイトへのアクセスに失敗しました\n" +
                    "\n" +
                    "\n\n" +
                    "\n\n\n";
            str_btn = "確認";
        }
        else{
            str_ttl = "Connection Failed";
            str_mess = "\nFailed to access Site.\n" +
                    "\n" +
                    "\n\n" +
                    "\n\n\n";
            str_btn = "OK";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(str_ttl);
        builder.setMessage(str_mess);
        builder.setPositiveButton(str_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void RedirectToPlayStoreForRating() {
        try {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            ShowRatingPopupNG();
        }
    }

    /****************************************************
        新画面処理
     ***************************************************/

    /* **************************************************
        表示処理
    ****************************************************/
    public void screen_display(){
        /* SEEK */
        if (seek_volume1 == null) {
            seek_volume1 = (SeekBar) findViewById(R.id.seek_volume1);
        }
        if (db_volume1 > 15)  db_volume1 = 15;
        seek_volume1.setProgress(db_volume1);

        /* SPINNER */
        if (sp_sound1 == null) {
            sp_sound1 = (Spinner) findViewById(R.id.sp_sound1);
        }
        if (db_normal > 11)  db_normal = 11 - 1;
        sp_sound1.setSelection(db_normal);  //通常 鈴音

        if (sp_sound2 == null) {
            sp_sound2 = (Spinner) findViewById(R.id.sp_sound2);
        }
        if (db_emergency > 11)  db_emergency = 11 -1;
        sp_sound2.setSelection(db_emergency);   //SOS 雷鳴音

        if (sp_light2 == null) {
            sp_light2 = (Spinner) findViewById(R.id.sp_light2);
        }
        sp_light2.setSelection(db_light2);

        if (sp_interval == null) {
            sp_interval = (Spinner) findViewById(R.id.sp_interval);
        }
        if (db_interval > 10)  db_interval = 10 -1;
        sp_interval.setSelection(db_interval);


        //再生中の表示切り替え
        LinearLayout layout_normal = findViewById(R.id.linearLayout11);
        LinearLayout layout_emer = findViewById(R.id.linearLayout21);
        ImageView img_normal = (ImageView) findViewById(R.id.img_normal);
        ImageView img_emer = (ImageView) findViewById(R.id.img_emergency);

        if (mainTimer1 == null){
            img_normal.setImageResource(R.drawable.bell_off2);
            layout_normal.setBackgroundResource(R.drawable.bak_inactive);
        }
        else{
            img_normal.setImageResource(R.drawable.bell_on2);
            layout_normal.setBackgroundResource(R.drawable.bak_active);
        }
        if (mainTimer3 == null){
            img_emer.setImageResource(R.drawable.sos_off2);
            layout_emer.setBackgroundResource(R.drawable.bak_inactive);
        }
        else{
            img_emer.setImageResource(R.drawable.sos_on2);
            layout_emer.setBackgroundResource(R.drawable.bak_active);
        }

        TextView v = (TextView) findViewById(R.id.textView);
        v.setBackgroundTintList(null);
        if(soundIsPlaying() == false){
             if (_language.equals("ja")) {
                v.setText("通常音 or 緊急音を選択して\n「PLAY」をタップして下さい");
            }
            else{
                 v.setText("Select Normal or Emergency Sound\nand tap [PLAY]");
            }
            v.setTextColor(Color.parseColor("black"));
        }
        else{
            if (_language.equals("ja")) {
                v.setText("＊＊注意＊＊ アプリを閉じても\n再生は継続します。停止する場合は\n「STOP」をタップして下さい");
            }
            else{
                v.setText("**NOTICE**\nPlayback stays active after closing.\nTap [STOP] to turn it off.");
            }
            v.setTextColor(Color.parseColor("red"));
        }
    }


    /* **************************************************
        アプリボタン処理
    ****************************************************/
    public void toggleSelect(){
        toggle_normal = (ToggleButton) findViewById(R.id.toggle_normal);
        toggle_emergency = (ToggleButton) findViewById(R.id.toggle_emergency);

        toggle_normal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggle_emergency.setChecked(false);
                    soundStart(1,  0);
                } else {
                    soundStop(1);
                }
                screen_display();
            }
        });

        toggle_emergency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toggle_normal.setChecked(false);
                    soundStart(3, 0);
                } else {
                    soundStop(2);
                }
                screen_display();
            }
        });

        screen_display();
    }

    public boolean soundIsPlaying(){

        if (this.mainTimer1 != null){
            return true;
        }
        if (this.mainTimer2 != null){
            return true;
        }
        if (this.mainTimer3 != null){
            return true;
        }
        return false;
    }

    public void seekSelect(){
        //  通常音の音量
        seek_volume1 = (SeekBar)findViewById(R.id.seek_volume1);
        seek_volume1.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    //ツマミをドラッグした時
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (soundIsPlaying() == false) {
                            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                            db_volume1 = seekBar.getProgress();
                            int tmp_volume = db_volume1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                tmp_volume = tmp_volume * 2;  //30段階になったため
                            }
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, tmp_volume, 0);
                        }
                        screen_display();
                    }
                    //ツマミに触れた時
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    //ツマミを離した時
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );
    }

    public void spinnerSelect(){

        //  スピナー（通常音）
        sp_sound1 = (Spinner)findViewById(R.id.sp_sound1);
        sp_sound1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                if (soundIsPlaying() == false){
                    db_normal = position;
                }
                screen_display();
            }
        });
        //  スピナー（SOS音）
        sp_sound2 = (Spinner)findViewById(R.id.sp_sound2);
        sp_sound2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                if (soundIsPlaying() == false) {
                    db_emergency = position;
                }
                screen_display();
            }
        });

        //  スピナー（通常ライト）
        sp_light2 = (Spinner)findViewById(R.id.sp_light2);
        sp_light2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                if (soundIsPlaying() == false) {
                    db_light2 = position;
                }
                screen_display();
            }
        });

        //  スピナー（再生間隔ライト）
        sp_interval = (Spinner)findViewById(R.id.sp_interval);
        sp_interval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //何も選択されなかった時の動作
            @Override
            public void onNothingSelected(AdapterView adapterView) {
            }
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                if (soundIsPlaying() == false) {
                    db_interval = position;
                }
                screen_display();
            }
        });
        screen_display();
    }

    public void soundSelect(int type){

        switch (type){
            default:
                this.countText = null;
            case 0:
                this.countText = null;
                break;
            case 1:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.bell_1);
                break;
            case 2:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.bell_2);
                break;
            case 3:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.bell_3);
                break;
            case 4:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.thunder_1);
                break;
            case 5:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.thunder_2);
                break;
            case 6:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.thunder_3);
                break;
            case 7:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.firecracker);
                break;
            case 8:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.firework);
                break;
            case 9:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.wolf);
                break;
            case 10:
                this.countText = (MediaPlayer) MediaPlayer.create(this, R.raw.peco);
                break;
        }
    }

    public int soundInterval(int type) {

        switch (type) {
            case 0:
                return INTERVAL_0;
            case 1:
                return INTERVAL_1;
            case 2:
                return INTERVAL_3;
            case 3:
                return INTERVAL_5;
            case 4:
                return INTERVAL_7;
            case 5:
                return INTERVAL_10;
            case 6:
                return INTERVAL_15;
            case 7:
                return INTERVAL_20;
            case 8:
                return INTERVAL_30;
            case 9:
                return INTERVAL_RANDUM;
        }
        return INTERVAL_0;
    }


}