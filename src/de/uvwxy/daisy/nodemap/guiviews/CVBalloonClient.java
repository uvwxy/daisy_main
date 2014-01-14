package de.uvwxy.daisy.nodemap.guiviews;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service.State;
import com.squareup.otto.Subscribe;

import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.guicontent.CMBalloon;
import de.uvwxy.daisy.nodemap.listobjects.SampleGridViewAdapter;
import de.uvwxy.daisy.proto.Messages;
import de.uvwxy.daisy.proto.Messages.BalloonImage;
import de.uvwxy.daisy.proto.Messages.BalloonPreview;
import de.uvwxy.daisy.proto.Messages.BalloonStats;
import de.uvwxy.daisy.proto.Messages.CamSize;
import de.uvwxy.daisy.proto.Messages.DaisyProtocolMessage;
import de.uvwxy.daisy.proto.Messages.Fin;
import de.uvwxy.daisy.proto.Messages.Peer;
import de.uvwxy.daisy.protocol.DaisyData;
import de.uvwxy.daisy.protocol.DaisyNetwork;
import de.uvwxy.daisy.protocol.DaisyProtocolBroadCastQueue;
import de.uvwxy.daisy.protocol.DaisyProtocolRoutinesBalloon;
import de.uvwxy.daisy.protocol.DaisyProtocolStartBalloonRequest;
import de.uvwxy.daisy.protocol.DaisySyncLock;
import de.uvwxy.daisy.protocol.busmessages.BalloonBusReply;
import de.uvwxy.helper.BitmapTools;
import de.uvwxy.helper.FileTools;
import de.uvwxy.helper.IntentTools;
import de.uvwxy.helper.IntentTools.ReturnStringECallback;
import de.uvwxy.helper.StringE;
@SuppressWarnings("unused")
public class CVBalloonClient extends CV {

	private View barBalloonCardActions0 = null;
	private View barBalloonCardPreviews0 = null;

	private View barBalloonCardStatus0 = null;
	private Button btnBallooCardActionsDelay = null;
	private Button btnBalloonCardActionsGetPreview = null;
	private Button btnBalloonCardActionsSetImageSize = null;
	private Button btnBalloonCardStatusConnect = null;
	private Button btnBalloonCardStatusClose = null;
	private GridView gvBallooCardPreviews = null;
	private Handler h;
	private TextView lblBalloonCardActions = null;
	private TextView lblBalloonCardPreviews = null;
	private TextView lblBalloonCardStatus = null;
	private LinearLayout llBalloonClientMain = null;
	private LinearLayout llBalloonClientPreviews = null;
	private SeekBar sbBalloonCardActionsDelay = null;
	private ScrollView scrollView1 = null;
	private Spinner spBallooCardImageSize = null;
	private Spinner spBallooCardPreviewSize = null;
	private ToggleButton tbtnBalloonCardActionsLoop = null;
	private TextView textView1 = null;
	private TextView textView2 = null;
	private TextView textView3 = null;
	private TextView textView4 = null;
	private TextView tvBalloonCardActionsDelay = null;
	private TextView tvBalloonCardStatusText = null;
	private SampleGridViewAdapter gvAdapter = null;
	private ArrayList<String> absolutePaths = new ArrayList<String>();

	private View View01 = null;

