package de.uvwxy.daisy.nodemap.guiviews;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.common.base.Preconditions;
import com.squareup.otto.Subscribe;

import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.proto.Messages.Peer;
import de.uvwxy.daisy.proto.Messages.PeerType;
import de.uvwxy.daisy.protocol.DaisyProtocolBroadCastQueue;
import de.uvwxy.daisy.protocol.DaisyProtocolStartMainReceiverLoop;
import de.uvwxy.daisy.protocol.DaisyProtocolStartNameRequest;
import de.uvwxy.daisy.protocol.DaisyProtocolStartSyncRequest;
import de.uvwxy.daisy.protocol.busmessages.DeploymentCreated;
import de.uvwxy.daisy.protocol.busmessages.DiscoveredPeer;
import de.uvwxy.helper.IntentTools;
import de.uvwxy.helper.IntentTools.ReturnStringCallback;
import de.uvwxy.net.wifi.WIFISetup;

public class CVParticipantScan extends CV {
	ToggleButton tbtnBTScan;
	ToggleButton tbtnBTListen;
	ToggleButton tbtnTcpIpListen;
	ToggleButton tbtnWIFIP2PScan;
	ToggleButton tbtnWIFIP2PListen;

	private TextView tvWifiAddIp;
	private Button btnWifiAddIp;
	LinearLayout llPScanDiscoveredDevices;
	LinearLayout llPScanParticipatingDevices;
	String ip = "";

