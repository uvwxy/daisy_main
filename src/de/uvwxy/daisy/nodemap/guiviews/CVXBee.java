package de.uvwxy.daisy.nodemap.guiviews;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.listobjects.APIMessageArrayAdapter;
import de.uvwxy.helper.IntentTools;
import de.uvwxy.xbee.apimode.messages.APIMessage;
import de.uvwxy.xbee.commands.ATCommand;

public class CVXBee extends CV {

	private static final int DEFAULT_BAUDRATE = 38400;

	private static final String BAUD_RATE = "BAUD_RATE";

	private static final String BAUD_SETTINGS = "BAUD_SETTINGS";

	private ATCommand selectedCommand = null;

	private Button btnXBeeOpen = null;
	private Spinner spXBeeCommands = null;
	private EditText etXBeeParams = null;
	private Button btnXBeeSend = null;
	private Button btnXBeeClose = null;
	private EditText etXBeeBaud = null;
	private ListView lvXBeeReceivedMessages = null;

	private static APIMessageArrayAdapter apiMessageAdapter;
	private ArrayList<APIMessage> apiMessageList = new ArrayList<APIMessage>();

	private void initGUI(View rootView) {
		btnXBeeOpen = (Button) rootView.findViewById(R.id.btnXBeeOpen);
		spXBeeCommands = (Spinner) rootView.findViewById(R.id.spXBeeCommands);
		etXBeeParams = (EditText) rootView.findViewById(R.id.etXBeeParams);
		btnXBeeSend = (Button) rootView.findViewById(R.id.btnXBeeSend);
		btnXBeeClose = (Button) rootView.findViewById(R.id.btnXBeeClose);
		lvXBeeReceivedMessages = (ListView) rootView.findViewById(R.id.lvXBeeReceivedMessages);
		etXBeeBaud = (EditText) rootView.findViewById(R.id.etXBeeBaud);
		String baud = "" + IntentTools.getSettings(CM.CTX, BAUD_SETTINGS).getInt(BAUD_RATE, DEFAULT_BAUDRATE);
		etXBeeBaud.setText(baud);
	}

	private void initGUIClicks() {
		btnXBeeOpen.setOnClickListener(click);
		btnXBeeSend.setOnClickListener(click);
		btnXBeeClose.setOnClickListener(click);

		ATCommand[] objects = ATCommand.values();
		ArrayAdapter<ATCommand> adapterATCommand = new ArrayAdapter<ATCommand>(CM.CTX,
				android.R.layout.simple_spinner_item, objects);
		spXBeeCommands.setAdapter(adapterATCommand);
		spXBeeCommands.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedCommand = ATCommand.values()[position];
				Log.i("XBEE_VIEW", "Selected " + selectedCommand);
			}

			@Override
			public void onNothingSelected(AdapterView<?> view) {
				Log.i("XBEE_VIEW", "Selected nothing");
			}
		});
	}

	private OnClickListener click = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.equals(btnXBeeOpen)) {
				int baud = DEFAULT_BAUDRATE;
				if (etXBeeBaud.getText() != null && !etXBeeBaud.getText().toString().equals("")) {
					try {
						baud = Integer.parseInt(etXBeeBaud.getText().toString());

						Editor e = IntentTools.getSettingsEditor(CM.CTX, BAUD_SETTINGS);
						e.putInt(BAUD_RATE, baud);
						IntentTools.saveEditor(e);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (CM.XB.create(CM.CTX, baud)) {
					btnXBeeOpen.setEnabled(false);
					btnXBeeClose.setEnabled(true);
					btnXBeeSend.setEnabled(true);
				} else {
					btnXBeeOpen.setEnabled(true);
					btnXBeeClose.setEnabled(false);
					btnXBeeSend.setEnabled(false);
				}
			} else if (v.equals(btnXBeeSend)) {

				String hex = null;
				if (!etXBeeParams.getText().equals("")) {
					hex = etXBeeParams.getText().toString();
				}
				if (selectedCommand == null) {
					Toast.makeText(CM.CTX, "Select a command type", Toast.LENGTH_SHORT).show();
					return;
				}

				if (hex == null || hex.equals("")) {
					// NO PARAMETERS
					CM.XB.sendAPICommand(selectedCommand, "");
				} else if (hex.length() > 0 && hex.length() % 2 == 0) {
					// HEX PARAMETERS
					CM.XB.sendAPICommand(selectedCommand, hex.toUpperCase());
				} else {
					// WRONG PARAMETERS
					Toast.makeText(CM.CTX, "Invalid input length", Toast.LENGTH_SHORT).show();
				}

			} else if (v.equals(btnXBeeClose)) {
				CM.XB.close();
				btnXBeeOpen.setEnabled(true);
				btnXBeeClose.setEnabled(false);
				btnXBeeSend.setEnabled(false);
			}
		}
	};

	public CVXBee(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_view_xbee, container, false);

		PackageManager PM = CM.CTX.getPackageManager();
		boolean hasUSBHost = PM.hasSystemFeature(PackageManager.FEATURE_USB_HOST);

		if (!hasUSBHost) {
			TextView t = new TextView(CM.CTX);
			t.setText("NO USBHOST");

			return t;
		}

		initGUI(rootView);
		initGUIClicks();

		if (CM.XB.xBeeDevice != null && CM.XB.xBeeDevice.isOpen()) {
			btnXBeeClose.setEnabled(true);
			btnXBeeSend.setEnabled(true);
			btnXBeeOpen.setEnabled(false);
		} else {
			btnXBeeClose.setEnabled(false);
			btnXBeeSend.setEnabled(false);
			btnXBeeOpen.setEnabled(true);
		}

		apiMessageAdapter = new APIMessageArrayAdapter(this, CM.CTX, apiMessageList);

		lvXBeeReceivedMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lvXBeeReceivedMessages.setStackFromBottom(true);
		lvXBeeReceivedMessages.setAdapter(apiMessageAdapter);

		CM.BUS.register(this);
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		CM.BUS.unregister(this);
	}

	@Subscribe
	public void onReceive(APIMessage msg) {
		Log.i("CVXBEE", "received APIMessage msg");
		apiMessageList.add(msg);
		apiMessageAdapter.notifyDataSetChanged();

	}


}