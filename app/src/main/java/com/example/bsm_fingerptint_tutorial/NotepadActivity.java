package com.example.bsm_fingerptint_tutorial;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleCoroutineScope;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class NotepadActivity extends AppCompatActivity {

    private List<Note> noteList;
    private LinearLayout notesContainer;
    private static final String SHARED_NAME_NOTES = "Notatki";

    Button buttonLogout, buttonAddNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);

        noteList = new ArrayList<>();
        notesContainer = findViewById(R.id.notes_container);

        loadNotesFromPreferencesToList();
        displayNotes();

        buttonLogout = findViewById(R.id.btn_logout);
        buttonAddNote = findViewById(R.id.btn_add_note);

        buttonLogout.setOnClickListener(view -> logOut());
        buttonAddNote.setOnClickListener(view -> showAddNoteDialog());

    }

    private void logOut(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showAddNoteDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.create_note_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView);
        builder.setTitle("Create new note");

        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            EditText noteTitleEditText = dialogView.findViewById(R.id.note_title_edit_text);
            EditText noteContentEditText = dialogView.findViewById(R.id.note_content_edit_text);

            String title = noteTitleEditText.getText().toString();
            String content = noteContentEditText.getText().toString();

            if(!title.isEmpty() && !content.isEmpty()){
                Note note = new Note();
                note.setTitle(title);
                note.setContent(content);

                noteList.add(note);

                saveNotesToPreferences("add");

                createNoteView(note);
                Toast.makeText(NotepadActivity.this, "Note saved!", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showEditNoteDialog(Note note){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.create_note_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Edit note");

        EditText noteTitleEditText = dialogView.findViewById(R.id.note_title_edit_text);
        EditText noteContentEditText = dialogView.findViewById(R.id.note_content_edit_text);
        noteTitleEditText.setText(note.getTitle());
        noteContentEditText.setText(note.getContent());

        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            String title = noteTitleEditText.getText().toString();
            String content = noteContentEditText.getText().toString();

            if (!title.isEmpty() && !content.isEmpty()){

                deleteNoteAndRefresh(note);

                note.setTitle(title);
                note.setContent(content);

                noteList.add(note);
                createNoteView(note);

                saveNotesToPreferences("add");

            }else {
                Toast.makeText(NotepadActivity.this, "Enter title and content!", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveNotesToPreferences(String mode){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_NAME_NOTES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (mode.equals("del")){
            int noteCount = sharedPreferences.getInt("_notecount_", 0);
            for(int i=0; i<noteCount; i++){
                editor.remove(i + "_title_");
                editor.remove(i + "_content_");
            }
        }

        editor.putInt("_notecount_", noteList.size());
        for(int i=0; i<noteList.size(); i++){
            Note note = noteList.get(i);
            editor.putString(i + "_title_", note.getTitle());
            editor.putString(i + "_content_", note.getContent());
        }

        editor.apply();
    }

    private void loadNotesFromPreferencesToList(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_NAME_NOTES, MODE_PRIVATE);
        int noteCount = sharedPreferences.getInt("_notecount_", 0);

        for(int i=0; i<noteCount; i++){
            String title = sharedPreferences.getString(i + "_title_", "");
            String content = sharedPreferences.getString(i + "_content_", "");

            Note note = new Note();
            note.setTitle(title);
            note.setContent(content);

            noteList.add(note);
        }
    }

    private void createNoteView(final Note note){
        @SuppressLint("InflateParams") View noteView = getLayoutInflater().inflate(R.layout.note_item, null);
        TextView noteTitleTextView = noteView.findViewById(R.id.note_title_text_view);
        TextView noteContentTextView = noteView.findViewById(R.id.note_content_text_view);
        Button deleteNoteButton = noteView.findViewById(R.id.btn_delete_note);

        noteTitleTextView.setText(note.getTitle());
        noteContentTextView.setText(note.getContent());

        deleteNoteButton.setOnClickListener(view -> showDeleteDialog(note));

        noteView.setOnLongClickListener(view -> {
            showEditNoteDialog(note);
            return true;
        });

        notesContainer.addView(noteView);
    }

    private void refreshNotesView(){
        notesContainer.removeAllViews();
        displayNotes();
    }

    private void displayNotes(){
        for(Note note : noteList){
            createNoteView(note);
        }
    }

    private void deleteNoteAndRefresh(Note note){
        noteList.remove(note);
        saveNotesToPreferences("del");
        refreshNotesView();
    }

    private void showDeleteDialog(final Note note){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete this note");
        builder.setMessage("Are you sure you want to delete it?");
        builder.setPositiveButton("Delete", (dialogInterface, i) -> {
            deleteNoteAndRefresh(note);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}













