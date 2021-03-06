package ua.hneu.languagetrainer.pages.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ua.hneu.edu.languagetrainer.R;
import ua.hneu.languagetrainer.App;
import ua.hneu.languagetrainer.ListViewAdapter;
import ua.hneu.languagetrainer.TextToVoiceMediaPlayer;
import ua.hneu.languagetrainer.model.tests.Answer;
import ua.hneu.languagetrainer.model.tests.Question;
import ua.hneu.languagetrainer.model.tests.Test;
import ua.hneu.languagetrainer.passing.TestPassing;
import ua.hneu.languagetrainer.service.TestService;

public class MockTestActivity extends Activity {
    ListView answersListView;
    TextView titleTextView;
    TextView sectionTextView;
    TextView taskTextView;
    TextView textTextView;
    Button soundButton;
    ImageView img;
    ProgressBar progressBar;
    ImageView isCorrect;
    Button skipSection;
    Chronometer chronometer;
    MediaPlayer player;

    TextToVoiceMediaPlayer ttwmp;

    // custom adapter for ListView
    ListViewAdapter adapter;
    int answersNumber = 5;
    int currentWordNumber = -1;
    boolean isLevelDef = false;
    Test t;
    Question q;
    ArrayList<Answer> answers;
    Answer rightAnswer;
    String testName = null;
    TestPassing tp = App.tp;
    // time limits for each section
    long timeLimits[];
    int currentSection = 1;
    boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        // Initialize
        sectionTextView = (TextView) findViewById(R.id.sectionTextView);
        taskTextView = (TextView) findViewById(R.id.taskTextView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        textTextView = (TextView) findViewById(R.id.textTextView);
        answersListView = (ListView) findViewById(R.id.answersListView);
        progressBar = (ProgressBar) findViewById(R.id.testProgressBar);
        isCorrect = (ImageView) findViewById(R.id.isCorrect);
        skipSection = (Button) findViewById(R.id.buttonSkipSection);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        img = (ImageView) findViewById(R.id.img);
        soundButton = (Button) findViewById(R.id.soundButton);
        player = new MediaPlayer();
        // clear test passing instance to clear previous results
        tp.clearInfo();
        ttwmp = new TextToVoiceMediaPlayer();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            testName = extras.getString("testName");
        }
        // if it's a level definition test hide chronometer
        // else start count and set time limits
        if (!testName.equals("level_def")) {
            timeLimits = App.getTimeTestLimits();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer
                    .setOnChronometerTickListener(new OnChronometerTickListener() {

                        @Override
                        public void onChronometerTick(Chronometer chronometer) {
                            boolean isElapsed = false;
                            long elapsedMillis = SystemClock.elapsedRealtime()
                                    - chronometer.getBase();
                            if (App.userInfo.getLevel() == 1
                                    || App.userInfo.getLevel() == 2) {
                                // if it is 1 or 2 section and time is elapsed
                                if ((currentSection == 1 || currentSection == 2)
                                        && elapsedMillis >= timeLimits[0])
                                    isElapsed = true;
                                // if it is 3 section and time is elapsed
                                if (currentSection == 3
                                        && elapsedMillis >= timeLimits[2])
                                    isElapsed = true;
                            } else {
                                // if it is 1 section and time is elapsed
                                if (currentSection == 1
                                        && elapsedMillis >= timeLimits[0])
                                    isElapsed = true;
                                // if it is 2 section and time is elapsed
                                if (currentSection == 2
                                        && elapsedMillis >= timeLimits[0])
                                    isElapsed = true;
                                // if it is 3 section and time is elapsed
                                if (currentSection == 3
                                        && elapsedMillis >= timeLimits[2])
                                    isElapsed = true;
                            }
                            if (isElapsed) {
                                // show toast, clear timer and go to next
                                // section
                                timeIsOver();
                                chronometer.setBase(SystemClock
                                        .elapsedRealtime());
                                chronometer.start();
                                toNextSection();

                            }
                        }
                    });
        } else {
            chronometer.setVisibility(View.INVISIBLE);
        }