	public CVBalloonClient(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	private void initCardActions(View root) {
		lblBalloonCardActions = (TextView) root.findViewById(R.id.lblBalloonCardActions);
		barBalloonCardActions0 = (View) root.findViewById(R.id.barBalloonCardActions0);
		sbBalloonCardActionsDelay = (SeekBar) root.findViewById(R.id.sbBalloonCardActionsDelay);
		btnBallooCardActionsDelay = (Button) root.findViewById(R.id.btnBallooCardActionsDelay);
		tvBalloonCardActionsDelay = (TextView) root.findViewById(R.id.tvBalloonCardActionsDelay);
		textView1 = (TextView) root.findViewById(R.id.textView1);
		spBallooCardPreviewSize = (Spinner) root.findViewById(R.id.spBallooCardPreviewSize);
		textView2 = (TextView) root.findViewById(R.id.textView2);
		spBallooCardImageSize = (Spinner) root.findViewById(R.id.spBallooCardImageSize);
		btnBalloonCardActionsGetPreview = (Button) root.findViewById(R.id.btnBalloonCardActionsGetPreview);
		textView4 = (TextView) root.findViewById(R.id.textView4);
		tbtnBalloonCardActionsLoop = (ToggleButton) root.findViewById(R.id.tbtnBalloonCardActionsLoop);
		btnBalloonCardActionsSetImageSize = (Button) root.findViewById(R.id.btnBalloonCardActionsSetImageSize);
		View01 = (View) root.findViewById(R.id.View01);
		textView3 = (TextView) root.findViewById(R.id.textView3);
	}

	private void initCardPreviews(View root) {
		lblBalloonCardPreviews = (TextView) root.findViewById(R.id.lblBalloonCardPreviews);
		barBalloonCardPreviews0 = (View) root.findViewById(R.id.barBalloonCardPreviews0);
		gvBallooCardPreviews = (GridView) root.findViewById(R.id.gvBallooCardPreviews);
		gvAdapter = new SampleGridViewAdapter(CM.CTX, absolutePaths);
		gvBallooCardPreviews.setAdapter(gvAdapter);
		gvBallooCardPreviews.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		gvBallooCardPreviews.setStackFromBottom(true);

		reloadImages();
	}

	private String getBalloonPreviewDir() {
		return FileTools.getAndCreateExternalFolder(DaisyData.DAISY_BALLOON_PREVIEW_FOLDER + CM.DAISY.getIdAndTimeStamp() + "/");
	}

	private void reloadImages() {
		Handler h = new Handler(CM.CTX.getMainLooper());
		h.post(new Runnable() {
			@Override
			public void run() {
				File f = new File(getBalloonPreviewDir());
				String[] dirListing = f.list();
				if (dirListing == null || dirListing.length == 0) {
					return;
				}

				Log.d("BALLOON", "Found " + dirListing.length + " previews");

				absolutePaths.clear();
				for (String ff : f.list()) {
					absolutePaths.add(getBalloonPreviewDir() + ff);
				}

				gvAdapter.notifyDataSetChanged();
			}
		});
	}

	private void initCardStatus(View root) {
		lblBalloonCardStatus = (TextView) root.findViewById(R.id.lblBalloonCardStatus);
		barBalloonCardStatus0 = (View) root.findViewById(R.id.barBalloonCardStatus0);
		tvBalloonCardStatusText = (TextView) root.findViewById(R.id.tvBalloonCardStatusText);
		btnBalloonCardStatusConnect = (Button) root.findViewById(R.id.btnBalloonCardStatusConnect);
		btnBalloonCardStatusClose = (Button) root.findViewById(R.id.btnBalloonCardStatusClose);
	}

	private void initRootGUI(View root) {
		scrollView1 = (ScrollView) root.findViewById(R.id.scrollView1);
		llBalloonClientMain = (LinearLayout) root.findViewById(R.id.llBalloonClientMain);
		llBalloonClientPreviews = (LinearLayout) root.findViewById(R.id.llBalloonClientPreviews);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		h = new Handler(CM.CTX.getMainLooper());

		View rootView = inflater.inflate(R.layout.content_view_balloon_client, container, false);
		LinearLayout llBalloonCardStatus = (LinearLayout) inflater.inflate(R.layout.balloon_card_status, null);
		LinearLayout llBalloonCardPreviews = (LinearLayout) inflater.inflate(R.layout.balloon_card_previews, null);
		LinearLayout llBalloonCardActions = (LinearLayout) inflater.inflate(R.layout.balloon_card_actions, null);

		initRootGUI(rootView);

		llBalloonClientMain.addView(llBalloonCardStatus);
		llBalloonClientPreviews.addView(llBalloonCardPreviews);
		llBalloonClientMain.addView(llBalloonCardActions);

		initCardStatus(rootView);
		initCardPreviews(rootView);
		initCardActions(rootView);
		setStatus("Not connected");

		onClickStuff();
		CM.BUS.register(this);

		if (CM.TWO_PANE) {
			LinearLayout rootLayout = (LinearLayout) rootView.findViewById(R.id.llBalloonClientRootView);
			rootLayout.setOrientation(LinearLayout.HORIZONTAL);
			LayoutParams lp = (LayoutParams) llBalloonCardPreviews.getLayoutParams();
			int dp = BitmapTools.dipToPixels(CM.CTX, 6);
			lp.setMargins(dp, 0, 0, 0);
			llBalloonCardPreviews.setLayoutParams(lp);
		}

		return rootView;
	}

	public void onDestroyView() {
		CM.BUS.unregister(this);
		// stop polling previews
		if (loopPoller != null) {
			loopPoller.stop();
			Log.d("BALLOON", "Stopped loop poller");
		}
	};

	private DaisyProtocolStartBalloonRequest protocol = null;
	private ArrayList<StringE<Peer>> mListItems = new ArrayList<StringE<Peer>>();
	private ReturnStringECallback<Peer> mSelectedCallback = new ReturnStringECallback<Peer>() {

		@Override
		public void result(StringE<Peer> stringE) {
			setStatus("Waiting for sync lock to be released..");
			DaisySyncLock.getInstance().waitAndLock();
			setStatus("Obtained lock..");

			protocol = new DaisyProtocolStartBalloonRequest(CM.DAISY, CM.CTX, stringE.e.getPeerNameTag());

			final Object commLock = new Object();
			// keep a thread parked in the background to releas the
			// DaisySyncLock
			// when the commlock has been notified (connection is then over)
			Thread t = new Thread(new Runnable() {
				public void run() {
					synchronized (commLock) {
						try {
							commLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					DaisySyncLock.getInstance().release();
					setStatus("Released DaisySyncLock");
				}
			});
			t.start();

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			DaisyNetwork.connect(CM.CTX, CM.DAISY, CM.DAISYNET, protocol, stringE.e, commLock);
			setStatus("Waiting for connection");

		}
	};

	private void onClickStuff() {
		btnBalloonCardStatusConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (DaisySyncLock.getInstance().isLocked()) {
					setStatus("Someone is using a connection, try again later");
					return;
				}

				mListItems.clear();
				List<Peer> list = DaisyProtocolBroadCastQueue.getUniquePeerList(CM.DAISY, CM.DAISY.getPeersList());
				for (Peer p : list) {
					StringE<Messages.Peer> e = new StringE<Messages.Peer>();
					e.e = p;
					e.s = p.getAddress() + " -> " + p.getPeerType();
					if (p.hasPeerNameTag()) {
						e.s = p.getPeerNameTag().getName() + " -> " + e.s;
					}

					mListItems.add(e);
				}

				IntentTools.userSelectStringE(CM.CTX, "Select client to connect to:", mListItems, mSelectedCallback);
			}
		});
		btnBalloonCardStatusClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (protocol == null || protocol.getConnection() == null) {
					return;
				}
				try {
					DaisyProtocolMessage.newBuilder().setFin(Fin.newBuilder().build()).build().writeDelimitedTo(protocol.getConnection().getOut());
					Thread.sleep(100);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				protocol.getConnection().close();
				setStatus("Closed Connection");
			}
		});
		btnBalloonCardActionsGetPreview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (protocol == null || protocol.getConnection() == null) {
					return;
				}

				try {
					DaisyProtocolRoutinesBalloon.getBalloonPreviewRequest(previewSize, -1).writeDelimitedTo(protocol.getConnection().getOut());
				} catch (IOException e) {
					protocol.getConnection().close();
					e.printStackTrace();
				}
			}
		});
		btnBallooCardActionsDelay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (hasConnection()) {
					try {
						DaisyProtocolRoutinesBalloon.getBalloonSettings(null, sbBalloonCardActionsDelay.getProgress()).writeDelimitedTo(
								protocol.getConnection().getOut());
					} catch (IOException e) {
						DaisySyncLock.getInstance().release();
						e.printStackTrace();
					}
				}
			}
		});

		sbBalloonCardActionsDelay.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				CMBalloon.setCaptureDelay(seekBar.getProgress());
				tvBalloonCardActionsDelay.setText("Capture Delay: " + seekBar.getProgress());
			}
		});

		tbtnBalloonCardActionsLoop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isChecked = tbtnBalloonCardActionsLoop.isChecked();
				if (isChecked) {
					// start loop pull
					if (loopPoller != null && (loopPoller.state() == State.RUNNING || loopPoller.state() == State.STARTING)) {
						loopPoller.stop();
					}

					loopPoller = new PreviewPoller();
					loopPoller.start();
					Log.d("BALLOON", "Started loop poller");

				} else {
					// stop loop pull
					if (loopPoller != null) {
						loopPoller.stop();
						Log.d("BALLOON", "Stopped loop poller");
					}
				}
			}
		});

		gvBallooCardPreviews.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String path = absolutePaths.get(arg2);
				File f = new File(path);

				String fileName = f.getName();
				int seq = DaisyProtocolRoutinesBalloon.getSequenceNumberFromFileName(fileName);

				ArrayList<StringE<String>> items = new ArrayList<StringE<String>>();

				StringE<String> actionViewPreview = new StringE<String>("Request Preview", path);
				items.add(actionViewPreview);

				StringE<String> fetchImage = new StringE<String>("Request Image", "" + seq);
				items.add(fetchImage);

				ReturnStringECallback<String> selected = new ReturnStringECallback<String>() {
					@Override
					public void result(StringE<String> stringE) {
						if (stringE.s.equals("Request Preview")) {
							if (protocol == null || protocol.getConnection() == null) {
								return;
							}
							try {
								DaisyProtocolRoutinesBalloon.getBalloonPreviewRequest(previewSize, Integer.parseInt(stringE.e)).writeDelimitedTo(
										protocol.getConnection().getOut());
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (stringE.s.equals("Request Image")) {
							if (protocol == null || protocol.getConnection() == null) {
								return;
							}
							try {
								DaisyProtocolRoutinesBalloon.getBalloonImageRequest(Integer.parseInt(stringE.e)).writeDelimitedTo(
										protocol.getConnection().getOut());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				IntentTools.userSelectStringE(CM.CTX, "Action:", items, selected);
				return true;
			}
		});
		gvBallooCardPreviews.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String path = absolutePaths.get(position);
				File f = new File(path);

				if (!f.exists()) {
					return;
				}

				String fileName = f.getName();
				int seq = DaisyProtocolRoutinesBalloon.getSequenceNumberFromFileName(fileName);
				String imagePath = DaisyProtocolRoutinesBalloon.getImagePath(CM.DAISY, seq);

				File fImage = new File(imagePath);
				ArrayList<StringE<String>> items = new ArrayList<StringE<String>>();

				if (fImage.exists()) {
					StringE<String> actionViewImage = new StringE<String>("View Image", fImage.getAbsolutePath());
					items.add(actionViewImage);

				} else {
					StringE<String> actionViewPreview = new StringE<String>("View Preview", path);
					items.add(actionViewPreview);

					StringE<String> fetchImage = new StringE<String>("Request Image", "" + seq);
					items.add(fetchImage);
				}

				ReturnStringECallback<String> selected = new ReturnStringECallback<String>() {
					@Override
					public void result(StringE<String> stringE) {
						if (stringE.s.equals("View Image")) {
							IntentTools.showImage(CM.CTX, stringE.e);
						} else if (stringE.s.equals("View Preview")) {
							IntentTools.showImage(CM.CTX, stringE.e);
						} else if (stringE.s.equals("Request Image")) {
							if (protocol == null || protocol.getConnection() == null) {
								return;
							}
							try {
								DaisyProtocolRoutinesBalloon.getBalloonImageRequest(Integer.parseInt(stringE.e)).writeDelimitedTo(
										protocol.getConnection().getOut());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				IntentTools.userSelectStringE(CM.CTX, "Action:", items, selected);
			}
		});
	}

	private CamSize previewSize;
	private List<CamSize> imageSizes;
	private CamSize imageSize;
	private OnItemSelectedListener imageSizeSelected = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
			imageSize = imageSizes.get(position);
			if (hasConnection()) {
				try {
					DaisyProtocolRoutinesBalloon.getBalloonSettings(imageSize, -1).writeDelimitedTo(protocol.getConnection().getOut());
				} catch (IOException e) {
					DaisySyncLock.getInstance().release();
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private OnItemSelectedListener previewSizeSelected = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
			previewSize = imageSizes.get(position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private boolean hasConnection() {
		if (protocol == null) {
			return false;
		}

		if (protocol.getConnection() == null) {
			return false;
		} else {
			return protocol.getConnection().isConnected();
		}
	}

	@Subscribe
	public void busReceive(BalloonBusReply reply) {
		if (reply.hostWasBalloon) {
			setStatus("Connected to balloon");

			if (reply.resolutions != null) {
				setStatus("Received " + reply.resolutions.size() + " resolutions");
			}

			imageSizes = reply.resolutions;
			ArrayAdapter<CamSize> imageSizeAdapter = new ArrayAdapter<Messages.CamSize>(CM.CTX, android.R.layout.simple_spinner_item, imageSizes);
			ArrayAdapter<CamSize> previewSizeAdapter = new ArrayAdapter<Messages.CamSize>(CM.CTX, android.R.layout.simple_spinner_item, imageSizes);

			spBallooCardImageSize.setAdapter(imageSizeAdapter);
			spBallooCardImageSize.setOnItemSelectedListener(imageSizeSelected);

			spBallooCardPreviewSize.setAdapter(previewSizeAdapter);
			spBallooCardPreviewSize.setOnItemSelectedListener(previewSizeSelected);

		} else {
			setStatus("Connected to host, but is not a balloon");
		}
	}

	@Subscribe
	public void busReceive(BalloonStats stats) {
		setStatus(stats.toString());
	}

	private int lastPreview = -1;

	@Subscribe
	public void busReceive(BalloonPreview bp) {
		reloadImages();
		Log.d("BALLOON", "Received preview");
		if (loopPoller != null) {
			try {
				Log.d("BALLOON", "Releasing trigger");
				synchronized (loopPoller.lock) {
					loopPoller.lock.notify();
				}
				Log.d("BALLOON", "Trigger Released");
			} catch (Exception e) {
				// might be not locked
				e.printStackTrace();
			}
		}

		if (lastPreview == bp.getSequenceNumber()) {
			tbtnBalloonCardActionsLoop.setChecked(false);
			if (loopPoller != null) {
				loopPoller.stop();
			}
		}
		lastPreview = bp.getSequenceNumber();
	}

	@Subscribe
	public void busReceive(final BalloonImage bp) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(CM.CTX);

		alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				IntentTools.showImage(CM.CTX, DaisyProtocolRoutinesBalloon.getImagePath(CM.DAISY, bp.getSequenceNumber()));
			}
		});

		alertDialog.setNegativeButton("No", null);
		alertDialog.setMessage("Received Image " + bp.getSequenceNumber() + " view it?");
		alertDialog.setTitle("View Image");
		alertDialog.show();
	}

	private void setStatus(final String msg) {
		h.post(new Runnable() {

			@Override
			public void run() {
				tvBalloonCardStatusText.setText(msg);
			}
		});
	}

	private PreviewPoller loopPoller;

	private class PreviewPoller extends AbstractExecutionThreadService {
		public Object lock = new Object();
		ArrayList<Integer> missingNumbers = new ArrayList<Integer>();

		@Override
		protected void startUp() throws Exception {
			missingNumbers = DaisyProtocolRoutinesBalloon.getMissingSequenceNumbers(CM.DAISY, DaisyData.DAISY_BALLOON_PREVIEW_FOLDER);
			Log.d("BALLOON", "Missing " + missingNumbers + " previews..");
		}

		@Override
		protected void run() throws Exception {
			int index = 0;
			String indexPath = null;
			while (isRunning()) {
				if (missingNumbers.size() > 0) {
					index = missingNumbers.get(0);
					indexPath = DaisyProtocolRoutinesBalloon.getPreviewPath(CM.DAISY, index);
				} else {
					index = -1;
				}

				// either get last index (-1) or the missing one
				if (index == -1 || (indexPath != null && !new File(indexPath).exists())) {

					if (protocol == null || protocol.getConnection() == null) {
						return;
					}
					try {
						Log.d("BALLOON", "Send getBalloonPreviewRequest " + index);

						DaisyProtocolRoutinesBalloon.getBalloonPreviewRequest(previewSize, index).writeDelimitedTo(protocol.getConnection().getOut());
					} catch (Exception e) {
						e.printStackTrace();
					}

					Log.d("BALLOON", "Waiting for preview reply");
					// wait for lock to be released (server reply or stop
					// thread)
					synchronized (lock) {
						lock.wait();
					}
					Log.d("BALLOON", "Preview reply released..");
				} else {
					// get index i but it exists: delete it
					missingNumbers.remove(0);
				}

			}
		}

		@Override
		protected void triggerShutdown() {
			synchronized (lock) {
				Log.d("BALLOON", "Releasing trigger and stopping loopPoller");
				try {
					lock.notify();
				} catch (Exception e) {
					// might be not locked
					e.printStackTrace();
				}
			}
		}
	}
}
