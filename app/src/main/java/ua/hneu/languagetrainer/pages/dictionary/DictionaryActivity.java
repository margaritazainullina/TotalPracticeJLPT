package ua.hneu.languagetrainer.pages.dictionary;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;

import com.google.android.gms.internal.en;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import ua.hneu.edu.languagetrainer.R;
import ua.hneu.languagetrainer.AllVocabularyListViewAdapter;
import ua.hneu.languagetrainer.App;
import ua.hneu.languagetrainer.DictionaryListViewAdapter;
import ua.hneu.languagetrainer.model.DictionaryAbstr;
import ua.hneu.languagetrainer.model.EntryAbstr;
import ua.hneu.languagetrainer.model.grammar.GrammarDictionary;
import ua.hneu.languagetrainer.model.grammar.GrammarRule;
import ua.hneu.languagetrainer.model.other.CounterWord;
import ua.hneu.languagetrainer.model.other.Giongo;
import ua.hneu.languagetrainer.model.vocabulary.VocabularyDictionary;
import ua.hneu.languagetrainer.model.vocabulary.VocabularyEntry;
import ua.hneu.languagetrainer.service.GrammarService;
import ua.hneu.languagetrainer.service.VocabularyService;

public class DictionaryActivity extends ListActivity {
    DictionaryListViewAdapter adapter1;

    //entries to show in list
    ArrayList<String> entriesToShow = new ArrayList<>();
    ArrayList<String> allJapToShow = new ArrayList<>();
    ArrayList<String> allTranslToShow = new ArrayList<>();

    //all real entries
    Set<EntryAbstr> all = new TreeSet<>();

    boolean fromJapanese = true;
    int counter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        handleIntent(getIntent());
        VocabularyDictionary vd = VocabularyService.allEntriesDictionary(App.cr);
        GrammarDictionary gd = GrammarService.allEntriesDictionary(App.cr);
        GrammarDictionary gd1 = GrammarService.selectAllEntriesOflevel(1, App.cr);
        all.addAll(gd1.getEntries());
        GrammarDictionary gd2 = GrammarService.selectAllEntriesOflevel(2, App.cr);
        all.addAll(gd2.getEntries());
        GrammarDictionary gd3 = GrammarService.selectAllEntriesOflevel(3, App.cr);
        all.addAll(gd3.getEntries());
        GrammarDictionary gd4 = GrammarService.selectAllEntriesOflevel(4, App.cr);
        all.addAll(gd4.getEntries());
        GrammarDictionary gd5 = GrammarService.selectAllEntriesOflevel(5, App.cr);
        all.addAll(gd5.getEntries());
        all.addAll(vd.getEntries());
        all.addAll(App.allGiongoDictionary.getEntries());
        all.addAll(App.allCounterWordsDictionary.getEntries());
        Log.d("TOTAL:", all.size()+"");

        TreeSet<String> temp = new TreeSet<>();
        for(EntryAbstr ea: all ){
            if(ea instanceof VocabularyEntry) {
                VocabularyEntry ve = (VocabularyEntry) ea;
                temp.addAll(ve.getTranslations());
                allJapToShow.add(ve.getKanjiOrHiragana());
            }
            if(ea instanceof GrammarRule) {
                GrammarRule ve = (GrammarRule) ea;
                temp.addAll(ve.getSplittedDescriptions());
                allJapToShow.add(ea.toString());
            }
            if(ea instanceof Giongo) {
                Giongo ve = (Giongo) ea;
                temp.addAll(ve.getSplittedDescriptions());
                allJapToShow.add(ea.toString());
            }
            if(ea instanceof CounterWord) {
                CounterWord ve = (CounterWord) ea;
                temp.addAll(ve.getSplittedDescriptions());
                allJapToShow.addAll(ve.getSplittedWord());
            }
       }

