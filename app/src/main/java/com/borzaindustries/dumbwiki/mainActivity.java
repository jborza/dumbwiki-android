package com.borzaindustries.dumbwiki;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.Stack;

public class mainActivity extends Activity {
    final int DIALOG_CREATE = 1;
    public final int REQUEST_EDIT = 0;
    public final static String NAME = "NAME";
    public static final String TEXT = "TEXT";
    public final static String OLDNAME = "OLDNAME";
    private Database db;
    private WikiPage page;
    java.util.Stack<String> history;
    private String currentPage;
    private WebView webView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        history = new Stack<String>();
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("wiki://")) {
                    loadWikiUrl(url);
                    return true;
                }
                // push current page?
                return false;
            }
        });

        db = new Database(this);
        db.loadPages();
        db.initializeIfFirstRun();
        goHome();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void goBack() {
        // pop page from history and go there
        if (history.isEmpty())
            return;
        String pageName = history.pop();
        navigate(pageName, false);
    }

    private void goHome() {
        loadWikiUrl("wiki://home");
    }

    public void loadWikiUrl(String url) {
        String pageName = url.substring("wiki://".length()).toLowerCase();
        navigate(pageName, true);
    }

    private void navigate(String pageName, boolean push) {
        page = db.getPage(pageName);
        if (page == null) {
            openInEdit(pageName, "");
            return;
        }
        setTitle(pageName + " - " + getString(R.string.app_name));
        String data = page.RenderToHTML(db);
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadData(URLEncoder.encode(data).replaceAll("\\+", "%20"),
                "text/html", "utf-8");
        // push page to stack, only if it's not already at the top of the stack
        if (currentPage != null && push) {
            pushPage(currentPage);
        }
        currentPage = pageName;
    }

    private void pushPage(String current) {
        // if the page at top of the stack is current page, don't do anything
        if (!history.empty() && history.peek().equals(currentPage))
            return;
        history.push(current);
    }

    private void openInEdit(String name, String text) {
        Intent intent = new Intent(getBaseContext(), EditActivity.class);
        intent.putExtra(NAME, name);
        intent.putExtra(TEXT, text);
        startActivityForResult(intent, REQUEST_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT) {
            if (resultCode != RESULT_OK) {
                // cancel pressed
                return;
            }
            // navigate to new page
            // save current edited page
            String name = data.getExtras().getString(NAME);
            String text = data.getExtras().getString(TEXT);
            String oldname = data.getExtras().getString(OLDNAME);
            // delete oldname
            if (!oldname.equals(name))
                db.deletePage(oldname);
            // boolean deleted = db.setPage(name, text);
            db.setPage(name, text);
            saveAll();
            /*
			 * if (deleted) goBack(); else
			 */
            navigate(name, true);
        }
    }

    private void saveAll() {
        db.savePages();
        Toast.makeText(this, "Saved.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CREATE:
                // create a dialog that asks if crearea a new page in edit
                return null;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit:
                openInEdit(page.Name, page.Text);
                return true;
            case R.id.menu_home:
                goHome();
                return true;
            case R.id.menu_back:
                goBack();
                return true;
            case R.id.menu_backup:
                Toast.makeText(this, "Saving backup to /sdcard/dumbwiki.bak",
                        Toast.LENGTH_SHORT).show();
                db.backup(); // TODO backup() should return bool
                Toast.makeText(this, "Backup saved to /sdcard/dumbwiki.bak",
                        Toast.LENGTH_SHORT).show();
                // db.zap();
                return true;
            case R.id.menu_restore:
                Toast.makeText(this, "Restoring from /sdcard/dumbwiki.bak",
                        Toast.LENGTH_SHORT).show();
                db.restore(); // TODO restore() should return bool
                Toast.makeText(this, "Restored from /sdcard/dumbwiki.bak",
                        Toast.LENGTH_SHORT).show();
                goHome();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}