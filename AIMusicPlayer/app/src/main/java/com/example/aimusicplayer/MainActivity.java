package com.example.aimusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    private RelativeLayout parentRelativeLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private ImageView pausePlayBtn, nextBtn, previousBtn;
    private TextView songNameTxt;


    private ImageView imageView;
    private RelativeLayout lowerRelativeLayout;
    private Button voiceEnabledBtn;
    private  String mode ="ON";

    private MediaPlayer myMediaPlayer;
    private int position;
    private  ArrayList<File> mySongs;
    private String mSongName;
    private ProgressBar progressBar;
    private SeekBar seekBar;
    private Handler mHandler = new Handler();
    private TextView timeText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkVoiceCommandPermission();

        pausePlayBtn = findViewById(R.id.play_pause_btn);
        nextBtn= findViewById(R.id.next_btn);
        previousBtn= findViewById(R.id.previous_btn);
        imageView = findViewById(R.id.logo);
        seekBar = findViewById(R.id.seekBar);
        timeText = findViewById(R.id.time_text);





        lowerRelativeLayout= findViewById(R.id.lower);
        voiceEnabledBtn= findViewById(R.id.voice_enabled_btn);
        songNameTxt = findViewById(R.id.songName);

        parentRelativeLayout =findViewById(R.id.parentRelativeLayout);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        validateReceiveValuesAndStartPlaying();
        imageView.setBackgroundResource(R.drawable.logo);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matchesFound = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matchesFound != null && !matchesFound.isEmpty()) {
                    if (mode.equals("ON")){
                        keeper = matchesFound.get(0);
                        if (keeper.equals("pause the song") || keeper.equals("pause"))
                        {
                            playPauseSong();
                            Toast.makeText(MainActivity.this, "Command" + keeper, Toast.LENGTH_SHORT).show();

                        }
                        else if (keeper.equals("play the song")|| keeper.equals("play"))
                        {

                            playPauseSong();
                            Toast.makeText(MainActivity.this, "Command recognized" + keeper, Toast.LENGTH_SHORT).show();

                        }
                        else if (keeper.equals("play next song")|| keeper.equals("next"))
                        {

                             playNextSong();
                            Toast.makeText(MainActivity.this, "Command recognized" + keeper, Toast.LENGTH_SHORT).show();

                        }
                        else if (keeper.equals("play previous song")|| keeper.equals("previous"))
                        {

                            playPreviousSong();
                            Toast.makeText(MainActivity.this, "Command recognized" + keeper, Toast.LENGTH_SHORT).show();

                        }
                        else if (keeper.equals("increase volume")) {
                            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 3, AudioManager.FLAG_SHOW_UI);
                            Toast.makeText(MainActivity.this, "Command recognized: Increase volume", Toast.LENGTH_SHORT).show();
                        }
                        else if (keeper.equals("decrease volume")) {
                            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 3, AudioManager.FLAG_SHOW_UI);
                            Toast.makeText(MainActivity.this, "Command recognized: Decrease volume", Toast.LENGTH_SHORT).show();
                        }
                        else if (keeper.equals("play random song") || keeper.equals("random"))
                        {
                            playRandomSong();
                            Toast.makeText(MainActivity.this, "Command recognized: Play random song", Toast.LENGTH_SHORT).show();
                        }
                        else if (keeper.contains("search for")) {
                            String keyword = keeper.replace("search for", "");
                            searchAndPlaySong(keyword);
                        }



                        Toast.makeText(MainActivity.this, "Result " + keeper, Toast.LENGTH_SHORT).show();
                    }



                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });



        parentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        keeper= "";
                        break;

                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;

                }
                return false;
            }
        });


        voiceEnabledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
               if(mode.equals("ON"))
               {
                 mode="OFF";
                 voiceEnabledBtn.setText("Voice Enabled Mode - OFF");
                 lowerRelativeLayout.setVisibility(View.VISIBLE);
               }
               else
               {
                   mode="ON";
                   voiceEnabledBtn.setText("Voice Enabled Mode - ON");
                   lowerRelativeLayout.setVisibility(View.GONE);
               }

            }
        });

        pausePlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               playPauseSong();
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myMediaPlayer.getCurrentPosition()>0){
                    playPreviousSong();
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myMediaPlayer.getCurrentPosition()>0){
                  playNextSong();
                }
            }
        });









    }

    private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (myMediaPlayer != null) {
                int mCurrentPosition = myMediaPlayer.getCurrentPosition();
                seekBar.setProgress(mCurrentPosition);
                // Update the time text
                int currentMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);

                int currentSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                        - (int)TimeUnit.MINUTES.toSeconds(currentMinutes);
                int totalMinutes =(int) TimeUnit.MILLISECONDS.toMinutes(myMediaPlayer.getDuration());
                int totalSeconds =(int) TimeUnit.MILLISECONDS.toSeconds(myMediaPlayer.getDuration())
                        - (int) TimeUnit.MINUTES.toSeconds(totalMinutes);
                String currentTime = String.format(Locale.getDefault(), "%d:%02d", currentMinutes, currentSeconds);
                String totalTime = String.format(Locale.getDefault(), "%d:%02d", totalMinutes, totalSeconds);
                timeText.setText(currentTime + " / " + totalTime);
            }
            mHandler.postDelayed(this, 50);
        }
    };





    private  void validateReceiveValuesAndStartPlaying (){

        if (myMediaPlayer != null)
        {
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();


        mySongs = (ArrayList) bundle.getParcelableArrayList("song");
        mSongName =mySongs.get(position).getName();
        String songName = intent.getStringExtra("name");

        songNameTxt.setText(songName);
        songNameTxt.setSelected(true);

        position = bundle.getInt("position", 0);
        Uri uri = Uri.parse(mySongs.get(position).toString());

        myMediaPlayer =MediaPlayer.create(MainActivity.this, uri);
        myMediaPlayer.start();

        seekBar.setMax(myMediaPlayer.getDuration());
        mHandler.postDelayed(mUpdateSeekBar, 50);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    myMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });




    }





    private void checkVoiceCommandPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    private void playPauseSong (){

        imageView.setBackgroundResource(R.drawable.four);

        if (myMediaPlayer.isPlaying()){
            pausePlayBtn.setImageResource(R.drawable.play);
            myMediaPlayer.pause();
            mHandler.removeCallbacks(mUpdateSeekBar);
        }
        else{
            pausePlayBtn.setImageResource(R.drawable.pause);
            myMediaPlayer.start();
            mHandler.postDelayed(mUpdateSeekBar, 50);


            imageView.setBackgroundResource(R.drawable.five);
        }

    }

    private void playNextSong(){
        if(mySongs == null || mySongs.isEmpty()){
            Toast.makeText(MainActivity.this,"No songs found", Toast.LENGTH_SHORT).show();
            return;
        }
        if(myMediaPlayer!=null){
            myMediaPlayer.pause();
            myMediaPlayer.stop();
            myMediaPlayer.reset();
            myMediaPlayer.release();
        }

        position = ((position+1)%mySongs.size());
        Uri uri = Uri.fromFile(mySongs.get(position));


        mSongName = mySongs.get(position).getName();
        songNameTxt.setText(mSongName);

        myMediaPlayer = new MediaPlayer();
        try {
            myMediaPlayer.setDataSource(MainActivity.this, uri);
            myMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        myMediaPlayer.start();


        imageView.setBackgroundResource(R.drawable.three);

        if (myMediaPlayer.isPlaying()){
            pausePlayBtn.setImageResource(R.drawable.pause);


        }
        else{
            pausePlayBtn.setImageResource(R.drawable.play);


        }
    }


    private void playPreviousSong(){

        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        position = ((position-1)<0?(mySongs.size()-1):(position-1));
        Uri uri = Uri.parse(mySongs.get(position).toString());

        myMediaPlayer = MediaPlayer.create(MainActivity.this, uri);

        mSongName = mySongs.get(position).getName();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();

        imageView.setBackgroundResource(R.drawable.two);

        if (myMediaPlayer.isPlaying()){
            pausePlayBtn.setImageResource(R.drawable.pause);

        }
        else{
            pausePlayBtn.setImageResource(R.drawable.play);

        }



    }

    private void playRandomSong(){
        if(mySongs == null || mySongs.isEmpty()){
            Toast.makeText(MainActivity.this,"No songs found", Toast.LENGTH_SHORT).show();
            return;
        }
        if(myMediaPlayer!=null){
            myMediaPlayer.pause();
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }

        position = new Random().nextInt(mySongs.size());
        Uri uri = Uri.fromFile(mySongs.get(position));

        mSongName = mySongs.get(position).getName();
        songNameTxt.setText(mSongName);

        myMediaPlayer = MediaPlayer.create(MainActivity.this, uri);
        myMediaPlayer.start();

        imageView.setBackgroundResource(R.drawable.three);

        if (myMediaPlayer.isPlaying()){
            pausePlayBtn.setImageResource(R.drawable.pause);
        }
        else{
            pausePlayBtn.setImageResource(R.drawable.play);
        }

    }
    private void searchAndPlaySong(String keyword) {
        for (File song: mySongs) {
            if (song.getName().toLowerCase().contains(keyword.toLowerCase())) {
                // Play the song
                myMediaPlayer = new MediaPlayer();
                try {
                    myMediaPlayer.setDataSource(song.getPath());
                    myMediaPlayer.prepare();
                    myMediaPlayer.start();
                    position = mySongs.indexOf(song);

                    // Update the UI
                    songNameTxt.setText(song.getName().replace(".mp3", ""));
                    pausePlayBtn.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.pause));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }



}







