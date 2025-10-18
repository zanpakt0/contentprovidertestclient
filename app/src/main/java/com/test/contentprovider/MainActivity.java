package com.test.contentprovider;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String CONTENT_URI = "content://com.example.heis2025.provider/text_records";
    private static final int LOADER_ID = 1;

    private ListView listView;
    private EditText editText;
    private Button btnRefresh, btnAddRecord, btnSave, btnHideAdd;
    private LinearLayout addRecordSection;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupAdapter();
        setupClickListeners();
        
        // Запускаем загрузчик данных
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void initViews() {
        listView = findViewById(R.id.listView);
        editText = findViewById(R.id.editText);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnAddRecord = findViewById(R.id.btnAddRecord);
        btnSave = findViewById(R.id.btnSave);
        btnHideAdd = findViewById(R.id.btnHideAdd);
        addRecordSection = findViewById(R.id.addRecordSection);
    }

    private void setupAdapter() {
        String[] from = {"text", "_id", "is_deleted", "created_at"};
        int[] to = {R.id.textView, R.id.idView, R.id.statusView, R.id.dateView};
        
        adapter = new SimpleCursorAdapter(this, R.layout.item_record, null, from, to, 0) {
            @Override
            public void setViewText(android.widget.TextView v, String text) {
                if (v.getId() == R.id.statusView) {
                    // Показываем статус записи
                    if ("1".equals(text)) {
                        v.setText("УДАЛЕНО");
                        v.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else {
                        v.setText("АКТИВНО");
                        v.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                } else if (v.getId() == R.id.dateView) {
                    // Форматируем дату
                    try {
                        long timestamp = Long.parseLong(text);
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                        v.setText(sdf.format(new java.util.Date(timestamp)));
                    } catch (Exception e) {
                        v.setText(text);
                    }
                } else if (v.getId() == R.id.idView) {
                    v.setText("ID: " + text);
                } else {
                    super.setViewText(v, text);
                }
            }
        };
        
        listView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> getLoaderManager().restartLoader(LOADER_ID, null, this));
        
        btnAddRecord.setOnClickListener(v -> {
            addRecordSection.setVisibility(View.VISIBLE);
            editText.requestFocus();
        });
        
        btnHideAdd.setOnClickListener(v -> {
            addRecordSection.setVisibility(View.GONE);
            editText.setText("");
        });
        
        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void saveRecord() {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Введите текст", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ContentValues values = new ContentValues();
            values.put("text", text);
            values.put("is_deleted", 0);
            values.put("created_at", System.currentTimeMillis());

            Uri uri = Uri.parse(CONTENT_URI);
            Uri resultUri = getContentResolver().insert(uri, values);
            
            if (resultUri != null) {
                Toast.makeText(this, getString(R.string.record_saved), Toast.LENGTH_SHORT).show();
                editText.setText("");
                addRecordSection.setVisibility(View.GONE);
                // Обновляем данные
                getLoaderManager().restartLoader(LOADER_ID, null, this);
            } else {
                Toast.makeText(this, getString(R.string.record_save_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(CONTENT_URI);
        return new CursorLoader(this, uri, null, null, null, "created_at DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        
        if (data == null || data.getCount() == 0) {
            Toast.makeText(this, getString(R.string.no_records), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}