package de.uvwxy.daisy.nodemap.guicontent;

import android.content.Context;
import android.location.Location;
import de.uvwxy.daisy.proto.ProtoHelper;
import de.uvwxy.sensors.location.GPSWIFIReader;
import de.uvwxy.sensors.location.LocationReader.LocationResultCallback;
import de.uvwxy.sensors.location.LocationReader.LocationStatusCallback;

public class CMLocation implements IDestroy {
	GPSWIFIReader locationReader = null;
	Location lastLocation = new Location("Dummy");
	@SuppressWarnings("unused")
	private Context mCtx;

	LocationStatusCallback cbStatus = new LocationStatusCallback() {
		@Override
		public void status(Location l) {
			addLocation(l);
		}

	};

	LocationResultCallback cbResult = new LocationResultCallback() {
		@Override
		public void result(Location l) {
			addLocation(l);
		}
	};

	private void addLocation(Location l) {
		if (l != null) {
			CM.BUS.post(l);
			lastLocation = l;
		}
	}

	public CMLocation(Context ctx) {
		this.mCtx = ctx;
		locationReader = new GPSWIFIReader(ctx, 0, 0, cbStatus, cbResult, true, true);
		locationReader.startReading();
	}

	public Location getLastLocation() {
		return lastLocation;
	}

	public de.uvwxy.daisy.proto.Messages.Location getLastProtoLocation() {
		return lastLocation != null ? ProtoHelper.androidLocationToProtoLocation(lastLocation) : null;
	}

	@Override
	public void destroy() {
		locationReader.stopReading();
	}

}