        allTranslToShow = new ArrayList(temp);
       // Collections.sort(allTranslToShow);
        showAll();
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchview_in_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
                .getActionView();
        if (searchView != null) {
            searchView
                    .setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                        public boolean onQueryTextSubmit(String submit) {
                            return false;
                        }

                        public boolean onQueryTextChange(String change) {
                            if (change.isEmpty())
                                showAll();
                            search(change);
                            return false;
                        }

                    });
            searchView.setOnCloseListener(new OnCloseListener() {

                @Override
                public boolean onClose() {
                    showAll();
                    return false;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        String search =  (String) getListAdapter().getItem(position);
        Log.d("onListItemClick", search);
        Set<EntryAbstr> result = new TreeSet<>();
        for(EntryAbstr ea : all){
            if(fromJapanese){
                if(ea.toString().equals(search)) result.add(ea);
            }
            else{
                if(ea.translationsToString().equals(search)) result.add(ea);
            }
        }

        for(EntryAbstr ea : result) Log.d("FOUND: ",ea.toString());

       /* EntryAbstr ea;
        if(fromJapanese)
             ea = allSortedByJap.get(position);
        else  ea = allSortedByTransl.get(position);

        if(ea instanceof VocabularyEntry){
            VocabularyEntry ve = (VocabularyEntry) ea;
            Intent intent = new Intent(this, VocabularyEntryDictionaryDetail.class);
            intent.putExtra("entry",ve);
            startActivity(intent);
        }
        if(ea instanceof GrammarRule){
            GrammarRule gr = (GrammarRule) ea;
            Intent intent = new Intent(this, GrammarEntryDictionaryDetail.class);
            intent.putExtra("entry",gr);
            startActivity(intent);
        }
        if(ea instanceof Giongo){
            Giongo g = (Giongo) ea;
            Intent intent = new Intent(this, GiongoEntryDictionaryDetail.class);
            intent.putExtra("entry",g);
            startActivity(intent);
        }
        if(ea instanceof CounterWord){
            CounterWord cw = (CounterWord) ea;
            Intent intent = new Intent(this, CWEntryDictionaryDetail.class);
            intent.putExtra("entry",cw);
            startActivity(intent);
        }*/

    }

    private void search(String query) {
        entriesToShow.clear();
        Log.d("query", query);
            for (EntryAbstr entry : all) {
                if (fromJapanese) {
                    if (entry instanceof VocabularyEntry) {
                        VocabularyEntry ve = (VocabularyEntry) entry;
                        if ((ve.getKanji().toLowerCase()).startsWith(query.toLowerCase())
                                || (ve.getRomaji().toLowerCase()).startsWith(query.toLowerCase())
                                || (ve.getTranscription().toLowerCase()).startsWith(query.toLowerCase())) {
                            entriesToShow.add(ve.getKanjiOrHiragana());
                        }
                    } else if (entry instanceof GrammarRule) {
                        GrammarRule ve = (GrammarRule) entry;
                        if ((ve.getRule().toLowerCase()).contains(query.toLowerCase())) {
                            entriesToShow.add(ve.toString());
                        }
                    } else if (entry instanceof Giongo) {
                        Giongo ve = (Giongo) entry;
                        if ((ve.getRomaji().toLowerCase()).startsWith(query.toLowerCase())
                                || (ve.getWord().toLowerCase()).startsWith(query.toLowerCase()))
                            entriesToShow.add(ve.toString());
                    } else if (entry instanceof CounterWord) {
                        CounterWord ve = (CounterWord) entry;
                        if ((ve.getRomaji().toLowerCase()).startsWith(query.toLowerCase())
                                || (ve.getWord().toLowerCase()).startsWith(query.toLowerCase()))
                            entriesToShow.add(ve.toString());
                    }
                } else {
                    if (entry.translationsToString().startsWith(query)) {
                        entriesToShow.add(entry.translationsToString());
                        Log.d("found", entry.toString());
                    }
                }
            }

        adapter1 = new DictionaryListViewAdapter(this,  new ArrayList<String>(entriesToShow), fromJapanese);
        this.setListAdapter(adapter1);

        /*Log.d("DictionaryActivity", "search "+query);
        ArrayList<String>  entriestoShow1 = new ArrayList<String>();
        boolean isFound = false;
        for (String word : japToShow) {
            if(word.contains(query)) entriestoShow1.add(word);
            Log.d("DictionaryActivity", "word "+word);
        }


            if(fromJapanese) {
                //if VocabularyEntry
                for (EntryAbstr entry : allSortedByJap) {
                    if (entry instanceof VocabularyEntry) {
                        VocabularyEntry ve = (VocabularyEntry) entry;
                        if ((ve.getKanji().toLowerCase()).startsWith(query.toLowerCase())
                                || (ve.getRomaji().toLowerCase()).startsWith(query.toLowerCase())
                                || (ve.getTranscription().toLowerCase()).startsWith(query.toLowerCase())) {
                            entriestoShow1.add(ve);
                        }
                    } else if (entry instanceof GrammarRule) {
                        GrammarRule ve = (GrammarRule) entry;
                        if ((ve.getRule().toLowerCase()).contains(query.toLowerCase())) {
                            entriestoShow1.add(ve);
                        }
                    } else if (entry instanceof Giongo) {
                        Giongo ve = (Giongo) entry;
                        if ((ve.getRomaji().toLowerCase()).startsWith(query.toLowerCase())
                                || (ve.getWord().toLowerCase()).startsWith(query.toLowerCase()))
                            entriestoShow1.add(ve);
                    } else if (entry instanceof CounterWord) {
                        CounterWord ve = (CounterWord) entry;
                        if ((ve.getRomaji().toLowerCase()).startsWith(query.toLowerCase())
                                || (ve.getWord().toLowerCase()).startsWith(query.toLowerCase()))
                            entriestoShow1.add(ve);
                    }
                }
            }
            else {
                for (EntryAbstr entry : allSortedByTransl) {
                    //if VocabularyEntry
                    if (entry instanceof VocabularyEntry) {
                        VocabularyEntry ve = (VocabularyEntry) entry;
                        if (ve.translationsToString().toLowerCase().contains(query.toLowerCase())) {
                            entriestoShow1.add(ve);
                        }
                    } else if (entry instanceof GrammarRule) {
                        GrammarRule ve = (GrammarRule) entry;
                        if (ve.getDescription().toLowerCase().contains(query.toLowerCase())) {
                            entriestoShow1.add(ve);
                        }
                    } else if (entry instanceof Giongo) {
                        Giongo ve = (Giongo) entry;
                        if (ve.translationsToString().toLowerCase().contains(query.toLowerCase()))
                            entriestoShow1.add(ve);
                    } else if (entry instanceof CounterWord) {
                        CounterWord ve = (CounterWord) entry;
                        if (ve.translationsToString().toLowerCase().contains(query.toLowerCase()))
                            entriestoShow1.add(ve);
                    }
                }
            }

        if(fromJapanese){
            ArrayList<String> entriesToShow = new ArrayList<>();
            for(EntryAbstr ea : allSortedByJap) entriesToShow.add(ea.toString());
            adapter1 = new DictionaryListViewAdapter(this, entriesToShow, fromJapanese);
        }
        else {
            ArrayList<String> entriesToShow = new ArrayList<>();
            for(EntryAbstr ea : allSortedByJap) entriesToShow.add(ea.translationsToString());
            adapter1 = new DictionaryListViewAdapter(this, entriesToShow, fromJapanese);
        }
        adapter1 = new DictionaryListViewAdapter(this, entriestoShow1, fromJapanese);
        this.setListAdapter(adapter1);*/
    }

    private void showAll() {
        entriesToShow.clear();
        if(fromJapanese) entriesToShow.addAll(allJapToShow);
        else  entriesToShow.addAll(allTranslToShow);
        adapter1 = new DictionaryListViewAdapter(this, new ArrayList<String>(entriesToShow), fromJapanese);
        this.setListAdapter(adapter1);
    }
    public void switchButtonOnClick(View v){
        fromJapanese=!fromJapanese;
        showAll();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }
}