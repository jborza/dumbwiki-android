package com.borzaindustries.dumbwiki;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditActivity extends Activity {
    String oldname;
    String name;
    String text;
    EditText editName;
    EditText editText;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        //load intent data
        oldname = name = getIntent().getExtras().getString(mainActivity.NAME);
        text = getIntent().getExtras().getString(mainActivity.TEXT);
        editName = (EditText) findViewById(R.id.name);
        editText = (EditText) findViewById(R.id.text);

        editName.setText(name);
        editText.setText(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                Intent data = new Intent();
                data.putExtra(mainActivity.NAME, getName());
                data.putExtra(mainActivity.TEXT, getText());
                data.putExtra(mainActivity.OLDNAME, oldname);
                setResult(RESULT_OK, data);
                finish();
                return true;
            case R.id.menu_cancel:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getName() {
        return editName.getText().toString();
    }

    private String getText() {
        return editText.getText().toString();
    }
}
