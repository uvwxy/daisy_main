package de.uvwxy.daisy.nodemap.gui;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.listobjects.SampleGridViewAdapter;
import de.uvwxy.daisy.proto.Messages.Location;
import de.uvwxy.daisy.proto.Messages.NodeCommunicationData;
import de.uvwxy.daisy.proto.Messages.NodeLocationData;
import de.uvwxy.daisy.proto.Messages.SensorsDataCollectionMsg;
import de.uvwxy.daisy.proto.ProtoHelper;
import de.uvwxy.daisy.protocol.DaisyData;
import de.uvwxy.daisy.sensornode.SensorNetworkMessageParser;
import de.uvwxy.helper.BitmapTools;
import de.uvwxy.helper.FileTools;
import de.uvwxy.helper.IntentExtras;
import de.uvwxy.helper.IntentTools;
import de.uvwxy.soundfinder.SoundFinder;

public class ActivityNodeDetails extends Activity {
	private static final int INCL_Y = 5;
	private static final int INCL_X = 4;
	private static final int ACC_TEMP = 3;
	private static final int ACC_Z = 2;
	private static final int ACC_Y = 1;
	private static final int ACC_X = 0;
	@SuppressWarnings("unused")
	private ScrollView scrollView1 = null;
	private LinearLayout llDeviceMainInsideScrollView = null;
	private LinearLayout llCharView = null;
	private LinearLayout llNodeViewRootLayout = null;
	private TextView lblNodeDetails = null;
	private TextView tvDeviceInformation = null;
	private TextView tvDeviceLandmark = null;
	private TextView tvNodeDistance = null;
	private GridView gvDeviceImages = null;
	private Button btnFindNode = null;

	private SampleGridViewAdapter gvAdapter = null;

	private int nodeId = -1;
	private NodeLocationData d = null;
	private GraphicalView mChart;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	private XYSeries[] mSeries = new XYSeries[6];
	private XYSeriesRenderer[] mCurrentRenderer = new XYSeriesRenderer[6];

	ArrayList<String> absolutePaths = new ArrayList<String>();

