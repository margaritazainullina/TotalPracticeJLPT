package ua.hneu.languagetrainer.pages.giongo;

import ua.hneu.edu.languagetrainer.R;
import ua.hneu.languagetrainer.App;
import ua.hneu.languagetrainer.ExamplesListViewAdapter;
import ua.hneu.languagetrainer.TextToVoiceMediaPlayer;
import ua.hneu.languagetrainer.model.other.Giongo;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AllGiongoExamples extends ListActivity {
	ExamplesListViewAdapter adapter;
	ListView kanjiListView;
	TextToVoiceMediaPlayer twmp;
	String phrase = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_allvocabulary);

		Bundle extras = getIntent().getExtras();
		Giongo gr = null;
		twmp = new TextToVoiceMediaPlayer();

		if (extras != null) {
			String rule = extras.get("giongo").toString();
			gr = App.allGiongoDictionary.getByWord(rule);
		}

		//TODO: custom adapter!
		adapter = new ExamplesListViewAdapter(this,
				gr.getAllExamplesText(), gr.getAllExamplesRomaji(),
				gr.getAllTranslations(), gr.getIntColor());
		this.setListAdapter(adapter);

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        AdView mAdView = (AdView) findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
	}
	
	public void onPlayClick1(View v) {
		// getting layout with text
		View v1 = (View) v.getParent();
		TextView textPart1 = (TextView) v1.findViewById(R.id.textPart1);
		TextView textPart2 = (TextView) v1.findViewById(R.id.textPart2);
		TextView textPart3 = (TextView) v1.findViewById(R.id.textPart3);
		phrase = (String) textPart1.getText() + textPart2.getText()
				+ textPart3.getText();
		twmp.loadAndPlay(phrase, App.speechVolume,App.speechSpeed);
	}
	
	@Override
	public boolean onSearchRequested() {
		return super.onSearchRequested();
	}
}
