package de.uvwxy.daisy.nodemap.guiviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.ar.ARViewFactory;
import de.uvwxy.daisy.nodemap.guicontent.CM;

public class CVARView extends CV {

	public CVARView(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_view_arview, container, false);

		Button btnARViewLaunch = (Button) rootView.findViewById(R.id.btnARViewLaunch);

		if (btnARViewLaunch != null) {
			btnARViewLaunch.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					gl.Color antenna = gl.Color.red();
					gl.Color box = gl.Color.battleshipGrey();
					CM.AR.arview = ARViewFactory.makeSetup(CM.ctxProxy, CM.DAISY.getNodeLocationDataListUniqueLatest(), antenna, box);
					ARViewFactory.showSetup(CM.ACT, CM.AR.arview);
				}
			});

		}
		return rootView;
	}

}