        TestService ts = new TestService();
        // getting and loading test by name

        t = ts.getTestByName(testName, getContentResolver());
        isLevelDef = (t.getName().equals("level_def"));
        // if level defining test - hide skippSection button, because it has no
        // sections
        if (isLevelDef) {
            skipSection.setVisibility(View.INVISIBLE);
            soundButton.setHeight(0);
            soundButton.setVisibility(View.INVISIBLE);
        }
        // at first show word and possible answers
        nextWord();

        titleTextView.setTypeface(App.kanjiFont, Typeface.NORMAL);
        sectionTextView.setTypeface(App.kanjiFont, Typeface.NORMAL);
        taskTextView.setTypeface(App.kanjiFont, Typeface.NORMAL);
        textTextView.setTypeface(App.kanjiFont, Typeface.NORMAL);
        isCorrect.setImageResource(android.R.color.transparent);
    }

    public void timeIsOver() {
        Toast.makeText(this, getString(R.string.your_time_is_over), Toast.LENGTH_LONG).show();
    }

    public void nextWord() {
        ttwmp.stop();
        isCorrect.setImageResource(android.R.color.transparent);
        // move pointer to next word
        currentWordNumber++;
        double progress = ((double) currentWordNumber / (double) t
                .getQuestions().size()) * 100;
        progressBar.setProgress((int) progress);
        if (currentWordNumber >= t.getQuestions().size()) {
            endTesting();
        } else {
            q = t.getQuestions().get(currentWordNumber);
            // setting sections variable - vocabulary, reading and listening
            if (q.getSection().equals("文字・語彙")) {
                soundButton.setHeight(0);
                soundButton.setVisibility(View.INVISIBLE);
                currentSection = 1;
            } else if (q.getSection().equals("読解・文法")) {
                soundButton.setHeight(0);
                soundButton.setVisibility(View.INVISIBLE);
                currentSection = 2;
            } else if (!testName.equals("level_def")) {
                soundButton.setHeight(50);
                soundButton.setVisibility(View.VISIBLE);
                currentSection = 3;
            }
            answers = q.getAnswers();
            titleTextView.setText(q.getText());
            titleTextView.setText(q.getTitle());
            taskTextView.setText(q.getTask());
            textTextView.setText(q.getText());
            // if image for question exists
            if (!q.getImgRef().isEmpty()) {
                InputStream ims;
                try {
                    Log.d("img path: ", "tests/question_img/" + q.getImgRef());
                    ims = getAssets().open("tests/question_img/" + q.getImgRef());
                    // load image as Drawable
                    Drawable d = Drawable.createFromStream(ims, null);
                    // set image to ImageView
                    img.setImageDrawable(d);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }// if audio for question exists
            if (!q.getAudioRef().isEmpty())
                playAudio(q.getAudioRef());

            sectionTextView.setText(q.getSection());
            boolean isOnlyNumbers = true;
            for (String answer : q.getAllAnswers()) {
                try {
                     Integer.parseInt(answer);
                } catch (NumberFormatException n) {
                    isOnlyNumbers = false;
                    break;
                }
            }
            adapter = new ListViewAdapter(this, q.getAllAnswers(), !isOnlyNumbers);
            answersListView.setAdapter(adapter);
            answersListView.setOnItemClickListener(answersListViewClickListener);
            rightAnswer = q.getRightAnswer();
        }
    }

    private void playAudio(final String s) {
        float speed = 0;
        ttwmp.loadAndPlay(s, App.speechVolume, App.speechSpeed);
    }

    // listeners for click on the list row
    final private transient OnItemClickListener answersListViewClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view,
                                final int position, final long itemID) {
            Answer selected = q.getAnswers().get(position);
            // comparing correct and selected answer
            if (selected == rightAnswer) {
                // if it's a level definition test - all results are wrote
                // in scoreInVoc, else - divided by sections
                if (isLevelDef) {
                    tp.incrementScoreInVocGr(q.getWeight());
                } else {
                    if (currentSection == 1)
                        tp.incrementScoreInVocGr(q.getWeight());
                    else if (currentSection == 2)
                        tp.incrementScoreInReading(q.getWeight());
                    else
                        tp.incrementScoreInListening(q.getWeight());
                }
                tp.incrementNumberOfCorrectAnswers();

                // change color to green and fade out
                isCorrect.setImageResource(R.drawable.yes);
            } else {
                // change color of row and set text
                isCorrect.setImageResource(R.drawable.no);
            }
            // fading out textboxes
            fadeOut(sectionTextView, 750);
            fadeOut(taskTextView, 750);
            fadeOut(titleTextView, 750);
            fadeOut(textTextView, 750);
            fadeOut(isCorrect, 750);

            // fading out listview
            ListView v = (ListView) view.getParent();
            fadeOut(v, 750);

            Animation fadeOutAnimation = AnimationUtils.loadAnimation(
                    v.getContext(), android.R.anim.fade_out);
            fadeOutAnimation.setDuration(750);
            v.startAnimation(fadeOutAnimation);

            fadeOutAnimation
                    .setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // when previous information faded out
                            // show next word and possible answers or go to
                            // next exercise
                            if (currentWordNumber <= t.getQuestions().size()) {
                                nextWord();
                            } else {
                                endTesting();
                            }
                        }

                        // doesn't needed, just implementation
                        @Override
                        public void onAnimationRepeat(Animation arg0) {
                        }

                        @Override
                        public void onAnimationStart(Animation arg0) {
                        }
                    });

        }
    };

    public void fadeOut(View v, long duration) {
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(
                v.getContext(), android.R.anim.fade_out);
        fadeOutAnimation.setDuration(750);
        v.startAnimation(fadeOutAnimation);
    }

    public void buttonIDontKnowOnClick(View v) {
        nextWord();
    }

    public void buttonEndTestOnClick(View v) {
        endTesting();
    }

    public void onPlayClick(View v) {
        playAudio(q.getAudioRef());
    }

    long mLastStopTime;

    public void buttonPauseOnClick(View v) {
        onPause();
        ttwmp.pause();
        mLastStopTime = SystemClock.elapsedRealtime();
        chronometer.stop();
        AlertDialog.Builder builder = new AlertDialog.Builder(MockTestActivity.this);
        builder.setTitle(testName)
                .setMessage(getResources().getString(R.string.pause))
                .setIcon(R.drawable.pause_icon)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.resume),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                onResume();
                                long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
                                chronometer.setBase(chronometer.getBase() + intervalOnPause);
                                chronometer.start();
                                ttwmp.resume();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void endTesting() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        // stop playing
                        if (player.isPlaying()) {
                            player.stop();
                            player.release();
                            player = new MediaPlayer();
                        }
                        // show recommendations or results
                        Intent intent;
                        if (testName.equals("level_def")) {
                            intent = new Intent(getBaseContext(),
                                    LevelDefTestResultActivity.class);
                        } else {
                            intent = new Intent(getBaseContext(),
                                    TestResultActivity.class);
                            intent.putExtra("testName", t.getName());
                        }
                        startActivity(intent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked - do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.end_test_confirmation))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener)
                .show();

    }

    public void toNextSection() {
        String section = q.getSection();
        if (currentSection == 3)
            endTesting();
        else
            while (true) {
                String s = t.getQuestions().get(currentWordNumber).getSection();
                if (!section.equals(s))
                    break;
                else
                    currentWordNumber++;
            }
        currentWordNumber--;
        nextWord();
    }

    public void skipSectionOnClick(View v) {
        // dialog yes/no
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked - increment currentWordNumber until
                        // question of next section
                        toNextSection();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked - do nothing
                        break;
                }
            }

        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.skip_section_confirmation))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener)
                .show();
    }

}
