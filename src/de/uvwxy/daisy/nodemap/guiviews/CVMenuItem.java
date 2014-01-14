package de.uvwxy.daisy.nodemap.guiviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class CVMenuItem extends CV {
	private OnClickListener onClick;

	public CVMenuItem(OnClickListener onClick, String id, String title, int iconID) {
		super(id, title, iconID);
		this.onClick = onClick;
		isMenuAction = true;
	}

	@Override
	public void onClickAction() {
		super.onClickAction();
		if (onClick != null) {
			onClick.onClick(null);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return null;
	}

}
