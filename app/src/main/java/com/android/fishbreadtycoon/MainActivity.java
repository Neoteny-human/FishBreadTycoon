package com.android.fishbreadtycoon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private SoundPool mSoundPool;
    private int moneySound, effectSound;

    int time = 60;

    Random rand = new Random();

    int money = 0;

    //가지고 있는 붕어빵 개수.
    int medium = 0;
    int well = 0;

    //손님들이 원하는 미디움 개수.
    int[] mediums = new int[3];

    //손님들이 원하는 웰던 개수.
    int[] wells = new int[3];

    //클릭애니메이션이 작동 중인가?(그동안은 cutomerHandler가 적용되면 안됨.)
    boolean[] isPlaying = {false, false, false};
    boolean isEnd = false;


    ImageView[] customerViews = new ImageView[3];
    ImageView[] moldViews = new ImageView[9];
    View[] ask = new View[3];
    TextView[] askMedium = new TextView[3];
    TextView[] askWell = new TextView[3];

    Animation[] anims = new Animation[9];

    ImageView pump;
    ProgressBar progressBar;


    int[] customerID = {R.id.customer1, R.id.customer2, R.id.customer3};
    int[] moldID = {R.id.mold1, R.id.mold2, R.id.mold3, R.id.mold4, R.id.mold5, R.id.mold6, R.id.mold7, R.id.mold8, R.id.mold9};
    int[] askID = {R.id.ask1, R.id.ask2, R.id.ask3};
    int[] askMediumID = {R.id.ask1medium, R.id.ask2medium, R.id.ask3medium};
    int[] askWellID = {R.id.ask1well, R.id.ask2well, R.id.ask3well};

    int[] animID = {R.anim.moving, R.anim.moving1, R.anim.moving2, R.anim.moving3, R.anim.moving4
            , R.anim.moving5, R.anim.moving6, R.anim.moving7, R.anim.moving8};

    final String TAG_MOLD = "mold";
    final String TAG_RARE = "rare";
    final String TAG_MEDIUM = "medium";
    final String TAG_WELL_DONE = "well_done";
    final String TAG_OVER_COOKED = "over_cooked";

    final String TAG_OFF = "off";
    final String TAG_HAPPY = "happy";
    final String TAG_ANGRY = "angry";
    final String TAG_GRUMPY = "grumpy";
    final String TAG_NEUTRAL = "neutral";

    CookHandler cookHandler = new CookHandler();
    CustomerOnHandler customerOnHandler = new CustomerOnHandler();
    PumpHandler pumpHandler = new PumpHandler();
    AnswerHandler answerHandler = new AnswerHandler();
    NumberHandler numberHandler = new NumberHandler();
    TimerHandler timerHandler = new TimerHandler();


    TextView mediumCount;
    TextView wellCount;
    TextView moneyView;

    Thread timerThread = new Thread(new TimerRunnable());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerThread.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .build();
        } else {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }

        moneySound = mSoundPool.load(getApplicationContext(), R.raw.money, 1);
        effectSound = mSoundPool.load(getApplicationContext(), R.raw.effect, 1);

        mediumCount = findViewById(R.id.mediumCount);
        mediumCount.setText("X " + medium);
        wellCount = findViewById(R.id.wellCount);
        wellCount.setText("X " + well);

        moneyView = findViewById(R.id.moneyView);
        moneyView.setText("₩ " + money);

        progressBar = findViewById(R.id.progressbar);

        pump = findViewById(R.id.pump);
        pump.bringToFront();


        for (int i = 0; i < 9; i++) {
            anims[i] = AnimationUtils.loadAnimation(getApplicationContext(), animID[i]);
        }

        Animation scale = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale);


        for (int i = 0; i < 3; i++) {
            customerViews[i] = findViewById(customerID[i]);
            ask[i] = findViewById(askID[i]);
            askMedium[i] = findViewById(askMediumID[i]);
            askWell[i] = findViewById(askWellID[i]);

            ask[i].setVisibility(View.INVISIBLE);
            customerViews[i].setTag(TAG_OFF);

            int finalI = i;
            customerViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customerViews[finalI].startAnimation(scale);
                    if (medium >= mediums[finalI] && well >= wells[finalI]) {
                        mSoundPool.play(moneySound, 1, 1, 1, 0, 1);
                        new Thread(new AnswerRunnable(finalI)).start();
                        medium -= mediums[finalI];
                        well -= wells[finalI];
                        mediumCount.setText("X " + medium);
                        wellCount.setText("X " + well);
                        money += (mediums[finalI] + wells[finalI]) * 1000;
                        moneyView.setText("₩ " + money);
                    } else {
                        //disappointed
                    }

                }
            });
        }


        for (int i = 0; i < 9; i++) {
            moldViews[i] = findViewById(moldID[i]);
            moldViews[i].setTag(TAG_MOLD);

            int finalI = i;
            moldViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String TAG = v.getTag().toString();
                    switch (TAG) {
                        case TAG_MOLD:
                            mSoundPool.play(effectSound, 1, 1, 1, 0, 1);
                            pump.startAnimation(anims[finalI]);
                            new Thread(new PumpRunnable(finalI)).start();
                            //소스 붓기.

                            break;
                        case TAG_RARE:
                            //너무 빨리 꺼냄.
                            v.setTag(TAG_MOLD);
                            ((ImageView) v).setImageResource(R.drawable.mold);
                            break;
                        case TAG_MEDIUM:
                            //미디움 +1
                            v.setTag(TAG_MOLD);
                            ((ImageView) v).setImageResource(R.drawable.mold);
                            medium += 1;
                            mediumCount.setText("X " + medium);
                            break;
                        case TAG_WELL_DONE:
                            //웰던 +1
                            v.setTag(TAG_MOLD);
                            ((ImageView) v).setImageResource(R.drawable.mold);
                            well += 1;
                            wellCount.setText("X " + well);
                            break;
                        case TAG_OVER_COOKED:
                            //타버렸음.
                            v.setTag(TAG_MOLD);
                            ((ImageView) v).setImageResource(R.drawable.mold);
                            break;
                    }
                }
            });
        }

        for (int i = 0; i < 9; i++) {
            new Thread(new cookRunnable(i)).start();
        }

        for (int i = 0; i < 3; i++) {
            new Thread(new CustomerRunnable(i)).start();
        }


    }


    public class AnswerRunnable implements Runnable {
        int index;

        AnswerRunnable(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            //핸들러에 전달해서 얼굴표정 바꿈.
            Message m = answerHandler.obtainMessage();
            m.arg1 = index;
            m.arg2 = 1;
            answerHandler.sendMessage(m);
            isPlaying[index] = true;


            //200ms 기다린 후 손님 떠남.
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message m2 = answerHandler.obtainMessage();
            m2.arg1 = index;
            m2.arg2 = 2;
            answerHandler.sendMessage(m2);
        }
    }

    public class AnswerHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int index = msg.arg1;
            if (msg.arg2 == 1) {
                String TAG = customerViews[index].getTag().toString();
                switch (TAG) {
                    case TAG_NEUTRAL:
                        customerViews[index].setImageResource(R.drawable.very_happy);
                        break;
                    case TAG_GRUMPY:
                        customerViews[index].setImageResource(R.drawable.happy);
                        break;
                    case TAG_ANGRY:
                        customerViews[index].setImageResource(R.drawable.disappointed);
                        break;
                }
            } else {
                customerViews[index].setTag(TAG_OFF);
                ask[index].setVisibility(View.INVISIBLE);
                isPlaying[index] = false;
            }
        }
    }

    public class PumpRunnable implements Runnable {
        int index;

        PumpRunnable(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = pumpHandler.obtainMessage();
            msg.arg1 = index;
            pumpHandler.sendMessage(msg);
        }
    }

    public class PumpHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            moldViews[msg.arg1].setTag(TAG_RARE);
            ((ImageView) moldViews[msg.arg1]).setImageResource(R.drawable.rare);
        }
    }

    public class cookRunnable implements Runnable {
        int index = 0;

        cookRunnable(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            while (time > 1) {
                try {
                    Thread.sleep(1000);
                    Message message = cookHandler.obtainMessage();
                    message.arg1 = index;
                    cookHandler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class CookHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int index = msg.arg1;
            switch (moldViews[index].getTag().toString()) {
//                case TAG_MOLD:
//                    moldViews[index].setTag(TAG_RARE);
//                    ((ImageView) moldViews[index]).setImageResource(R.drawable.rare);
//                    break;
                case TAG_RARE:
                    moldViews[index].setTag(TAG_MEDIUM);
                    ((ImageView) moldViews[index]).setImageResource(R.drawable.medium);
                    break;
                case TAG_MEDIUM:
                    moldViews[index].setTag(TAG_WELL_DONE);
                    ((ImageView) moldViews[index]).setImageResource(R.drawable.well_done);
                    break;
                case TAG_WELL_DONE:
                    moldViews[index].setTag(TAG_OVER_COOKED);
                    ((ImageView) moldViews[index]).setImageResource(R.drawable.overcooked);
                    break;
                case TAG_OVER_COOKED:
                    break;
            }
        }
    }


    public class NumberHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int index = msg.arg1;
            askMedium[index].setText("X " + mediums[index]);
            askWell[index].setText("X " + wells[index]);
        }
    }


    public class CustomerRunnable implements Runnable {
        int index = 0;

        CustomerRunnable(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            while (time > 1) {
                String TAG = customerViews[index].getTag().toString();
                if (TAG.equals(TAG_OFF)) {
                    try {
                        int offTime = rand.nextInt(5000) + 1000;
                        Thread.sleep(offTime);

                        customerViews[index].setTag(TAG_NEUTRAL);
                        while (true) {
                            wells[index] = rand.nextInt(3);
                            mediums[index] = rand.nextInt(3);
                            if (wells[index] != 0 || mediums[index] != 0) {
                                break;
                            }
                        }
                        Message message2 = numberHandler.obtainMessage();
                        message2.arg1 = index;
                        numberHandler.sendMessage(message2);


                        Message message1 = customerOnHandler.obtainMessage();
                        message1.arg1 = index;
                        message1.arg2 = 1;
                        customerOnHandler.sendMessage(message1);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (TAG.equals(TAG_NEUTRAL) || TAG.equals(TAG_GRUMPY) || TAG.equals(TAG_ANGRY)) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message2 = customerOnHandler.obtainMessage();
                    message2.arg1 = index;
                    message2.arg2 = 2;
                    customerOnHandler.sendMessage(message2);
                }

            }
        }
    }


    public class CustomerOnHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int index = msg.arg1;
            String TAG = customerViews[index].getTag().toString();
            if (msg.arg2 == 2) {
                Log.d(TAG, "커스터머들: 아규2");
                switch (TAG) {
                    case TAG_NEUTRAL:
                        if (!isPlaying[index]) {
                            customerViews[index].setTag(TAG_GRUMPY);
                            ((ImageView) customerViews[index]).setImageResource(R.drawable.grumpy);
                        }
                        break;
                    case TAG_GRUMPY:
                        if (!isPlaying[index]) {
                            customerViews[index].setTag(TAG_ANGRY);
                            ((ImageView) customerViews[index]).setImageResource(R.drawable.furious);
                        }
                        break;
                    case TAG_ANGRY:
                        if (!isPlaying[index]) {
                            customerViews[index].setTag(TAG_OFF);
                            ask[index].setVisibility(View.INVISIBLE);
                            Message m = timerHandler.obtainMessage();
                            timerHandler.sendMessage(m);
                            break;
                        }
                }
            } else {
                Log.d(TAG, "커스터머들: 아규1");
                customerViews[index].setTag(TAG_NEUTRAL);
                ((ImageView) customerViews[index]).setImageResource(R.drawable.neutral);
                ask[index].setVisibility(View.VISIBLE);
            }
        }
    }

    public class TimerRunnable implements Runnable {
        @Override
        public void run() {
            while (time > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message m = timerHandler.obtainMessage();
                timerHandler.sendMessage(m);
            }
        }
    }

    public class TimerHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            time -= 1;
            progressBar.setProgress(time);
            if (time <= 0 && !isEnd) {
                isEnd = true;
                Log.d("타이머 쓰레드", "무한반복/???");
                Intent i = new Intent(MainActivity.this, ResultActivity.class);
                i.putExtra("money", money);
                startActivity(i);
                finish();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //뮤직서비스를 인텐트를 넘기면서 시작.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getApplicationContext(), MusicService.class));
    }


    //홈화면으로 나갔을 때 음악이 꺼질 수 있도록.
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        stopService(new Intent(getApplicationContext(), MusicService.class));
    }


}