package de.uvwxy.daisy.nodemap.guiviews;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;

public abstract class CV {
	public String ID;
	protected String title = null;
	protected int iconID;

	private boolean enabled = true;
	protected boolean isMenuAction = false;
	
	public boolean isMenuAction(){
		return isMenuAction;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public CV(String id, String title, int iconID) {
		this.ID = id;
		this.title = title;
		this.iconID = iconID;
	}

	public View onCreateListItemView(LayoutInflater inflater, View convertView, ViewGroup parent) {
		View rootView = inflater.inflate(R.layout.content_item_simple, parent, false);
		ImageView tvIcon = (ImageView) rootView.findViewById(R.id.ivItemIcon);
		tvIcon.setImageResource(iconID);
		TextView tvLabel = (TextView) rootView.findViewById(R.id.tvLabel);
		tvLabel.setText(title);
		tvLabel.setEnabled(enabled);
		tvIcon.setEnabled(enabled);	
		return rootView;
	}

	
	
	public abstract View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	public void onSaveInstanceState(Bundle outState) {

	}
	
	public void onDestroyView(){
		
	}

	public void onPause() {

	}

	public void onResume() {

	}

	public void onDestroy() {

	}
	
	public void onClickAction(){
		
	}

	@Override
	public String toString() {
		return title;
	}

	public boolean onCreateOptionsMenu(Menu mENU, MenuInflater inflater) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
}
