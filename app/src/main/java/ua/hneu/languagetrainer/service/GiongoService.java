package ua.hneu.languagetrainer.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import ua.hneu.languagetrainer.App;
import ua.hneu.languagetrainer.db.dao.CounterWordsDAO;
import ua.hneu.languagetrainer.db.dao.GiongoDAO;
import ua.hneu.languagetrainer.model.other.Giongo;
import ua.hneu.languagetrainer.model.other.GiongoDictionary;
import ua.hneu.languagetrainer.model.other.GiongoExample;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Debug;
import android.util.Log;

/**
 * @author Margarita Zainullina <margarita.zainullina@gmail.com>
 * @version 1.0
 */
public class GiongoService {
	GiongoExampleService ges = new GiongoExampleService();
	static int numberOfEnteries = 0;
	private ArrayList<Giongo> entries;

	/**
	 * Inserts a Giongo to database
	 * 
	 * @param g
	 *            - Giongo instance to insert
	 * @param cr
	 *            - content resolver to database
	 */
	public void insert(Giongo g, ContentResolver cr) {
		for (GiongoExample ge : g.getExamples()) {
			ges.insert(ge, numberOfEnteries, cr);
		}
		numberOfEnteries++;
		ContentValues values = new ContentValues();
		values.put(GiongoDAO.WORD, g.getWord());
		values.put(GiongoDAO.ROMAJI, g.getRomaji());
		values.put(GiongoDAO.TRANSLATION_ENG, g.getTranslEng());
		values.put(GiongoDAO.TRANSLATION_RUS, g.getTranslRus());
		values.put(GiongoDAO.PERCENTAGE, g.getLearnedPercentage());
		values.put(GiongoDAO.LASTVIEW, g.getLastview());
		values.put(GiongoDAO.SHOWNTIMES, g.getShownTimes());
		values.put(GiongoDAO.COLOR, g.getColor());
		cr.insert(GiongoDAO.CONTENT_URI, values);
	}

	/**
	 * Updates a Giongo in database
	 * 
	 * @param g
	 *            - Giongo instance to upate
	 * @param contentResolver
	 *            - content resolver to database
	 */
	public void update(Giongo g, ContentResolver contentResolver) {
		ContentValues values = new ContentValues();
		values.put(GiongoDAO.WORD, g.getWord());
		values.put(GiongoDAO.ROMAJI, g.getRomaji());
		values.put(GiongoDAO.TRANSLATION_ENG, g.getTranslEng());
		values.put(GiongoDAO.TRANSLATION_RUS, g.getTranslRus());
		values.put(GiongoDAO.PERCENTAGE, g.getLearnedPercentage());
		values.put(GiongoDAO.LASTVIEW, g.getLastview());
		values.put(GiongoDAO.SHOWNTIMES, g.getShownTimes());
		values.put(GiongoDAO.COLOR, g.getColor());
		contentResolver.insert(GiongoDAO.CONTENT_URI, values);
		String s = CounterWordsDAO.WORD + " =\"" + g.getWord() + "\" ";
		contentResolver.update(GiongoDAO.CONTENT_URI, values, s, null);

	}

	/**
	 * A stub to find out last id of giongo to insert an example to it
	 * 
	 * @param contentResolver
	 *            content resolver to database
	 */
	public static void startCounting(ContentResolver contentResolver) {
		numberOfEnteries = getNumberOfGiongo(contentResolver) + 1;
	}

	/**
	 * Returns number of entries in Giongo table
	 * 
	 * @param cr
	 *            content resolver to database
	 * @return number of entries
	 */
	public static int getNumberOfGiongo(ContentResolver cr) {
            Cursor countCursor = cr.query(GiongoDAO.CONTENT_URI,
                    new String[] { "count(*) AS count" }, null, null, null);
            countCursor.moveToFirst();
            int num = countCursor.getInt(0);
            countCursor.close();
            return num;
	}

	/**
	 * Deletes all entries from Giongo table
	 */
	public void emptyTable() {
		GiongoDAO.getDb().execSQL("delete from " + GiongoDAO.TABLE_NAME);
	}

