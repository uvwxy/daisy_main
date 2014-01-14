package de.uvwxy.daisy.nodemap.guicontent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.gui.ActivityAnnotation;
import de.uvwxy.daisy.nodemap.gui.MainListFragment;
import de.uvwxy.daisy.nodemap.guiviews.CV;
import de.uvwxy.daisy.nodemap.guiviews.CVARView;
import de.uvwxy.daisy.nodemap.guiviews.CVBalloon;
import de.uvwxy.daisy.nodemap.guiviews.CVBalloonClient;
import de.uvwxy.daisy.nodemap.guiviews.CVChat;
import de.uvwxy.daisy.nodemap.guiviews.CVDeployment;
import de.uvwxy.daisy.nodemap.guiviews.CVDeploymentData;
import de.uvwxy.daisy.nodemap.guiviews.CVLogs;
import de.uvwxy.daisy.nodemap.guiviews.CVMap;
import de.uvwxy.daisy.nodemap.guiviews.CVMenuItem;
import de.uvwxy.daisy.nodemap.guiviews.CVParticipantScan;
import de.uvwxy.daisy.nodemap.guiviews.CVXBee;
import de.uvwxy.daisy.nodescanner.NodeDataConverter;
import de.uvwxy.daisy.nodescanner.NodeScanLauncher;
import de.uvwxy.daisy.proto.Messages.ChatMessage;
import de.uvwxy.daisy.proto.Messages.Image;
import de.uvwxy.daisy.proto.Messages.NameTag;
import de.uvwxy.daisy.proto.Messages.NodeLocationData;
import de.uvwxy.daisy.protocol.DaisyData;
import de.uvwxy.daisy.protocol.DaisyNetwork;
import de.uvwxy.daisy.protocol.DaisyProtocolBroadCastQueue;
import de.uvwxy.daisy.protocol.DaisyProtocolStartMainReceiverLoop;
import de.uvwxy.daisy.protocol.busmessages.DeploymentCreated;
import de.uvwxy.daisy.protocol.busmessages.DeploymentLoaded;
import de.uvwxy.daisy.protocol.busmessages.NeedsSync;
import de.uvwxy.helper.ContextProxy;
import de.uvwxy.helper.IntentTools;

/**
 * This class handles the creation of menu items and as a static resource to
 * save UI/System states between activity/fragment changes.
 * 
 * Each view has a corresponding ContentManagerXYZ which has the role of
 * creating/loading/saving/buffering data.
 * 
 * @author paul
 * 
 */
public class CM {

	public final static boolean DEBUG = true;

	public static final int INTENT_CODE_SCAN_NODE = 10;
	public static final int INTENT_ADD_IMAGE = 11;
	public static boolean TWO_PANE = false;
	public static final int SEED = 8026;

	public static Context CTX;
	public static Activity ACT;

	public static DaisyData DAISY;
	public static CMMap MAP = new CMMap();
	public static DaisyNetwork DAISYNET;
	public static CMLogs LOGS = new CMLogs();
	public static CMARView AR = new CMARView();
	public static CMXBee XB = new CMXBee();
	public static CMLocation LOC = null;
	public static CMSensors SENS = null;
	public static Bus BUS = new Bus();
	public static BusReceiver BUS_RECEIVER = new BusReceiver();
	public static DaisyProtocolBroadCastQueue DAISY_SYNC_QUEUE;

	private static String lastImagePath;

	public static class BusReceiver {
		public void register(Bus bus) {
			bus.register(this);
		}

		public void unregister(Bus bus) {
			bus.unregister(this);
		}

		@Subscribe
		public void busReceive(DeploymentCreated m) {
			CM.enableMenu();

			if (CM.DEBUG) {
				initChatDebug();
				initLogDebug();
				CM.MAP.initDebug();
			}

			DAISYNET.btListen(new DaisyProtocolStartMainReceiverLoop(CM.DAISY, CM.CTX, null));
		}

		@Subscribe
		public void busReceive(final NeedsSync empty) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					long tt = (long) (1000 * (4 * Math.random()));
					Log.i("DAISY", "Requesting resync in " + tt + " ms");

