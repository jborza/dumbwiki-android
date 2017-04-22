package com.borzaindustries.dumbwiki;

import android.util.Log;

import com.cforcoding.jmd.MarkDown;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiPage {
    public String Text;
    public String Name;
    public int ID;

    static boolean initializedPatterns = false;
    static Pattern link;
    static Pattern outlink;

    static void initPatterns() {
        if (initializedPatterns)
            return;
        outlink = Pattern.compile("\\[\\[(.+?)\\]\\]");
        link = Pattern.compile("\\[(.+?)\\]");
        initializedPatterns = true;
    }

    public String RenderToHTML(Database db) {
        long start = System.currentTimeMillis();
        // ////////////start
        initPatterns();
        String html = Text;
        Matcher m = outlink.matcher(html);
        html = m.replaceAll("<a href=\"$1\">$1</a>");
        // html = doLinks(outlink, html, db);
        html = doLinks(link, html, db);
        com.cforcoding.jmd.MarkDown ma = new MarkDown();
        html = ma.transform(html);

        // /////////////end
        long duration = System.currentTimeMillis() - start;
        Log.e("SAVE", "Saved in " + duration + " ms");
        return html;
    }

    private String doLinks(Pattern linkPattern, String html, Database db) {
        Matcher m = link.matcher(html);
        // check each link if it exists
        while (m.find()) {
            // check link
            String name = m.group(1);
            if (db.pageExists(name)) {
                html = m.replaceFirst("<a href=\"wiki://$1\">$1</a>");
            } else {
                html = m.replaceFirst("<a style=\"color:red\" href=\"wiki://$1\">$1</a>");
            }
            m = link.matcher(html);
        }
        return html;
    }
}