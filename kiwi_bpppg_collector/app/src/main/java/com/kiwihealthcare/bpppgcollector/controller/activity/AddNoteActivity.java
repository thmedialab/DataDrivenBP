package com.kiwihealthcare.bpppgcollector.controller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kiwihealthcare.bpppgcollector.R;

public class AddNoteActivity extends Activity {

	private String noteString;
	private EditText noteEditText;
	private Button saveButton;
	private Intent addNoteIntent;
	private LinearLayout noteList;
	private ImageView backImage;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_note);

		initContentView();
	}

	private void initContentView() {
		addNoteIntent = getIntent();
		noteString = addNoteIntent.getStringExtra("note");
		noteEditText = (EditText) findViewById(R.id.add_note_notes_edit);
		noteEditText.setText(noteString);

		saveButton = (Button) findViewById(R.id.add_note_save_button);
		saveButton.setOnClickListener(onSaveButtonClickListener);

		noteList = (LinearLayout) findViewById(R.id.add_note_notes_layout);
		String[] freqNote = getResources()
				.getStringArray(R.array.note_frequent);

		for (int i = 0; i < freqNote.length; i++) {
			Button button = new Button(this);
			button.setText(freqNote[i]);
			// if (i == 0) {
			// button.setBackgroundDrawable(getResources().getDrawable(
			// R.drawable.kiwi_list_top_background));
			// } else if (i == freqNote.length - 1) {
			// button.setBackgroundDrawable(getResources().getDrawable(
			// R.drawable.kiwi_list_bottom_background));
			// } else {
			// button.setBackgroundDrawable(getResources().getDrawable(
			// R.drawable.kiwi_list_single_background));
			// }
			// button.setBackgroundResource(R.drawable.kiwi_list_single_background);
			button.setOnClickListener(getOnClickListener(button));
			noteList.addView(button);

			backImage = (ImageView) findViewById(R.id.add_note_back_image);
			backImage.setOnClickListener(onSaveButtonClickListener);
		}
	}

	private View.OnClickListener getOnClickListener(final Button button) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				noteEditText.append(' ' + button.getText().toString() + ' ');
			}
		};
	}

	private View.OnClickListener onSaveButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			noteString = noteEditText.getText().toString();
			addNoteIntent.putExtra("noteRes", noteString);
			setResult(RESULT_OK, addNoteIntent);
			finish();
		}
	};

}
