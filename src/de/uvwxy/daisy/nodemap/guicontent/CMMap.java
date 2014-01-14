package de.uvwxy.daisy.nodemap.guicontent;

import java.util.ArrayList;

import de.uvwxy.daisy.proto.Messages.NodeCommunicationData;
import de.uvwxy.daisy.proto.Messages.NodeLocationData;
import de.uvwxy.daisy.proto.Messages.SensorsDataCollectionMsg;

public class CMMap implements IDestroy {

	public String lastMapConfig = "Mapnik";
	public ArrayList<String> selectedCustomMaps = new ArrayList<String>();

	// save runtime settings of the map. on restart everything is on.
	// TODO: migrate to shared preferences
	public boolean showChatOverlay = true;
	public boolean showNodeLocationOverlay = true;
	public boolean showAnnotationOverlay = true;
	public boolean showImageOverlay = true;
	public boolean showNetworkTraffic = true;

	public void initDebug() {
		double lat = 50.778839;
		double lon = 6.060758;

		de.uvwxy.daisy.proto.Messages.Location.Builder locBuilder = de.uvwxy.daisy.proto.Messages.Location.newBuilder();
		NodeLocationData.Builder b = NodeLocationData.newBuilder();

		locBuilder.setAccuracy(10.0).setAltitude(234.0).setBearing(90.0 * Math.random()).setProvider("GPS")
				.setSpeed(1.0);
		locBuilder.setLatitude(lat * ((Math.random() * .0001) + 1)).setLongitude(lon * ((Math.random() * .00001) + 1))
				.setTime((long) (System.currentTimeMillis() - Math.random() * 4000));

		addDebugNode(locBuilder, b, 2010);

		locBuilder.setLatitude(lat * ((Math.random() * .0001) + 1)).setLongitude(lon * ((Math.random() * .00011) + 1))
				.setTime((long) (System.currentTimeMillis() - Math.random() * 3000));

		addDebugNode(locBuilder, b, 2011);
		locBuilder.setLatitude(lat * ((Math.random() * .0001) + 1)).setLongitude(lon * ((Math.random() * .00101) + 1))
				.setTime((long) (System.currentTimeMillis() - Math.random() * 8000));

		addDebugNode(locBuilder, b, 2012);
		locBuilder.setLatitude(lat * ((Math.random() * .0001) + 1)).setLongitude(lon * ((Math.random() * .00004) + 1))
				.setTime((long) (System.currentTimeMillis() - Math.random() * 9000));

		addDebugNode(locBuilder, b, 2013);

		// might be a Y-shaped example, depending on where the nodes are
		// randomly placed
		CM.BUS.post(getDebugComm(2010, 2012));
		CM.BUS.post(getDebugComm(2011, 2012));
		CM.BUS.post(getDebugComm(2012, 2013));
		CM.BUS.post(getDebugComm(2010, 2011));
		CM.BUS.post(getDebugComm(2010, 2012));
		CM.BUS.post(getDebugComm(2010, 2012));

	}

	private void addDebugNode(de.uvwxy.daisy.proto.Messages.Location.Builder locBuilder, NodeLocationData.Builder b,
			int id) {
		b.setLocation(locBuilder.build());
		b.setNodeId(id);
		b.setHeight(2.12f);
		b.setLandmarkText("This is a test Landmark description text.");
		b.setQrCodeBearing(0);
		b.setTag(CM.DAISY.getNextTag());
		b.setTimestamp(System.currentTimeMillis());
		CM.BUS.post(b.build());
	}

	private NodeCommunicationData getDebugComm(int from, int to) {
		NodeCommunicationData.Builder b = NodeCommunicationData.newBuilder();

		// b.setSourceAddress(123456789);
		// b.setIs16Bit(false);
		//
		// b.setRssi(010);
		// b.setOptions(0);
		b.setTimestamp(System.currentTimeMillis());

		SensorsDataCollectionMsg.Builder bb = SensorsDataCollectionMsg.newBuilder();

		bb.setNodeId(from);
		bb.setParentNodeId(to);
		bb.setEtx(0);
		bb.setTime(0);
		bb.setBatteryVoltage(10);
		bb.setAccelerationX(0);
		bb.setAccelerationY(0);
		bb.setAccelerationZ(0);
		bb.setAccelerationTemperature(0);
		bb.setInclinationX(0);
		bb.setInclinationY(0);

		b.setSensorData(bb.build());

		// TODO: set location!!
		// b.setLocation(CM.LOC.getLastLocation());

		b.setTag(CM.DAISY.getNextTag());
		return b.build();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