	private void initGUI() {
		scrollView1 = (ScrollView) findViewById(R.id.scrollView1);
		llDeviceMainInsideScrollView = (LinearLayout) findViewById(R.id.llDeviceMainInsideScrollView);
		llCharView = (LinearLayout) findViewById(R.id.llChartView);
		llNodeViewRootLayout = (LinearLayout) findViewById(R.id.llNodeViewRootLayout);
		lblNodeDetails = (TextView) findViewById(R.id.lblNodeDetails);
		tvDeviceInformation = (TextView) findViewById(R.id.tvDeviceInformation);
		tvDeviceLandmark = (TextView) findViewById(R.id.tvDeviceLandmark);
		gvDeviceImages = (GridView) findViewById(R.id.gvNodeImages);
		btnFindNode = (Button) findViewById(R.id.btnFindNode);
		tvNodeDistance = (TextView) findViewById(R.id.tvNodeDistance);

		lblNodeDetails.setText(lblNodeDetails.getText().toString() + " (ID: " + d.getNodeId() + ")");

		for (String str : d.getImagePathList()) {
			String path = FileTools.getAndCreateExternalFolder(DaisyData.IMAGES_FOLDER) + CM.DAISY.getIdAndTimeStamp() + "/" + str;
			absolutePaths.add(path);
		}
		gvAdapter = new SampleGridViewAdapter(this, absolutePaths);
		gvDeviceImages.setAdapter(gvAdapter);
		gvDeviceImages.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				IntentTools.showImage(CM.CTX, absolutePaths.get(position));
			}
		});

		btnFindNode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Location l = d.getLocation();
				double dist = 1000;
				if (CM.LOC.getLastLocation() != null) {
					dist = CM.LOC.getLastLocation().distanceTo(ProtoHelper.protoLocationToAndroidLocation(l));
				}
				SoundFinder.findNode(CM.ACT, l.getLatitude(), l.getLongitude(), l.getAltitude(), 25, 15, dist);
			}
		});

		if (CM.TWO_PANE) {
			llNodeViewRootLayout.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams lp = (LayoutParams) llCharView.getLayoutParams();
			int dp = BitmapTools.dipToPixels(CM.CTX, 6);
			lp.setMargins(dp, 0, 0, 0);
			llCharView.setLayoutParams(lp);
		}

	}

	private void initChart() {

		mSeries[ACC_X] = new XYSeries("Acc. X");
		mSeries[ACC_Y] = new XYSeries("Acc. Y");
		mSeries[ACC_Z] = new XYSeries("Acc. Z");
		mSeries[ACC_TEMP] = new XYSeries("Acc. Temp.");
		mSeries[INCL_X] = new XYSeries("Incl. X");
		mSeries[INCL_Y] = new XYSeries("Incl. Y");

		mDataset.addSeries(mSeries[ACC_X]);
		mDataset.addSeries(mSeries[ACC_Y]);
		mDataset.addSeries(mSeries[ACC_Z]);
		mDataset.addSeries(mSeries[ACC_TEMP]);
		mDataset.addSeries(mSeries[INCL_X]);
		mDataset.addSeries(mSeries[INCL_Y]);

		mCurrentRenderer[ACC_X] = new XYSeriesRenderer();
		mCurrentRenderer[ACC_Y] = new XYSeriesRenderer();
		mCurrentRenderer[ACC_Z] = new XYSeriesRenderer();
		mCurrentRenderer[ACC_TEMP] = new XYSeriesRenderer();
		mCurrentRenderer[INCL_X] = new XYSeriesRenderer();
		mCurrentRenderer[INCL_Y] = new XYSeriesRenderer();

		mRenderer.addSeriesRenderer(mCurrentRenderer[ACC_X]);
		mRenderer.addSeriesRenderer(mCurrentRenderer[ACC_Y]);
		mRenderer.addSeriesRenderer(mCurrentRenderer[ACC_Z]);
		mRenderer.addSeriesRenderer(mCurrentRenderer[ACC_TEMP]);
		mRenderer.addSeriesRenderer(mCurrentRenderer[INCL_X]);
		mRenderer.addSeriesRenderer(mCurrentRenderer[INCL_Y]);

		mCurrentRenderer[ACC_X].setColor(Color.RED);
		mCurrentRenderer[ACC_Y].setColor(Color.GREEN);
		mCurrentRenderer[ACC_Z].setColor(Color.BLUE);
		mCurrentRenderer[ACC_TEMP].setColor(Color.BLACK);

		mCurrentRenderer[INCL_X].setColor(Color.MAGENTA);
		mCurrentRenderer[INCL_Y].setColor(Color.CYAN);

		mRenderer.setBackgroundColor(Color.WHITE);
		mRenderer.setMarginsColor(Color.WHITE);
		mRenderer.setApplyBackgroundColor(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		nodeId = getIntent().getIntExtra(IntentExtras.INTENT_EXTRA_NODE_ID_INT, -1);

		if (nodeId == -1) {
			TextView t = new TextView(this);
			t.setText("Caller forgot to set int extra INTENT_EXTRA_NODE_ID_INT");
			setContentView(t);
			return;
		}
		d = CM.DAISY.getLatestNodeLocationData(nodeId);

		setContentView(R.layout.activity_node_details);
		initGUI();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mChart == null) {
			initChart();
			initContent();
			mChart = ChartFactory.getCubeLineChartView(this, mDataset, mRenderer, 0.3f);
			mChart.setBackgroundColor(Color.WHITE);
			llCharView.addView(mChart);
		} else {
			mChart.repaint();
		}
		CM.BUS.register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		CM.BUS.unregister(this);
	}

	private void initContent() {

		String info = ProtoHelper.getDescription(d);

		NodeCommunicationData ncd = CM.DAISY.getLatestNodeCommunicationDataWithSensorData(nodeId);

		if (ncd != null) {
			info += String.format("\nBattery: %.2f V", ncd.getSensorData().getBatteryVoltage() / 1000.0);
			ImageView iv = new ImageView(this);
			iv.setImageDrawable(SensorNetworkMessageParser.getBatteryIcon(CM.DAISY, this, CM.DAISY.getLatestNodeLocationData(nodeId)));
			llDeviceMainInsideScrollView.addView(iv);
		}

		tvDeviceInformation.setText(info);
		String str = "Not Available";
		if (d.hasLandmarkText()) {
			str = d.getLandmarkText();
		}
		tvDeviceLandmark.setText(str);
		for (NodeCommunicationData nd : CM.DAISY.getNodeCommunicationDataList()) {
			addToSeries(nd);
		}
	}

	private void addToSeries(NodeCommunicationData ncd) {
		if (ncd == null || !ncd.hasSensorData()) {
			return;
		}
		SensorsDataCollectionMsg sdc = ncd.getSensorData();

		if (sdc.getNodeId() != nodeId) {
			return;
		}
		Log.i("NODEVIEW", "getAccelerationX = " + sdc.getAccelerationX());

		mSeries[0].add(sdc.getTime(), sdc.getAccelerationX() / 1000);
		mSeries[1].add(sdc.getTime(), sdc.getAccelerationY() / 1000);
		mSeries[ACC_Z].add(sdc.getTime(), sdc.getAccelerationZ() / 1000);
		mSeries[ACC_TEMP].add(sdc.getTime(), sdc.getAccelerationTemperature() / 100);

		mSeries[INCL_X].add(sdc.getTime(), sdc.getInclinationX() / 100000);
		mSeries[INCL_Y].add(sdc.getTime(), sdc.getInclinationY() / 100000);

	}

	@Subscribe
	public void busReceive(NodeCommunicationData nodeData) {
		if (nodeData != null && nodeData.hasSensorData()) {
			addToSeries(nodeData);
			mChart.repaint();
		}
	}

	@Subscribe
	public void busReceive(android.location.Location loc) {
		if (loc == null) {
			return;
		}
		android.location.Location nodeLoc = ProtoHelper.protoLocationToAndroidLocation(d.getLocation());
		int error = (int) (nodeLoc.getAccuracy() + loc.getAccuracy());
		tvNodeDistance.setText("Distance to Node: " + (int) loc.distanceTo(nodeLoc) + " m (+- " + error + " m)");
	}
}
