package de.uvwxy.daisy.nodemap.guiviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;

/**
 * A class to display the deployment data as text form.
 * 
 * @author paul
 * 
 */
public class CVDeploymentData extends CV {

	public CVDeploymentData(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_view_deployment_data, container, false);

		TextView tvDeplData = (TextView) rootView.findViewById(R.id.tvDeplData);
		try {
			tvDeplData.setText(CM.DAISY.toString());
		} catch (Exception e) {
			tvDeplData.setText("Error reading deployment to String");
		}
		return rootView;
	}

}
