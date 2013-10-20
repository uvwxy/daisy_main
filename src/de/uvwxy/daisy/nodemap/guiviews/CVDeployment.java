package de.uvwxy.daisy.nodemap.guiviews;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.proto.Messages;
import de.uvwxy.helper.DateTools;
import de.uvwxy.helper.IntentTools;
import de.uvwxy.helper.IntentTools.ReturnStringECallback;
import de.uvwxy.helper.StringE;

public class CVDeployment extends CV {
	private View view1 = null;

	private TextView textView1 = null;
	private TextView textView2 = null;
	private TextView textView3 = null;
	private TextView textView4 = null;
	private TextView tvDeploymentOnDiskInfo = null;
	private TextView tvDeploymentDate = null;
	private TextView tvDeploymentCurrent = null;

	private Button btnDeploymentCreate = null;
	private Button btnDeploymentChoose = null;
	private Button btnDeploymentSave = null;
	private Button btnDeploymentDelete = null;

	private EditText etDeploymentName = null;

	public CVDeployment(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	private ReturnStringECallback<String> selectAction = new ReturnStringECallback<String>() {

		@Override
		public void result(StringE<String> se) {
			if (se != null) {

				String res = CM.DAISY.loadDeployment(se.e);
				Toast.makeText(CM.CTX, "Selected deployment \"" + se.s + "\":\n(" + se.e + ")\n\nLoad State: " + res, Toast.LENGTH_LONG).show();
				Messages.DeploymentHeader header = CM.DAISY.loadDeploymentHeader(se.e);
				etDeploymentName.setText(header.getDeploymentName());
				tvDeploymentDate.setText(DateTools.getDateTimeLong(CM.CTX, header.getIdAndTimeStamp()));
				btnDeploymentSave.setEnabled(true);
				updateCurrentText();
				CM.LOGS.add("Deployment", "Selected deployment \"" + se.s + "\":\n(" + se.e + ")\n\nLoad State: " + res);

			} else {
				Toast.makeText(CM.CTX, "Nothing selected or error", Toast.LENGTH_LONG).show();
				CM.LOGS.add("Deployment", "Nothing selected or error");
			}

		}
	};

	protected ReturnStringECallback<String> deleteAction = new ReturnStringECallback<String>() {

		@Override
		public void result(final StringE<String> stringE) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(CM.CTX);

			alertDialog.setPositiveButton("Delete!", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					File f = new File(stringE.e);
					f.delete();
					f = new File(stringE.e + ".seq");
					f.delete();
				}
			});

			alertDialog.setNegativeButton("Cancel", null);
			String msg = "Permanently delete deployment \"" + stringE.s + "\"";
			msg += "\n\n" + stringE.e;
			msg += "\n\n" + stringE.e + ".seq";
			msg += "\n\nNote: images/maps are not deleted.";

			alertDialog.setMessage(msg);
			alertDialog.setTitle("Delete Deployment?");
			alertDialog.show();
		}
	};

	private void updateCurrentText() {
		String temp = "Current Depoyment: \"" + CM.DAISY.getDeploymentName();
		temp += "\" Created on " + DateTools.getDateTimeLong(CM.CTX, CM.DAISY.getIdAndTimeStamp());
		temp += "\nDeployment OK: " + (CM.DAISY.deplOK());
		tvDeploymentCurrent.setText(temp);
	}

	private void initGUI(View rootView) {
		textView1 = (TextView) rootView.findViewById(R.id.lblSlider);
		textView2 = (TextView) rootView.findViewById(R.id.tvBubbleNodeBattery);
		etDeploymentName = (EditText) rootView.findViewById(R.id.etDeploymentName);
		textView3 = (TextView) rootView.findViewById(R.id.tvDeviceLandmark);
		tvDeploymentDate = (TextView) rootView.findViewById(R.id.tvDeploymentDate);
		btnDeploymentCreate = (Button) rootView.findViewById(R.id.btnDeploymentCreate);
		view1 = (View) rootView.findViewById(R.id.View1);
		textView4 = (TextView) rootView.findViewById(R.id.textView4);
		tvDeploymentOnDiskInfo = (TextView) rootView.findViewById(R.id.tvDeploymentOnDiskInfo);
		btnDeploymentChoose = (Button) rootView.findViewById(R.id.btnDeploymentChoose);
		btnDeploymentSave = (Button) rootView.findViewById(R.id.btnDeploymentSave);
		btnDeploymentDelete = (Button) rootView.findViewById(R.id.btnDeploymentDelete);
		tvDeploymentCurrent = (TextView) rootView.findViewById(R.id.tvDeploymentCurrent);

		btnDeploymentSave.setEnabled(false);

		btnDeploymentChoose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CM.DAISY.showSelectDeploymentDialog(selectAction);
			}
		});

		btnDeploymentDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				IntentTools.userSelectStringE(CM.CTX, "Delete a deployment", CM.DAISY.getAllDeploymentFileNames(), deleteAction);
			}
		});

		btnDeploymentCreate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				CM.DAISY.createDeployment(etDeploymentName.getText().toString(), System.currentTimeMillis());
				boolean noFail = CM.DAISY.saveActiveDeployment();
				if (noFail) {
					Toast.makeText(CM.CTX, "Created new deployment", Toast.LENGTH_LONG).show();
					updateCurrentText();
					setUIDeplOK();
					CM.LOGS.add("Deployment", "Created new deployment");
				} else {
					Toast.makeText(CM.CTX, "Failed to create new deployment", Toast.LENGTH_LONG).show();
					CM.LOGS.add("Deployment", "Failed to create new deployment");
					setUIDeplNotOK();
				}
			}
		});

		btnDeploymentSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CM.DAISY.saveActiveDeployment();
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_view_deployment, container, false);

		initGUI(rootView);

		tvDeploymentDate.setText("[now ~" + DateTools.getDateTime(CM.CTX, System.currentTimeMillis()) + "]");

		tvDeploymentOnDiskInfo.setText(String.format(CM.CTX.getString(R.string.there_are_d_deployments_on_this_device), CM.DAISY.getNumberOfDeployments()));
		return rootView;
	}

	@Override
	public void onResume() {
		if (!CM.DAISY.deplOK()) {
			setUIDeplNotOK();
			return;
		}

		etDeploymentName.setText(CM.DAISY.getDeploymentName());

		updateCurrentText();
		setUIDeplOK();
	}

	public void setUIDeplNotOK() {
		btnDeploymentSave.setEnabled(false);
		btnDeploymentCreate.setEnabled(true);
		etDeploymentName.setEnabled(true);
	}

	public void setUIDeplOK() {
		btnDeploymentSave.setEnabled(true);
		btnDeploymentCreate.setEnabled(false);
		etDeploymentName.setEnabled(false);
	}

}
