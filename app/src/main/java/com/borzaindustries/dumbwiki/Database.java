package com.borzaindustries.dumbwiki;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Database {
    public HashMap<String, WikiPage> pages;
    private Context context;

    final String BACKUP_FILE_NAME = "/sdcard/dumbwiki.bak";
    final String BACKUP_ENCODING = "UTF-8";
    final String NAME = "name";
    final String TEXT = "text";

    public Database(Context context) {
        pages = new HashMap<String, WikiPage>();
        this.context = context;
    }

    public boolean pageExists(String name) {
        return pages.containsKey(name.toLowerCase());
    }

    public WikiPage getPage(String name) {
        String key = name.toLowerCase();
        if (!pageExists(key))
            return null;
        return pages.get(key);
    }

    public void loadFakePages() {
        WikiPage p = new WikiPage();
        p.Text = "Home\n===\n\nWelcome to DumbWiki, your not-too-smart personal wiki. It isn't too dumb, really, it's just plain and minimalistic.\n\nThis is your new Home page. Edit it as you want, it's what's displayed on DumbWiki start. \n\nCheck out [Sample page]\n\nand [Help] or just create a [new page] or maybe a [TODO list].";
        p.Name = "Home";
        pages.put(p.Name.toLowerCase(), p);
        p = new WikiPage();
        p.Name = "Sample page";
        p.Text = "Sample page page. <hr/>Go to [Help] or [Home] or [Yet Nonexistent Page]\n\nNew *bold* paragraph, \nnot recognized newline, _italic_ text and `some preformatted stuff\nnewline \nand another`. \n\n some HTML: <div style=\"background-color:yellow\">yellow <b>div</b> text<hr/></div>";
        pages.put(p.Name.toLowerCase(), p);
        p = new WikiPage();
        p.Name = "Help";
        p.Text = "DumbWiki help\n\nTo create new page, just use a [link] within square brackets.\n\n Edit this page to see how the formatting works!\n\n*Bold text*, ''italics'', _underline_ (can be combined),\n\n [Link] (inside DumbWiki), [[www.google.sk]] outer link,\n\n`preformated\n  `\n\nAll of <b>HTML</b> also works\n\n";
        pages.put(p.Name.toLowerCase(), p);
    }

    /**
     * saves page, or deletes, if empty
     *
     * @param name
     * @param text
     * @return false if page was deleted
     */
    public boolean setPage(String name, String text) {
        String key = name.toLowerCase();
        /*if (text.trim().length() == 0) {
			pages.remove(key);
			return false;
		}*/
        WikiPage wp = new WikiPage();
        wp.Name = name;
        wp.Text = text;
        pages.put(key, wp);
        return true;
    }

    public void loadPages() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        Map<String, ?> map = prefs.getAll();
        for (String key : map.keySet()) {
            WikiPage page = new WikiPage();
            page.Name = key;
            page.Text = prefs.getString(key, "");
            pages.put(page.Name.toLowerCase(), page);
        }
    }

    public void savePages() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        for (String key : pages.keySet()) {
            WikiPage wp = pages.get(key);
            editor.putString(wp.Name, wp.Text);
        }
        editor.commit();
    }

    public void zap() {
        loadFakePages();
        savePages();
    }

    public void deletePage(String oldname) {
        pages.remove(oldname.toLowerCase());
    }

    public void initializeIfFirstRun() {
        if (!pages.containsKey("home"))
            zap();
    }

    // backup and restore
    public void backup() {
        try {
            JSONArray ja = new JSONArray();
            for (String key : pages.keySet()) {
                WikiPage wp = pages.get(key);
                JSONObject jo = new JSONObject();
                jo.put(NAME, wp.Name);
                jo.put(TEXT, wp.Text);
                ja.put(jo);
            }
            String data = ja.toString(2);
            byte[] bytes = data.getBytes(BACKUP_ENCODING);
            OutputStream output = new FileOutputStream(BACKUP_FILE_NAME);
            GZIPOutputStream gzos = new GZIPOutputStream(output);
            gzos.write(bytes, 0, bytes.length);
            gzos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restore() {
        try {
            Log.d("dw", "restore() start");
            InputStream input = new FileInputStream(BACKUP_FILE_NAME);
            GZIPInputStream gzis = new GZIPInputStream(input);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(16384);
            byte[] temp = new byte[1024];
            while (true) {
                int read = gzis.read(temp);
                if (read <= 0)
                    break;
                bos.write(temp, 0, read);
            }
            gzis.close();
            Log.d("dw", "file read ok, json follows");
            byte[] jsonData = bos.toByteArray();
            String json = new String(jsonData, BACKUP_ENCODING); // default encoding?
            Log.d("dw", json);
            JSONArray ja = new JSONArray(json);
            Log.d("dw", "got jsonarray of length" + ja.length());
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                WikiPage wp = new WikiPage();
                wp.Name = jo.getString(NAME);
                wp.Text = jo.getString(TEXT);
                this.pages.put(wp.Name.toLowerCase(), wp);
            }
            Log.d("dw", "put pages, saving...");
            savePages();
            Log.d("dw", "saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
