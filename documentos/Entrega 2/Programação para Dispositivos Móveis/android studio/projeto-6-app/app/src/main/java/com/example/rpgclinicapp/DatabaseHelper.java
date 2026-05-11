package com.example.rpgclinicapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nome do arquivo que ficará escondido no celular
    private static final String DATABASE_NAME = "RPGClinicLocal.db";

    // Aumentamos a versão para 2 para o Android entender que o banco mudou
    private static final int DATABASE_VERSION = 2;

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
                "observacao TEXT," +
                "comentario TEXT)"); // Novo campo adicionado

        // Tabela para salvar Check-ins que falharam por falta de internet
        db.execSQL("CREATE TABLE checkins_pendentes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "paciente_id INTEGER," +
                "paciente_nome TEXT," +
                "exercicio_nome TEXT," +
                "dor INTEGER," +
                "comentario TEXT)"); // Novo campo adicionado
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Como estamos em fase de desenvolvimento, se a versão mudar,
        // ele apaga as tabelas antigas e cria as novas com a coluna comentário
        db.execSQL("DROP TABLE IF EXISTS prontuario_local");
        db.execSQL("DROP TABLE IF EXISTS checkins_pendentes");
        onCreate(db);
    }
}