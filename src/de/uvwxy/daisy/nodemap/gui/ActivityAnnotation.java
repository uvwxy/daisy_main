package de.uvwxy.daisy.nodemap.gui;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.proto.Messages.Annotation;
import de.uvwxy.daisy.protocol.DaisyData;
import de.uvwxy.daisy.protocol.busmessages.NeedsSync;

public class ActivityAnnotation extends Activity {
	private static final String LOG_TAG = "ActivityAnnotation";

	private static String mFileName = null;
	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;

	private ToggleButton tbtnAnnotationRecording;
	private ToggleButton tbtnAnnotationPlayback;
	private Button btnAnnotationSave;
	private EditText etAnnotationText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_annotation);

		mFileName = CM.DAISY.getDeploymentPath(DaisyData.DAISY_RECORDINGS_FOLDER) + "recording_" + System.currentTimeMillis() + ".amr";

		tbtnAnnotationPlayback = (ToggleButton) findViewById(R.id.tbtnAnnotationPlayback);
		tbtnAnnotationRecording = (ToggleButton) findViewById(R.id.tbtnAnnotationRecording);
		btnAnnotationSave = (Button) findViewById(R.id.btnAnnotationSave);
		etAnnotationText = (EditText) findViewById(R.id.etAnnotationText);

		btnAnnotationSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});

		tbtnAnnotationRecording.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!tbtnAnnotationRecording.isChecked()) {
					stopRecording();
					tbtnAnnotationPlayback.setEnabled(true);
				} else {
					startRecording();
					tbtnAnnotationPlayback.setEnabled(false);
				}
			}
		});

		tbtnAnnotationPlayback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!tbtnAnnotationPlayback.isChecked()) {
					stopPlaying();
					tbtnAnnotationRecording.setEnabled(true);
				} else {
					startPlaying();
					tbtnAnnotationRecording.setEnabled(false);
				}
			}
		});
	}

	private void save() {
		Annotation.Builder b = Annotation.newBuilder();

		boolean ok = false;

		String text = etAnnotationText.getText().toString();
		if (text != null && !text.equals("")) {
			b.setNote(text);
			ok = true;
		}

		File f = new File(mFileName);
		if (f.exists()) {
			b.setAudioFile(f.getName()); // folder is derived from external dir + deployment id
			ok = true;
		}

		if (!ok) {
			Toast.makeText(this, "Recording or Text missing", Toast.LENGTH_LONG).show();
			return;
		}

		b.setTimestamp(System.currentTimeMillis());
		b.setLocation(CM.LOC.getLastProtoLocation());

		b.setTag(CM.DAISY.getNextTag());
		Annotation annotation = b.build();
		CM.BUS.post(annotation);
		CM.BUS.post(new NeedsSync());
		finish();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

}
