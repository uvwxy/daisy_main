package de.uvwxy.daisy.nodemap.listobjects;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import de.uvwxy.daisy.nodemap.guiviews.CV;

public class CVArrayAdapter extends ArrayAdapter<CV> {
	private Context ctx;
	private LayoutInflater inflater;
	private ArrayList<CV> contentViewList;

	public CVArrayAdapter(Context ctx, LayoutInflater inflater, int textViewResourceId, List<CV> contentViewList) {
		super(ctx, textViewResourceId, contentViewList);
		this.inflater = inflater;
		this.ctx = ctx;
		this.contentViewList = (ArrayList<CV>) contentViewList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return contentViewList.get(position).onCreateListItemView(inflater, convertView, parent);
	}
}