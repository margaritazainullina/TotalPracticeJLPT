package ua.hneu.languagetrainer.model.other;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;

import ua.hneu.languagetrainer.App;
import ua.hneu.languagetrainer.model.DictionaryAbstr;

public class GiongoDictionary extends DictionaryAbstr {
    private ArrayList<Giongo> entries = new ArrayList<Giongo>();

    public void add(Giongo giongo) {
        entries.add(giongo);
    }

    public Giongo get(int i) {
        return entries.get(i);
    }

    public int size() {
        return entries.size();
    }

    public void remove(Giongo g) {
        entries.remove(g);
    }

    public Hashtable<GiongoExample, Giongo> getRandomExamplesWithWord(int size) {
        Hashtable<GiongoExample, Giongo> random = new Hashtable<GiongoExample, Giongo>();
        Random rn = new Random();
        // TODO: what if wasn't learned words number is less than size
        if (this.size() <= size) {
            for (Giongo gr : this.entries) {
                int j = 0;
                if (gr.getExamples().size() > 0) j = rn.nextInt(gr.getExamples().size());
                if (!random.contains(gr.getExamples())) {
                    random.put(gr.getExamples().get(j), gr);
                }
            }
        } else {
            int counter = 0;
            while (counter <= size) {
                int i = rn.nextInt(entries.size());
                Giongo g = entries.get(i);
                int j = rn.nextInt(g.getExamples().size());
                if (!random.contains(g.getExamples())) {
                    random.put(g.getExamples().get(j), g);
                    counter++;
                }
            }
        }
        return random;
    }

    // returns Set with stated size of unique random entries from current
    // dictionary
    public Set<Giongo> getRandomEntries(int size) {
        Set<Giongo> random = new HashSet<Giongo>();
        if (App.userInfo.getNumberOfGiongoInLevel() <= App.userInfo.getLearnedGiongo())
            return random;
        Random rn = new Random();

        while (random.size() <= size) {
            int i = rn.nextInt(entries.size());
            random.add(entries.get(i));

        }
        return random;
    }

    public ArrayList<Giongo> getEntries() {
        return entries;
    }

    public void addEntriesToDictionaryAndGet(int size) {
        Random rn = new Random();

        while (this.size() < size) {
            int i = rn.nextInt(App.allGiongoDictionary.size());
            Giongo g = App.allGiongoDictionary.get(i);
            // if the word is not learned
            if (g.getLearnedPercentage() < 1) {
                this.entries.add(g);
            }
        }
    }

    public ArrayList<String> getAllGiongo() {
        ArrayList<String> giongo = new ArrayList<String>();
        for (Giongo e : entries) {
            giongo.add(e.getWord());
        }
        return giongo;
    }

    public ArrayList<String> getAllTranslation() {
        ArrayList<String> translation = new ArrayList<String>();
        for (Giongo e : entries) {
            translation.add(e.translationsToString());
        }
        return translation;
    }

    public GiongoDictionary search(String query) {
        GiongoDictionary result = new GiongoDictionary();

        for (Giongo ve : entries) {
            if ((ve.getRomaji().toLowerCase()).startsWith(query.toLowerCase())
                    || (ve.translationsToString().toLowerCase()).startsWith(query.toLowerCase())
                    || (ve.getWord().toLowerCase()).startsWith(query.toLowerCase()))
                result.add(ve);
        }
        return result;
    }

    public Giongo getByWord(String word) {
        Giongo gr = null;
        for (Giongo g : entries) {
            if (g.getWord().equals(word)) {
                gr = g;
                break;
            }
        }
        return gr;
    }

    public ArrayList<String> allToString() {
        return getAllGiongo();
    }

    public Giongo fetchRandom() {
        int a = new Random().nextInt(entries.size() - 1);
        return entries.get(a);
    }
}
