package com.drawsome.drawing;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.drawsome.R;
import com.drawsome.UiFlow.Difficulty.DifficultySecondUserActivity;
import com.drawsome.bluetooth.SingletonBluetoothSocket;

import java.util.UUID;

/**
 * This activity presents the user with the canvas to draw and the tools to draw.
 * Authors: Pooja Kanchan and Syed Omer Salar Khureshi
 */
public class DrawingActivity extends Activity implements View.OnClickListener{

    DrawView mView;
    private ImageView currPaint, eraseBtn, newBtn, saveBtn, drawBtn, brushBtn;
    private Spinner brush;
    private int brushDefSize;
    private int eraserSize;
    private float smallBrush, mediumBrush, largeBrush, xlBrush, xxlBrush;
    private final int waitTime = 3;
    private final int eastTimeToGuess =3;
    private final int mediumTimeToGuess =4;
    private final int hardTimeToGuess =5;
    private String word;
    CountDownTimer timer;
    private int LEVEL_EASY =1;
    private int LEVEL_MEDIUM =2;
    private int LEVEL_HARD =3;
    private int level = LEVEL_EASY;
    LinearLayout linearLayout;
    RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the actions bar for full screen viewing.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_drawing);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String wordToGuess = extras.getString("wordToGuess");
            String[] words = wordToGuess.split(";");
            word = words[0];
            if(words[1] != null){
                level = Integer.parseInt(words[1]);
            }
            if(word != null){
                TextView textView = (TextView)findViewById(R.id.drawing_text);
                textView.setText("You are drawing: " + word);
            }
        }

        linearLayout = (LinearLayout)findViewById(R.id.background);
        relativeLayout = (RelativeLayout)findViewById(R.id.rel_background);
        mView = (DrawView) findViewById(R.id.draw);
        mView.setMmSocket(SingletonBluetoothSocket.getBluetoothSocketInstance().getMmSocket());
        mView.startThread();
        mView.setTouchable(true);
        //sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        xlBrush = getResources().getInteger(R.integer.xl_size);
        xxlBrush = getResources().getInteger(R.integer.xxl_size);
        brushDefSize = R.integer.medium_size;

        setTimer();

     //   progressBar.onDrawForeground();
        //brush button
       // brush = (Spinner) findViewById(R.id.brushes_spinner);
       // MyAdapterBrushSize myAdapterBrushSize = new MyAdapterBrushSize(getApplicationContext(), R.layout.spinner_view_brush_size, new String[] {"50"});
       // brush.setAdapter(myAdapterBrushSize);

        //eraser button
      //  brush = (Spinner) findViewById(R.id.eraser_spinner);
      //  MyAdapterEraserSize myAdapterEraserSize = new MyAdapterEraserSize(getApplicationContext(), R.layout.spinner_view_brush_size, new String[] {"50"});
      //  brush.setAdapter(myAdapterEraserSize);

        //draw button
        drawBtn = (ImageView)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        //brush button
        brushBtn = (ImageView)findViewById(R.id.draw_brush);
        brushBtn.setOnClickListener(this);

        //set initial size
        mView.setBrushSize(mediumBrush);

        //erase button
        eraseBtn = (ImageView)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        //new button
        newBtn = (ImageView)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //save button
        saveBtn = (ImageView)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

    }
    public void onLargeBrushClick(View v) {
        mView.setStrokeWidth(30);
    }
    public void onMedBrushClick(View v) {
        mView.setStrokeWidth(20);
    }
    public void onSmallBrushClick(View v) {
        mView.setStrokeWidth(10);
    }

    public void setColor(View v) {
     if(v instanceof ImageButton) {
         ImageButton img = (ImageButton) v;
         ColorDrawable colorDrawable = (ColorDrawable)img.getBackground();
         mView.setColor(colorDrawable.getColor());
     }

    }


    private void setTimer(){
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBarDrawing);
        // progressBar.setProgress(10);
        progressBar.setIndeterminate(false);



        final TextView timerText = (TextView)findViewById(R.id.time_text);
        final int endMin;
        final int endSec = 0;
        if(level == LEVEL_EASY){
            endMin = eastTimeToGuess;
        } else if(level == LEVEL_MEDIUM){
            endMin = mediumTimeToGuess;
        } else{
            endMin = hardTimeToGuess;
        }
        timer = new CountDownTimer((endMin*60 + endSec + waitTime) * 1000,500){
            int min =endMin;
            int sec =endSec;
            boolean flag = false;
            @Override
            public void onTick(long millisUntilFinished) {

                if (flag || (min == 0 && sec < 10)) {
                    if(!flag){
                        // set red color for last 10 seconds and make i flashing
                        timerText.setTextColor(Color.parseColor("red"));
                        timerText.setVisibility(View.INVISIBLE);
                    } else {
                        sec--;
                        if (sec <= -1) {
                            min--;
                            sec = 59;
                        }

                        // when time is up, show screen and start audio
                        if (min == 0 && sec == 0) {
                            setContentView(R.layout.time_is_up);
                            MediaPlayer player = MediaPlayer.create(DrawingActivity.this,R.raw.negative);
                            player.start();

                            Log.d("counter ", "finished");
                        }

                        // set label to show time left
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

            /**
             * onFinish callback is used to stop thread and invoke the DifficultySecondUserActivity activity
             */
            @Override
            public void onFinish(){
                // start new activity: difficultySecondActivity
                Log.d("counter ","finished");
                mView.stopThreads();
                Intent intent = new Intent(getApplicationContext(), DifficultySecondUserActivity.class);
                startActivity(intent);

            }
        }.start();

        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration((endMin*60 + endSec) * 1000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) { }

            @Override
            public void onAnimationEnd(Animator animator) {
                //do something when the countdown is complete
                Log.d("Progressbar "," Animation complete");
            }

            @Override
            public void onAnimationCancel(Animator animator) { }

            @Override
            public void onAnimationRepeat(Animator animator) { }
        });
        animation.start();



    }


    /**
     * Class handles UI events related to brush size.
     */
    public class MyAdapterBrushSize extends ArrayAdapter<String> {

        String[] sObj;

        public MyAdapterBrushSize(Context context, int resource, String[] objs) {
            super(context, resource, objs);
            sObj = objs;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view= getLayoutInflater().inflate(R.layout.spinner_view_brush_size, parent,false);
            return view;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row=inflater.inflate(R.layout.spinner_item_brush_size, parent, false);
            final TextView label= (TextView) row.findViewById(R.id.brushes_spinnerItem_textView);
            label.setText(String.valueOf(brushDefSize));

            SeekBar seekBar = (SeekBar) row.findViewById(R.id.brushes_spinnerItem_seekBar);
            seekBar.setProgress(brushDefSize);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    label.setText(String.valueOf(progress));
                    mView.setErase(false);
                    mView.setBrushSize(progress);
                    brushDefSize = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            return row;
        }
    }

    /*
    * Class which handles UI events of eraser.
     */
    public class MyAdapterEraserSize extends ArrayAdapter<String> {

        String[] sObj;

        public MyAdapterEraserSize(Context context, int resource, String[] objs) {
            super(context, resource, objs);
            sObj = objs;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view= getLayoutInflater().inflate(R.layout.spinner_view_eraser, parent,false);
            return view;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row=inflater.inflate(R.layout.spinner_item_eraser, parent, false);
            final TextView label= (TextView) row.findViewById(R.id.eraser_spinnerItem_textView);
            label.setText(String.valueOf(brushDefSize));

            SeekBar seekBar = (SeekBar) row.findViewById(R.id.eraser_spinnerItem_seekBar);
            seekBar.setProgress(brushDefSize);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    label.setText(String.valueOf(progress));
                    mView.setErase(true);
                    mView.setBrushSize(progress);
                    brushDefSize = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            return row;
        }
    }


    /*
    * Method which handles UI event of color selection.
     */
    public void pickColor (View view) {
        HSVColorPickerDialog cpd = new HSVColorPickerDialog( this, 0xFF4488CC, new HSVColorPickerDialog.OnColorSelectedListener() {
            @Override
            public void colorSelected(Integer color) {
                // Do something with the selected color
                float[] colorHsv = { 0f, 0f, 1f };
                int test = 1;
                //drawView.setColor(test, colorHsv);
                Toast.makeText(getApplicationContext(), "Color: " + color, Toast.LENGTH_SHORT).show();
                mView.setColor(color);
                linearLayout.setBackgroundColor(color);
                relativeLayout.setBackgroundColor(color);
            }
        });
        cpd.setTitle( "Pick a color" );
        cpd.show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    //user clicked paint
    public void paintClicked(View view){
        //use chosen color

        //set erase false
        mView.setErase(false);
        mView.setBrushSize(mView.getLastBrushSize());

        if(view!=currPaint){
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            mView.setColor(Integer.parseInt(color));
            //update ui
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed, getTheme()));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint, getTheme()));
            currPaint=(ImageButton)view;
        }
    }

    @Override
    public void onClick(View view){

        if(view.getId()==R.id.draw_btn){
            //draw button clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //listen for clicks on size buttons
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(smallBrush);
                    mView.setLastBrushSize(smallBrush);
                    mView.setAlpha(255);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(mediumBrush);
                    mView.setLastBrushSize(mediumBrush);
                    mView.setAlpha(255);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(largeBrush);
                    mView.setLastBrushSize(largeBrush);
                    mView.setAlpha(255);
                    brushDialog.dismiss();
                }
            });
            ImageButton xlBtn = (ImageButton)brushDialog.findViewById(R.id.xl_brush);
            xlBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(xlBrush);
                    mView.setLastBrushSize(xlBrush);
                    mView.setAlpha(255);
                    brushDialog.dismiss();
                }
            });
            ImageButton xxlBtn = (ImageButton)brushDialog.findViewById(R.id.xxl_brush);
            xxlBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(xxlBrush);
                    mView.setLastBrushSize(xxlBrush);
                    mView.setAlpha(255);
                    brushDialog.dismiss();
                }
            });

            //show and wait for user interaction
            brushDialog.show();
        }
        else if(view.getId()==R.id.draw_brush){
            //draw button clicked
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Select size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //listen for clicks on size buttons
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(smallBrush);
                    mView.setLastBrushSize(smallBrush);
                    mView.setAlpha(125);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(mediumBrush);
                    mView.setLastBrushSize(mediumBrush);
                    mView.setAlpha(125);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(largeBrush);
                    mView.setLastBrushSize(largeBrush);
                    mView.setAlpha(125);
                    brushDialog.dismiss();
                }
            });
            ImageButton xlBtn = (ImageButton)brushDialog.findViewById(R.id.xl_brush);
            xlBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(xlBrush);
                    mView.setLastBrushSize(xlBrush);
                    mView.setAlpha(125);
                    brushDialog.dismiss();
                }
            });
            ImageButton xxlBtn = (ImageButton)brushDialog.findViewById(R.id.xxl_brush);
            xxlBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(false);
                    mView.setBrushSize(xxlBrush);
                    mView.setLastBrushSize(xxlBrush);
                    mView.setAlpha(125);
                    brushDialog.dismiss();
                }
            });

            //show and wait for user interaction
            brushDialog.show();
        }
        else if(view.getId()==R.id.erase_btn){
            //switch to erase - choose size
            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Eraser size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            //size buttons
            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(true);
                    mView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(true);
                    mView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(true);
                    mView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton xlBtn = (ImageButton)brushDialog.findViewById(R.id.xl_brush);
            xlBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(true);
                    mView.setBrushSize(xlBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton xxlBtn = (ImageButton)brushDialog.findViewById(R.id.xxl_brush);
            xxlBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mView.setErase(true);
                    mView.setBrushSize(xxlBrush);
                    brushDialog.dismiss();
                }
            });
            brushDialog.show();
        }
        else if(view.getId()==R.id.new_btn){
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    mView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        }
        else if(view.getId()==R.id.save_btn){
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                    mView.setDrawingCacheEnabled(true);
                    //attempt to save
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), mView.getDrawingCache(),
                            UUID.randomUUID().toString()+".png", "drawing");
                    //feedback
                    if(imgSaved!=null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    mView.destroyDrawingCache();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();
        }
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setMessage("Give up?");
        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                mView.sendGiveUpMessage();
                timer.cancel();
                new CountDownTimer(4000,1000){
                    boolean flagViewSet = true;
                    @Override
                    public void onTick(long millisUntilFinished){
                        if(flagViewSet) {
                            setContentView(R.layout.give_up);
                            MediaPlayer player = MediaPlayer.create(DrawingActivity.this, R.raw.give_up);
                            player.start();
                            flagViewSet = false;
                        }
                    }

                    @Override
                    public void onFinish(){
                        //set the new Content of your activity
                        mView.stopThreads();
                        finish();
                    }
                }.start();



            }
        });
        newDialog.setNegativeButton("No", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();

            }
        });
        newDialog.show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("DrawingActivity", "canceling timer");
        timer.cancel();
    }
}