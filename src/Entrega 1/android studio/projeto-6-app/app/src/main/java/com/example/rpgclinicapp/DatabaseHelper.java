package com.example.rpgclinicapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nome do arquivo que ficará escondido no celular
    private static final String DATABASE_NAME = "RPGClinicLocal.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabela para salvar o Prontuário (Evolução) offline
        db.execSQL("CREATE TABLE prontuario_local (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "paciente_id INTEGER," +
                "dor INTEGER," +
                "data TEXT," +
                "observacao TEXT)");

        // Tabela para salvar Check-ins que falharam por falta de internet
        db.execSQL("CREATE TABLE checkins_pendentes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "paciente_id INTEGER," +
                "paciente_nome TEXT," +
                "exercicio_nome TEXT," +
                "dor INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS prontuario_local");
        db.execSQL("DROP TABLE IF EXISTS checkins_pendentes");
        onCreate(db);
    }
}