	public CVParticipantScan(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	@Subscribe
	public void busReceive(DeploymentCreated m) {
		enableListen();
	}

	@Subscribe
	public void busReceive(Peer p) {
		updateFoundDevicesList();
		updateParticipatingDevicesList();
	}

	@Subscribe
	public void busReceive(DiscoveredPeer dp) {
		updateFoundDevicesList();
		updateParticipatingDevicesList();
	}

	public void enableListen() {
		tbtnBTListen.setEnabled(true);
		tbtnTcpIpListen.setEnabled(true);
		// btnWifiAddIp.setEnabled(true);
		tbtnWIFIP2PListen.setEnabled(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_view_participant_discovery, container, false);
		llPScanDiscoveredDevices = (LinearLayout) rootView.findViewById(R.id.llPScanDiscoveredDevices);
		llPScanParticipatingDevices = (LinearLayout) rootView.findViewById(R.id.llPScanParticipatingDevices);
		tbtnBTScan = (ToggleButton) rootView.findViewById(R.id.tbtnBTScan);
		tbtnBTListen = (ToggleButton) rootView.findViewById(R.id.tbtnBTListen);
		tbtnTcpIpListen = (ToggleButton) rootView.findViewById(R.id.tbtnTcpIpListen);
		tvWifiAddIp = (TextView) rootView.findViewById(R.id.tvWifiAddIp);
		btnWifiAddIp = (Button) rootView.findViewById(R.id.btnWifiAddIp);

		tbtnWIFIP2PScan = (ToggleButton) rootView.findViewById(R.id.tbtnWIFIP2PScan);
		tbtnWIFIP2PListen = (ToggleButton) rootView.findViewById(R.id.tbtnWIFIP2PListen);

		ip = WIFISetup.getIpAddr(CM.CTX);
		tvWifiAddIp.setText(ip);

		if (CM.DAISY == null || !CM.DAISY.deplOK()) {
			tbtnBTListen.setEnabled(false);
			tbtnTcpIpListen.setEnabled(false);
			// btnWifiAddIp.setEnabled(false);
			tbtnWIFIP2PListen.setEnabled(false);

		}

		if (CM.DAISYNET.btIsDiscovering()) {
			tbtnBTScan.setChecked(true);
		}
		if (CM.DAISYNET.btIsListening()) {
			tbtnBTListen.setChecked(true);
		}
		if (CM.DAISYNET.wifiP2pIsDiscoring()) {
			tbtnWIFIP2PScan.setChecked(true);
		}
		if (CM.DAISYNET.wifiP2pIsListening()) {
			tbtnWIFIP2PListen.setChecked(true);
		}

		tbtnBTScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("DAISY", "tbtnBTScan.setOnClickListener..");

				if (tbtnBTScan.equals(v) && tbtnBTScan.isChecked()) {
					Log.i("DAISY", "tbtnBTScan.equals(v) && tbtnBTScan.isChecked()");

					CM.LOGS.add("BlueTooth", "Starting discovery/visibility..");
					CM.DAISYNET.btStartDiscovery();

				} else {
					Log.i("DAISY", "else..");

					CM.LOGS.add("BlueTooth", "Stopping discovery..");
					CM.DAISYNET.btStopDiscovery();
				}
			}
		});

		tbtnBTListen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("DAISY", "tbtnBTListen.setOnClick..");

				if (tbtnBTListen.equals(v) && tbtnBTListen.isChecked()) {
					CM.LOGS.add("BlueTooth", "Starting listen..");
					CM.DAISYNET.btListen(new DaisyProtocolStartMainReceiverLoop(CM.DAISY, CM.CTX, null));

				} else {
					CM.LOGS.add("BlueTooth", "Stopping listen..");
					CM.DAISYNET.btStopListen();
				}
			}
		});

		tbtnTcpIpListen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tbtnTcpIpListen.equals(v) && tbtnTcpIpListen.isChecked()) {
					CM.LOGS.add("IP", "Begin listening..");
					CM.DAISYNET.tcpIpListen(new DaisyProtocolStartMainReceiverLoop(CM.DAISY, CM.CTX, null));

				} else {
					CM.LOGS.add("IP", "Stop listening..");
					CM.DAISYNET.tcpIpStopListen();
				}
			}
		});

		btnWifiAddIp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (CM.DAISY == null || !CM.DAISY.deplOK()) {
					Toast.makeText(CM.CTX, "Can only add IP if bootstrapped. Try LONG CLICK to add remote IP!",
							Toast.LENGTH_LONG).show();
					return;
				}
				if (ip == null || ip.equals("")) {
					Toast.makeText(CM.CTX, "No Ip address found", Toast.LENGTH_LONG).show();
					return;
				}
				Peer peer = Peer.newBuilder()//
						.setAddress(ip)//
						.setPeerType(PeerType.IP)//
						.setTag(CM.DAISY.getNextTag())//
						.setPeerNameTag(CM.DAISY.getLocalUserTag())//
						.build();
				CM.BUS.post(peer);
			}
		});

		btnWifiAddIp.setOnLongClickListener(new OnLongClickListener() {
			EditText input = new EditText(CM.CTX);

			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(CM.CTX);

				alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Peer peer = Peer.newBuilder()//
								.setAddress(input.getText().toString())//
								.setPeerType(PeerType.IP)//
								.setTag(CM.DAISY.getNextTag())//
								.build();
						CM.DAISY.addDiscoveredPeer(peer);
					}
				});

				alertDialog.setNegativeButton("Cancel", null);
				alertDialog.setMessage("Type in the address to bootstrap from below:");

				alertDialog.setView(input);

				alertDialog.setTitle("Manual IP Config");
				alertDialog.show();
				return true;
			}
		});

		CM.LOGS.add("WiFi", "Test 1..");
		tbtnWIFIP2PScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tbtnWIFIP2PScan.equals(v) && tbtnWIFIP2PScan.isChecked()) {
					CM.LOGS.add("WiFi", "Starting discovery..");
					CM.DAISYNET.wifiP2pStartDiscovery();
					Toast.makeText(CM.CTX, "Starting WiFi P2P discovery", Toast.LENGTH_LONG).show();

				} else {
					CM.LOGS.add("WiFi", "Stopping discovery..");
					CM.DAISYNET.wifiP2pStopDiscovery();
				}
			}
		});
		CM.LOGS.add("WiFi", "Test 2..");

		tbtnWIFIP2PListen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tbtnWIFIP2PListen.equals(v) && tbtnWIFIP2PListen.isChecked()) {
					CM.LOGS.add("WiFi", "Begin listening..");
				} else {
					CM.LOGS.add("WiFi", "Stop listening..");

				}
			}
		});

		updateFoundDevicesList();
		updateParticipatingDevicesList();

		CM.BUS.register(this);

		return rootView;
	}

	@Override
	public void onDestroy() {
		CM.BUS.unregister(this);
		super.onDestroy();
	}

	public void updateFoundDevicesList() {
		if (llPScanDiscoveredDevices == null) {
			return;
		}
		Log.i("DAISY", "updateFoundDevicesList llPScanDiscoveredDevices OK");

		if (CM.DAISY.getDiscoveredPeers() == null) {
			return;
		}

		Log.i("DAISY", "updateFoundDevicesList getDiscoveredPeers OK");
		llPScanDiscoveredDevices.removeAllViews();

		for (Peer p : CM.DAISY.getDiscoveredPeers()) {
			Log.i("DAISY", "updateFoundDevicesList Peer OK");
			if (p.getPeerNameTag().getUuid().equals(CM.DAISY.getUuid())) {
				Log.i("DAISY", "updateFoundDevicesList FOUND SELF");

				continue;
			}

			llPScanDiscoveredDevices.addView(createEntryDevice(p));
		}
	}

	public void updateParticipatingDevicesList() {
		if (llPScanParticipatingDevices == null) {
			return;
		}

		Log.i("DAISY", "updateFoundDevicesList llPScanDiscoveredDevices OK");

		llPScanParticipatingDevices.removeAllViews();

		if (CM.DAISY.getPeersList() == null) {
			return;
		}

		ArrayList<Peer> uniquePeers = DaisyProtocolBroadCastQueue.getUniquePeerList(CM.DAISY, CM.DAISY.getPeersList());

		for (Peer p : uniquePeers) {
			Log.i("DAISY", "updateParticipatingDevicesList Peer OK");
			if (p.getPeerNameTag().getUuid().equals(CM.DAISY.getUuid())) {
				Log.i("DAISY", "updateParticipatingDevicesList FOUND SELF");

				continue;
			}
			llPScanParticipatingDevices.addView(createEntryPeer(p));
		}
	}

	private View createEntryPeer(final Peer p) {
		Preconditions.checkNotNull(p);

		TextView t = new TextView(CM.CTX);
		String lblPeer = "" + p.getPeerType() + " [" + p.getAddress() + "]";
		if (p.hasPeerNameTag() && p.getPeerNameTag().hasName()) {
			lblPeer += "\n -- " + p.getPeerNameTag().getName() + " -- ";
		}
		t.setText(lblPeer + "\n");

		t.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String[] items = { "Sync. Deployment", "Cancel" };
				ReturnStringCallback selected = new ReturnStringCallback() {

					@Override
					public void result(String s) {
						if (items[items.length - 1].equals(s)) {
							return;
						} else if (items[0].equals(s)) {
							// TODO: test connection.
							switch (p.getPeerType()) {
							case BLUETOOTH:
								CM.DAISYNET.btConnect(new DaisyProtocolStartSyncRequest(CM.DAISY, CM.CTX, null),
										p.getAddress(), null);
								break;
							case IP:
								CM.DAISYNET.tcpIpConnect(new DaisyProtocolStartSyncRequest(CM.DAISY, CM.CTX, null),
										p.getAddress(), null);
								break;
							case WIFI_MAC:
								CM.DAISYNET.wifiP2pConnect(new DaisyProtocolStartSyncRequest(CM.DAISY, CM.CTX, null),
										p.getAddress(), null);
								break;
							case XBEE:
								break;
							default:
								break;
							}
						}
					}
				};
				IntentTools.userSelectString(CM.ACT, "Select Action", items, selected);
			}
		});

		return t;
	}

	private View createEntryDevice(final Peer p) {
		Preconditions.checkNotNull(p);

		TextView t = new TextView(CM.CTX);
		String lblPeer = "" + p.getPeerType() + " [" + p.getAddress() + "]";
		if (p.hasPeerNameTag() && p.getPeerNameTag().hasName()) {
			lblPeer += "\n -- " + p.getPeerNameTag().getName() + " -- ";
		}
		t.setText(lblPeer + "\n");

		if (CM.DAISY.deplOK()) {
			return t;
		}

		// only add bootstrap command if no deployment. avoid trouble later on.
		// restart app to restart deployment.
		t.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String[] items = { "Bootstrap Deployment", "Cancel" };
				ReturnStringCallback selected = new ReturnStringCallback() {

					@Override
					public void result(String s) {
						if (items[items.length - 1].equals(s)) {
							return;
						} else if (items[0].equals(s)) {
							switch (p.getPeerType()) {
							case BLUETOOTH:
								CM.DAISYNET.btConnect(new DaisyProtocolStartNameRequest(CM.DAISY, CM.CTX, null),
										p.getAddress(), null);
								break;
							case IP:
								CM.DAISYNET.tcpIpConnect(new DaisyProtocolStartNameRequest(CM.DAISY, CM.CTX, null),
										p.getAddress(), null);
								break;
							case WIFI_MAC:
								CM.DAISYNET.wifiP2pConnect(new DaisyProtocolStartNameRequest(CM.DAISY, CM.CTX, null),
										p.getAddress(), null);
								break;
							case XBEE:
								break;
							default:
								break;

							}
						}
					}
				};
				IntentTools.userSelectString(CM.ACT, "Select Action", items, selected);
			}
		});

		return t;
	}
}
