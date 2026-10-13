package com.ucsal.avaliacaon1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class WaterDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "consumo_agua.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_CONFIG = "configuracoes";
    private static final String TABLE_CONSUMOS = "consumos";

    public WaterDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlConfig = "CREATE TABLE " + TABLE_CONFIG + " (" +
                "id INTEGER PRIMARY KEY, " +
                "meta_diaria INTEGER NOT NULL)";

        db.execSQL(sqlConfig);

        String sqlConsumos = "CREATE TABLE " + TABLE_CONSUMOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "volume_ml INTEGER NOT NULL, " +
                "data_hora TEXT NOT NULL, " +
                "data TEXT NOT NULL)";

        db.execSQL(sqlConsumos);

        ContentValues valores = new ContentValues();
        valores.put("id", 1);
        valores.put("meta_diaria", 2000);

        db.insert(TABLE_CONFIG, null, valores);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        // Futuras alterações na estrutura do banco serão tratadas aqui.
    }

    public int buscarMeta() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT meta_diaria FROM " + TABLE_CONFIG + " WHERE id = 1",
                null
        );

        int meta = 2000;

        try {
            if (cursor.moveToFirst()) {
                meta = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }

        return meta;
    }

    public boolean salvarMeta(int meta) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("meta_diaria", meta);

        int linhasAlteradas = db.update(
                TABLE_CONFIG,
                valores,
                "id = ?",
                new String[]{"1"}
        );

        return linhasAlteradas > 0;
    }

    public boolean registrarConsumo(int volume) {
        SQLiteDatabase db = getWritableDatabase();

        Date agora = new Date();

        String dataHora = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(agora);

        String data = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(agora);

        ContentValues valores = new ContentValues();
        valores.put("volume_ml", volume);
        valores.put("data_hora", dataHora);
        valores.put("data", data);

        long resultado = db.insert(TABLE_CONSUMOS, null, valores);

        return resultado != -1;
    }

    public int buscarConsumoDoDia() {
        SQLiteDatabase db = getReadableDatabase();
        String hoje = obterDataAtual();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(volume_ml), 0) FROM " +
                        TABLE_CONSUMOS + " WHERE data = ?",
                new String[]{hoje}
        );

        int total = 0;

        try {
            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }

        return total;
    }

    public List<WaterRecord> buscarRegistrosDoDia() {
        SQLiteDatabase db = getReadableDatabase();
        String hoje = obterDataAtual();

        Cursor cursor = db.rawQuery(
                "SELECT volume_ml, data_hora, data FROM " +
                        TABLE_CONSUMOS +
                        " WHERE data = ? ORDER BY id ASC",
                new String[]{hoje}
        );

        List<WaterRecord> registros = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                int volume = cursor.getInt(0);
                String dataHora = cursor.getString(1);
                String data = cursor.getString(2);

                WaterRecord registro =
                        new WaterRecord(volume, dataHora, data);

                registros.add(registro);
            }
        } finally {
            cursor.close();
        }

        return registros;
    }

    private String obterDataAtual() {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());
    }

    public void limparRegistrosAntigos() {
        SQLiteDatabase db = getWritableDatabase();
        String hoje = obterDataAtual();

        db.delete(
                TABLE_CONSUMOS,
                "data <> ?",
                new String[]{hoje}
        );
    }
}