	/**
	 * Creates Giongo table
	 */
	public void createTable() {
		SQLiteDatabase db = GiongoDAO.getDb();
		db.execSQL("CREATE TABLE if not exists " + GiongoDAO.TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + GiongoDAO.WORD
				+ " TEXT, " + GiongoDAO.ROMAJI + " TEXT, "
				+ GiongoDAO.TRANSLATION_ENG + " TEXT, "
				+ GiongoDAO.TRANSLATION_RUS + " TEXT, " + GiongoDAO.PERCENTAGE
				+ " REAL, " + GiongoDAO.LASTVIEW + " DATETIME,"
				+ GiongoDAO.SHOWNTIMES + " INTEGER, " + GiongoDAO.COLOR
				+ " TEXT); ");
	}

	/**
	 * Drops GiongoExamples table
	 */
	public void dropTable() {
		GiongoDAO.getDb().execSQL(
				"DROP TABLE if exists " + GiongoDAO.TABLE_NAME + ";");
	}

	/**
	 * Gets list of all Giongo in db
	 * 
	 * @param cr
	 *            - content resolver to database
	 * @return giongoList list of all giongo in the table
	 */
	@SuppressLint("NewApi")
	public static GiongoDictionary getAllGiongo(ContentResolver cr) {
		String[] col = { GiongoDAO.ID, GiongoDAO.WORD, GiongoDAO.ROMAJI,
				GiongoDAO.TRANSLATION_ENG, GiongoDAO.TRANSLATION_RUS,
				GiongoDAO.PERCENTAGE, GiongoDAO.LASTVIEW, GiongoDAO.SHOWNTIMES,
				GiongoDAO.COLOR };
		Cursor c = cr.query(GiongoDAO.CONTENT_URI, col, null, null, null, null);
		c.moveToFirst();
		int id = 0;
		String word;
		String romaji;
		String translEng;
		String translRus;
		double percentage = 0;
		String lastview = "";
		int showntimes = 0;
		String color;

		GiongoExampleService ges = new GiongoExampleService();
		GiongoDictionary giongoList = new GiongoDictionary();
		while (!c.isAfterLast()) {
			id = c.getInt(0);
			word = c.getString(1);
			romaji = c.getString(2);
			translEng = c.getString(3);
			translRus = c.getString(4);
			percentage = c.getDouble(5);
			lastview = c.getString(6);
			showntimes = c.getInt(7);
			color = c.getString(8);
			ArrayList<GiongoExample> ge = new ArrayList<GiongoExample>();
			ge = ges.getExamplesByGiongoId(id, cr);
			giongoList.add(new Giongo(word, romaji, translEng, translRus,
					percentage, showntimes, lastview, color, ge));
			c.moveToNext();
		}
		c.close();
		return giongoList;
	}

	/**
	 * Inserts all entries divided by tabs from assets file
	 * 
	 * @param filepath
	 *            path to assets file
	 * @param assetManager
	 *            assetManager from activity context
	 * @param cr
	 *            content resolver to database
	 */
	public void bulkInsertFromCSV(String filepath, AssetManager assetManager,
			ContentResolver cr) {
		BufferedReader reader = null;
        int number=0;
		try {
			reader = new BufferedReader(new InputStreamReader(
					assetManager.open(filepath)));
			// do reading, usually loop until end of file reading
			String mLine;
			boolean isFirst = true;
			Giongo g = new Giongo();
			// setting random colors
			Random r = new Random();
			int r1 = r.nextInt(5);
			String color = "";

			while ((mLine = reader.readLine()) != null) {
				if (!mLine.isEmpty()) {
					if (isFirst) {
                        number++;
						String[] s = mLine.split("\\t");
						r = new Random();
						r1 = r.nextInt(5);
						switch (r1) {
						case 0: {
							color = "138,213,240";
							break;
						}
						case 1: {
							color = "214,173,235";
							break;
						}
						case 2: {
							color = "197,226,109";
							break;
						}

						case 3: {
							color = "255,217,128";
							break;
						}
						case 4: {
							color = "255,148,148";
							break;
						}
						}
						g = new Giongo(s[0].trim(), s[1].trim(), s[2].trim(),
								s[3].trim(), 0, 0, "", color.trim(),
								new ArrayList<GiongoExample>());
						isFirst = false;
                        Log.d("GiongoService - bulk insert", g.getWord());
					} else {
						String[] s = mLine.split("\\t");
						GiongoExample ge = new GiongoExample(s[0].trim()
								+ "\\t" + s[1].trim() + "\\t" + s[2].trim(),
								s[3].trim(), s[4].trim(), s[5].trim());
						g.examples.add(ge);
                        Log.d("GiongoService - bulk insert", ge.getText());
					}
				} else {
					this.insert(g, cr);
					g = new Giongo();
					g.setExamples(new ArrayList<GiongoExample>());
					isFirst = true;
				}
			}
		} catch (IOException e) {
			Log.e("VocabularyService", e.getMessage() + " " + e.getCause());

		}
	}