					try {
						Thread.sleep(tt);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.i("DAISY", "Requesting resync");
					CM.DAISY_SYNC_QUEUE.sync(empty.uuid);
				}
			});
			t.start();
		}

		@Subscribe
		public void busRecive(DeploymentLoaded m) {
			CM.enableMenu();
			CM.DAISYNET.btListen(new DaisyProtocolStartMainReceiverLoop(CM.DAISY, CM.CTX, null));
		}
	}

	public static ContextProxy ctxProxy = new ContextProxy() {

		@Override
		public Context ctx() {
			return CTX;
		}
	};

	private static void addItem(CV item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.ID, item);
	}

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<CV> ITEMS = new ArrayList<CV>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, CV> ITEM_MAP = new HashMap<String, CV>();

	public static Menu MENU;

	static {
		/*
		 * Add your [extends AContentItem] classes here The parameters are "id",
		 * "Title" <- shown in the list view
		 */
		addItem(new CVDeployment("idDeployment", "Deployment", R.drawable.ic_action_deployment));
		addItem(new CVParticipantScan("idParticipantScan", "Discover Participants", R.drawable.ic_action_wifi));

	}

	public static void disableAllMenus() {
		for (CV c : ITEMS) {

			c.setEnabled(false);

		}

		notifyMenuDataChanged();
	}

	public static void enableAllMenus() {
		for (CV c : ITEMS) {
			c.setEnabled(true);
		}

		notifyMenuDataChanged();
	}

	private static void enableMenu() {

		addItem(new CVMenuItem(CM.onClickScanNode, "idScanNode!", "Scan Node", R.drawable.ic_action_node));
		addItem(new CVMenuItem(onClickAddNote, "idAddAnnotation", "Add Annotation", R.drawable.ic_action_annotation));
		addItem(new CVMenuItem(onClickAddImage, "idAddImage", "Add Image", R.drawable.ic_action_image));

		addItem(new CVMap("idMap", "Map", R.drawable.ic_action_map));
		addItem(new CVARView("idARView", "ARView", R.drawable.ic_action_map_ar));
		addItem(new CVChat("idChat", "Messages", R.drawable.ic_action_chat));

		addItem(new CVLogs("idLogs", "Logs", R.drawable.ic_action_logs));
		addItem(new CVXBee("idXBee", "XBee", R.drawable.ic_action_xbee));
		addItem(new CVDeploymentData("idDeploymentData", "Deployment Data", R.drawable.ic_action_deployment_data));
		addItem(new CVBalloon("idBalloon", "Balloon/Kite Mode", R.drawable.ic_action_balloon));
		addItem(new CVBalloonClient("idBalloonClient", "Balloon/Kite Client", R.drawable.ic_action_balloon));
		notifyMenuDataChanged();
	}

	public static OnClickListener onClickScanNode = new OnClickListener() {
		@Override
		public void onClick(View v) {
			NodeScanLauncher.show(CM.ACT, CM.DAISY, INTENT_CODE_SCAN_NODE);
		}
	};

	public static OnClickListener onClickAddNote = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(CM.ACT.getApplicationContext(), ActivityAnnotation.class);
			try {
				CM.ACT.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public static OnClickListener onClickAddImage = new OnClickListener() {

		@Override
		public void onClick(View v) {
			lastImagePath = DAISY.getDeploymentPath(DaisyData.IMAGES_FOLDER) + "map_image_"
					+ System.currentTimeMillis() + ".jpg";
			Log.i("CM", "Will capture image to " + lastImagePath);
			IntentTools.captureImage(CM.ACT, lastImagePath, INTENT_ADD_IMAGE);
		}
	};

	public static void notifyMenuDataChanged() {
		if (MainListFragment.nvlia != null) {
			Handler h = new Handler(CM.CTX.getMainLooper());
			h.post(new Runnable() {

				@Override
				public void run() {
					MainListFragment.nvlia.notifyDataSetChanged();
				}
			});
		}
	}

	/**
	 * Clean up your CM* Modules here.
	 */
	public static void destroy() {
		MAP.destroy();
		LOGS.destroy();
		AR.destroy();
		XB.destroy();
		SENS.destroy();
		LOC.destroy();
	}

	public static boolean globalOnResultHandling(int requestCode, int resultCode, Intent data) {
		if (requestCode == CM.INTENT_CODE_SCAN_NODE) {
			NodeLocationData nodeData = NodeDataConverter.handleReturnIntent(data);

			if (nodeData == null) {
				return false;
			}

			// update tag to our running instance
			NodeLocationData nodeDataTagFixed = NodeLocationData.newBuilder(nodeData).setTag(CM.DAISY.getNextTag())
					.build();
			BUS.post(nodeDataTagFixed);
			//	CM.DAISY_SYNC_QUEUE.sync(null);
			CM.BUS.post(new NeedsSync());
			return true;
		}

		if (requestCode == CM.INTENT_ADD_IMAGE) {
			if (resultCode == Activity.RESULT_OK) {
				// lastImagePath
				Image.Builder b = Image.newBuilder();
				b.setImagePath(new File(lastImagePath).getName());
				b.setLocation(CM.LOC.getLastProtoLocation());
				b.setTag(CM.DAISY.getNextTag());
				b.setTimestamp(System.currentTimeMillis());
				Image i = b.build();
				CM.BUS.post(i);
				CM.BUS.post(new NeedsSync());
				// Bitmap bmp = BitmapTools.loadScaledBitmap(CTX,
				// mLastTakenImagePath, 128, 128);
				// if (bmp != null) {
				// ViewTools.addToLinearLayout(ctx, llPhotoList, bmp,
				// mLastTakenImagePath, removePathCallback);
				// imagePaths.add((new File(mLastTakenImagePath)).getName());
				// updateLblPhotoList();
				// }

			}
		}
		return false;
	}

	private static void initChatDebug() {
		double lat = 50.778839;
		double lon = 6.060758;
		de.uvwxy.daisy.proto.Messages.Location.Builder locBuilder = de.uvwxy.daisy.proto.Messages.Location.newBuilder();
		ChatMessage.Builder b = ChatMessage.newBuilder();

		locBuilder.setAccuracy(10.0).setAltitude(234.0).setBearing(90.0 * Math.random()).setProvider("GPS")
				.setSpeed(1.0);
		locBuilder.setLatitude(lat * ((Math.random() * .0001) + 1)).setLongitude(lon * ((Math.random() * .00001) + 1))
				.setTime((long) (System.currentTimeMillis() - Math.random() * 4000));

		NameTag u = NameTag.newBuilder(CM.DAISY.getNextTag()).setName("Terp").build();
		b.setTag(u).setMessage("Hi").setTimestamp((long) (System.currentTimeMillis() - Math.random() * 4000))
				.setLocation(locBuilder.build());
		CM.BUS.post(b.build());

		locBuilder.setLatitude(lat * ((Math.random() * .0001) + 1)).setLongitude(lon * ((Math.random() * .0001) + 1))
				.setTime((long) (System.currentTimeMillis() - Math.random() * 4000));
		u = NameTag.newBuilder(CM.DAISY.getNextTag()).setName("Berp").build();
		b.setTag(u).setMessage("Yehaw").setTimestamp((long) (System.currentTimeMillis() - Math.random() * 4000))
				.setLocation(locBuilder.build());
		CM.BUS.post(b.build());

		locBuilder.setLatitude(lat * ((Math.random() * .0001) + 1)).setLongitude(lon * ((Math.random() * .0001) + 1))
				.setTime((long) (System.currentTimeMillis() - Math.random() * 4000));
		u = NameTag.newBuilder(CM.DAISY.getNextTag()).setName("Derp").build();
		b.setTag(u).setMessage("Bonjour").setTimestamp((long) (System.currentTimeMillis() - Math.random() * 4000))
				.setLocation(locBuilder.build());
		CM.BUS.post(b.build());

		locBuilder.setLatitude(lat * ((Math.random() * .0001) + 1)).setLongitude(lon * ((Math.random() * .0001) + 1))
				.setTime((long) (System.currentTimeMillis() - Math.random() * 4000));
		u = NameTag.newBuilder(CM.DAISY.getNextTag()).setName("Herp").build();
		b.setTag(u).setMessage("Ey was geht?").setTimestamp((long) (System.currentTimeMillis() - Math.random() * 4000))
				.setLocation(locBuilder.build());
		CM.BUS.post(b.build());
	}

	private static void initLogDebug() {
		CM.LOGS.add("LOG", "Logs started.");
	}

}
