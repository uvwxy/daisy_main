package de.uvwxy.daisy.nodemap.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.guicontent.CMLocation;
import de.uvwxy.daisy.nodemap.guicontent.CMSensors;
import de.uvwxy.daisy.nodemap.guiviews.CV;
import de.uvwxy.daisy.protocol.DaisyData;
import de.uvwxy.daisy.protocol.DaisyNetwork;
import de.uvwxy.daisy.protocol.DaisyProtocolBroadCastQueue;
import de.uvwxy.helper.IntentTools;
import de.uvwxy.helper.SettingsCallback;
import de.uvwxy.net.DummyConnectionCallback;
import de.uvwxy.net.bluetooth.BTConnectionSetup;

/**
 * An activity representing a list of NodeViews. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link DetailActivity} representing item details. On tablets, the activity
 * presents the list of items and item details side-by-side using two vertical
 * panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MainListFragment} and the item details (if present) is a
 * {@link DetailFragment}.
 * <p>
 * This activity also implements the required {@link MainListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class MainListActivity extends FragmentActivity implements MainListFragment.Callbacks {

	private static final int SEED = 2608;
	private static final String DAISY_PREFS = "DAISY_PREFS";
	private static final String DAISY_USER_NAME = "DAISY_USER_NAME";
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	private String ITEM_ID;

	protected PowerManager.WakeLock mWakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nodeview_list);
		CM.CTX = this;
		CM.ACT = this;

		if (CM.DAISY == null) {
			CM.DAISY = new DaisyData(CM.ctxProxy, SEED, CM.BUS);
		}

		if (CM.DAISYNET == null) {
			CM.DAISYNET = new DaisyNetwork(CM.ctxProxy, CM.DAISY);
		}

		SettingsCallback callbackSettingsName = new SettingsCallback() {

			@Override
			public void onValue(String valueId, String value) {
				CM.DAISY.setLocalUserName(value);
			}
		};

		if (CM.DAISY_SYNC_QUEUE == null) {
			CM.DAISY_SYNC_QUEUE = new DaisyProtocolBroadCastQueue(CM.DAISY, CM.DAISYNET, this);
		}

		if (CM.LOC == null) {
			CM.LOC = new CMLocation(this);
		}

		if (CM.SENS == null) {
			CM.SENS = new CMSensors(this);
		}

		registerAllBusClients();

		IntentTools.getFromSettingsOrFromUser(this, DAISY_PREFS, DAISY_USER_NAME, "User Name",
				"Set the user name for this device", "Settings", false, callbackSettingsName);

		if (findViewById(R.id.nodeview_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;
			CM.TWO_PANE = true;
			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((MainListFragment) getSupportFragmentManager().findFragmentById(R.id.nodeview_list))
					.setActivateOnItemClick(true);
		}

		BTConnectionSetup btc = new BTConnectionSetup(getApplicationContext(), new DummyConnectionCallback());
		btc.setInterfaceEnabled(true);

		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// TODO: fix this deprecation warning:
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		this.mWakeLock.acquire();

		// TODO: If exposing deep links into your app, handle intents here.
	}

	public void registerAllBusClients() {
		CM.BUS_RECEIVER.register(CM.BUS);
		CM.DAISY.register(CM.BUS);
	}

	/**
	 * Callback method from {@link MainListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {

		// don't click on disabled or non existing elements
		CV c = CM.ITEM_MAP.get(id);
		if (c == null || !c.isEnabled()) {
			return;
		}

		if (c.isMenuAction()) {
			// just handle click and don't open sth.
			c.onClickAction();
			return;
		}

		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			ITEM_ID = id;
			arguments.putString(DetailFragment.ARG_ITEM_ID, id);
			DetailFragment fragment = new DetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction().replace(R.id.nodeview_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, DetailActivity.class);
			detailIntent.putExtra(DetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_CANCELED) {
			return;
		}

		if (CM.globalOnResultHandling(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}

		if (mTwoPane) {
			if (ITEM_ID != null) {
				CM.ITEM_MAP.get(ITEM_ID).onActivityResult(requestCode, resultCode, data);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		if (mTwoPane) {
			if (ITEM_ID != null) {
				CM.ITEM_MAP.get(ITEM_ID).onPause();
			}
		}

		super.onPause();
	}

	@Override
	public void onResume() {
		CM.CTX = this;
		CM.ACT = this;

		if (mTwoPane) {
			if (ITEM_ID != null) {
				CM.ITEM_MAP.get(ITEM_ID).onResume();
			}
		}
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (CM.ITEM_MAP.get(ITEM_ID) == null) {
			onItemSelected("idSettings");
			return true;
		}

		return CM.ITEM_MAP.get(ITEM_ID).onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mTwoPane) {

			if (ITEM_ID != null) {
				CV mItem = CM.ITEM_MAP.get(ITEM_ID);
				MenuInflater inflater = getMenuInflater();
				return mItem.onCreateOptionsMenu(CM.MENU, inflater);
			} else {
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.dummy_menu, menu);
				CM.MENU = menu;
				return true;
			}
		}
		return false;
	}

	@SuppressLint("Wakelock")
	@Override
	protected void onDestroy() {
		this.mWakeLock.release();

		if (mTwoPane) {
			if (ITEM_ID != null) {
				CM.ITEM_MAP.get(ITEM_ID).onDestroy();
			}
		}

		Log.i("DAISY", "DESTROY BT REG");
		CM.DAISYNET.btStopDiscovery();
		CM.DAISYNET.btStopListen();

		unregisterAllBusClients();

		if (CM.DAISY != null) {
			Handler h = new Handler(CM.CTX.getMainLooper());

			h.post(new Runnable() {

				@Override
				public void run() {
					CM.DAISY.saveActiveDeployment();
				}
			});
		}
		CM.destroy();

		super.onDestroy();
	}

	public void unregisterAllBusClients() {
		CM.BUS_RECEIVER.unregister(CM.BUS);
		CM.DAISY.unregister(CM.BUS);
	}

}
