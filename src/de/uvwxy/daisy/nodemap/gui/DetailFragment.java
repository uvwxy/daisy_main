package de.uvwxy.daisy.nodemap.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.guiviews.CV;

/**
 * A fragment representing a single NodeView detail screen. This fragment is
 * either contained in a {@link MainListActivity} in two-pane mode (on tablets)
 * or a {@link DetailActivity} on handsets.
 */
public class DetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private CV mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public DetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = CM.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = mItem.onCreateView(inflater, container, savedInstanceState);
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mItem.onDestroyView();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mItem.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		mItem.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		mItem.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mItem.onResume();
	}
}