	// returns Set with stated size of unique random entries from current
	// dictionary
	public Set<Giongo> getRandomEntries(int size) {
		Set<Giongo> random = new HashSet<Giongo>();
		Random rn = new Random();

		while (random.size() <= size) {
			int i = rn.nextInt(entries.size());
			if (entries.get(i).getLearnedPercentage() < 1) {
				random.add(entries.get(i));
			}
		}
		return random;
	}

	public static GiongoDictionary getNLastViewedEntries(int size,
			ContentResolver cr) {
		String[] col = { GiongoDAO.ID, GiongoDAO.WORD, GiongoDAO.ROMAJI,
				GiongoDAO.TRANSLATION_ENG, GiongoDAO.TRANSLATION_RUS,
				GiongoDAO.PERCENTAGE, GiongoDAO.LASTVIEW, GiongoDAO.SHOWNTIMES,
				GiongoDAO.COLOR };
		Cursor c = cr.query(GiongoDAO.CONTENT_URI, col, GiongoDAO.PERCENTAGE
				+ "<1", null, GiongoDAO.LASTVIEW + " ASC limit " + size, null);
		c.moveToFirst();
		int id = 0;
		String word;
		String romaji;
		String translEng;
		String translRus;
		double percentage = 0;
		String lastview = "";
		int showntimes = 0;
		String color;

		GiongoExampleService ges = new GiongoExampleService();
		GiongoDictionary giongoList = new GiongoDictionary();
		while (!c.isAfterLast()) {
			id = c.getInt(0);
			word = c.getString(1);
			romaji = c.getString(2);
			translEng = c.getString(3);
			translRus = c.getString(4);
			percentage = c.getDouble(5);
			lastview = c.getString(6);
			showntimes = c.getInt(7);
			color = c.getString(8);
			ArrayList<GiongoExample> ge = new ArrayList<GiongoExample>();
			ge = ges.getExamplesByGiongoId(id, cr);
			giongoList.add(new Giongo(word, romaji, translEng, translRus,
					percentage, showntimes, lastview, color, ge));
			c.moveToNext();
		}
		c.close();
		return giongoList;
	}

	/**
	 * Returns GiongoDictionary to store it in the App class
	 * 
	 * @param numberWordsInCurrentDict
	 *            number of entries to select for learning
	 * @param contentResolver
	 *            content resolver to database
	 * @return currentDict GiongoDictionary with Giongo entries
	 */
	public static GiongoDictionary createCurrentDictionary(int numberWordsInCurrentDict, ContentResolver contentResolver) {
        Log.d("GS","createCurrentDictionary numberWordsInCurrentDict="+numberWordsInCurrentDict);
        if (App.allGiongoDictionary == null) {
            App.allGiongoDictionary = new GiongoDictionary();
            App.allGiongoDictionary = getAllGiongo(contentResolver);
        }
        GiongoDictionary currentDict = new GiongoDictionary();
        // if words have never been showed - set entries randomly
        if (App.userInfo.getIsGiongoLaunchedFirstTime() == 1) {
            currentDict.getEntries().addAll(
                    App.allGiongoDictionary.getRandomEntries(
                            App.numberOfEntriesInCurrentDict));
            App.userInfo.setIsGiongoLaunchedFirstTime(0);
        } else {
            currentDict = GiongoService.getNLastViewedEntries( App.numberOfEntriesInCurrentDict,  contentResolver);
        }
        return currentDict;
	}
    public void makeProgress(ContentResolver cr) {
        Random r = new Random();
        int i=0;
        for (Giongo e : App.allGiongoDictionary.getEntries()) {
            int rr = r.nextInt(100);
            if (rr < 99){
                i++;
                e.setLearnedPercentage(1);
                update(e, cr);
            }
            else
            {
                e.setLearnedPercentage(0.9);
                update(e, cr);
            }
        }

        App.userInfo.setLearnedGiongo(i);
        App.us.update(App.userInfo, cr);
    }
}
