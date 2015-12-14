package com.drawsome.drawing;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drawsome.R;
import com.drawsome.UiFlow.Difficulty.DifficultyActivity;
import com.drawsome.UiFlow.Difficulty.DifficultySecondUserActivity;
import com.drawsome.bluetooth.ConnectedThread;
import com.drawsome.bluetooth.ConnectedThreadSingleton;
import com.drawsome.bluetooth.SingletonBluetoothSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
 * The activity which handles UI events of receiver side. It replicates the drawing from sender
 * side on drawing canvas and provides a user shuffled words, which he can select and guess the word.
 * Created by pooja on 10/15/2015.
 */
public class ViewDrawingActivity extends Activity {

    DrawView mView;
    List<Integer> letterPlace = new ArrayList<>();
    String word = "cat";

    private final int waitTime = 3;
    private int level =1;
    private final int eastTimeToGuess =3;
    private final int mediumTimeToGuess =4;
    private final int hardTimeToGuess =5;

    List<Character> currentWord = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drawing);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String wordToGuess = extras.getString("wordToGuess");
            String[] words = wordToGuess.split(";");
            word = words[0];
            if(words[1] != null){
                level = Integer.parseInt(words[1]);
            }
        }
        System.out.println(" word " + word + " level " + level);
        if(word == null){
           System.out.println(" word null!");
            Toast.makeText(getApplicationContext(),"Something went wrong,Please relaunch the application",Toast.LENGTH_SHORT);
            finish();
        }
        setTimer();
        ConnectedThread connectedThread = ConnectedThreadSingleton.getConnectedThreadInstance().getConnectedThread();
        //connectedThread.write("Ending thread");
        if(connectedThread!= null)
         connectedThread.interrupt();

        // get a word and add characters to the list.
        List<Character> shuffledList = new ArrayList<>();
        for(char c:word.toCharArray()){
            shuffledList.add(c);
        }
        //generate random characters
        Random r = new Random();
        int numberOfCharsToGenerate = 12 - word.length();
        for(int i =0 ; i < numberOfCharsToGenerate; i++) {
            char c = (char)(r.nextInt(26) + 65);
            System.out.println("Adding " + c);
            shuffledList.add(c);
        }
        // shuffle the words.

        Collections.shuffle(shuffledList);

       // set the shuffled letters as text to the buttons
        int index = 1;
        for(char c : shuffledList){
            int id = getResources().getIdentifier("guess_button" + index,"id","com.drawsome");
           System.out.println("id " + id + " " + "guess_button" + index + " actual " + R.id.guess_button1);
            Button b = (Button)findViewById(id);
            b.setText(String.valueOf(c));
            index++;
        }

        // add buttons to let user guess the word.
        // Number of buttons = length of the word to be gussed

        LinearLayout layout = (LinearLayout) findViewById(R.id.guessWordLayout);
        for(int i = 0 ; i < word.length(); i ++) {
            currentWord.add('@');
            letterPlace.add(-1);
            Button button = new Button(this);
            System.out.println(i);
            button.setId(i);
            DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
            int pixelHeight = (int)(30 * displayMetrics.density + 0.5);
            int pixelWidth =(int)(30 * displayMetrics.density + 0.5);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(pixelWidth,pixelHeight);
            lp.setMargins(10, 10, 10, 10);
            button.setLayoutParams(lp);
            layout.addView(button);
            Button button1 = (Button)findViewById(i);
            button1.setTypeface(Typeface.DEFAULT_BOLD);
            button1.setTextColor((Color.parseColor("#BA55D3")));
            //button1.setText(String.valueOf("C"));
            button1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 27);
            button1.setBackgroundColor(Color.parseColor("#FFFFFF"));
            button1.setPadding(0,0,0,0);
            button.setOnClickListener(new View.OnClickListener() {
              // on click of the button, move the character back to original place.
                // set the button clickable as true.
                @Override
                public void onClick(View v) {
                    Button b = (Button)v;
                        int button_id = b.getId();
                    System.out.println("button clicked: " +button_id);
                    if(b.getText() != "" && letterPlace.get(button_id) != -1) {
                        String resource = "guess_button" + letterPlace.get(button_id);
                        int id = getResources().getIdentifier(resource, "id", "com.drawsome");
                       System.out.println("id " + id);
                        Button replace = (Button) findViewById(id);
                        replace.setClickable(true);
                        replace.setAlpha(1);
                        b.setText("");
                        letterPlace.set(button_id,-1);
                        currentWord.set(button_id,'@');
                        System.out.println("currentWord " + currentWord + "  ");
                        for(int i:letterPlace){
                            System.out.println("i " + i);
                        }
                    }
                }
            });
        }

        mView = (DrawView) findViewById(R.id.viewDraw);
        mView.setClickable(false);
       mView.setMmSocket(SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket());
        mView.startThread();
        mView.setTouchable(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_drawing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * on click, move the character on the button to the empty space available,
     * and set button clickable property false.
     *  Also, if all the positions are filled, check the guessed word is right or not.
     *  If yes, display success image with animation.
     */
    public void guessButtonClicked(View v){
        System.out.println("guessButtonClicked");
        Button button = (Button)v;
        String letter = button.getText().toString();
        String tag = (String)button.getTag();
        int id = Integer.parseInt(tag);
        System.out.println("id " + id);

        int emptyPlace = 0;
        for(int i :letterPlace) {
            if(i == -1) {
                break;
            }
            emptyPlace ++;
        }
        System.out.println("empty" + emptyPlace);
        if(emptyPlace < letterPlace.size()) {
            Button guessButton = (Button)findViewById(emptyPlace);
            System.out.println("setting text " + letter);
            guessButton.setText(letter.toUpperCase());
            button.setAlpha(.5f);
            button.setClickable(false);
            letterPlace.set(emptyPlace, id);
            currentWord.set(emptyPlace,letter.charAt(0));
            StringBuffer current = new StringBuffer();
            for(char c: currentWord) {
                current = current.append(c);
            }
            System.out.println("current word" + current);
            if(word.equalsIgnoreCase(current.toString())) {
                System.out.println("Bingo");
                mView.sendWordGuessedMessage();


                ImageButton img = (ImageButton)findViewById(R.id.checkbox);
              //  img.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                img.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .setListener(null);

               /* try {
                    Thread.sleep(3000);
                }  catch(InterruptedException ie){
                    Log.d("interruptedException", ie.getMessage());
                    ie.printStackTrace();
                }*/
                //display the change user screen during 6 seconds,
               new CountDownTimer(6000,2000){
                   boolean flagViewSet = false;
                   @Override
                    public void onTick(long millisUntilFinished){
                        System.out.println(millisUntilFinished);
                        if(millisUntilFinished < 4500 && !flagViewSet) {
                            flagViewSet = true;
                            setContentView(R.layout.change_user);
                        }
                    }

                    @Override
                    public void onFinish(){
                        //set the new Content of your activity
                        mView.stopThreads();
                        System.out.println("loading activity");
                        Intent intent = new Intent(getApplicationContext(), DifficultyActivity.class);
                        startActivity(intent);
                        //   YourActivity.this.setContentView(R.layout.main);

                    }
                }.start();

            }
        }


    }

    private void setTimer(){


        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBarViewDrawing);
        // progressBar.setProgress(10);
        progressBar.setIndeterminate(false);
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration(5000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) { }

            @Override
            public void onAnimationEnd(Animator animator) {
                //do something when the countdown is complete
                Log.d("Progressbar ", " Animation complete");
            }

            @Override
            public void onAnimationCancel(Animator animator) { }

            @Override
            public void onAnimationRepeat(Animator animator) { }
        });
        animation.start();


        final TextView timerText = (TextView)findViewById(R.id.timer_text);

        final int endMin;
        final int endSec = 0;
        System.out.println("level " + level);
        if(level == 1){
            endMin = eastTimeToGuess;
        } else if(level == 2){
            endMin = mediumTimeToGuess;
        } else{
            endMin = hardTimeToGuess;
        }
        System.out.println("endmin " + endMin);
        new CountDownTimer((endMin*60 + endSec + waitTime) * 1000,500){
            int min =endMin;
            int sec =endSec;
            boolean flag = false;
            @Override
            public void onTick(long millisUntilFinished) {

                if (flag || (min == 0 && sec < 10)) {
                 if(!flag){
                     timerText.setTextColor(Color.parseColor("red"));
                     timerText.setVisibility(View.INVISIBLE);
                 } else {
                     sec--;
                     if (sec <= -1) {
                         min--;
                         sec = 59;
                     }
                     if (min == 0 && sec == 0) {
                         setContentView(R.layout.time_is_up);
                         Log.d("counter ", "finished");
                     }
                     timerText.setVisibility(View.VISIBLE);
                     if (sec < 10) {
                         timerText.setText(min + ":0" + sec);
                     } else {
                         timerText.setText(min + ":" + sec);
                     }
                 }
                }
                flag = !flag;
            }

            @Override
            public void onFinish(){
                Log.d("counter ","finished");
                mView.stopThreads();
                Intent intent = new Intent(getApplicationContext(), DifficultyActivity.class);
                startActivity(intent);

            }
        }.start();



    }


}
