package de.uvwxy.daisy.nodemap.guicontent;

import android.content.Context;
import android.location.Location;
import de.uvwxy.daisy.proto.ProtoHelper;
import de.uvwxy.sensors.AccelerometerReader;
import de.uvwxy.sensors.BarometerReader;
import de.uvwxy.sensors.CompassReader;
import de.uvwxy.sensors.SensorReader.SensorResultCallback;

public class CMSensors implements IDestroy {
	CompassReader compassReader = null;
	BarometerReader barometerReader = null;
	AccelerometerReader accelReader = null;

	private Context mCtx;

	private float[] compassData;
	private float[] accelData;
	private float[] barodata;

	private SensorResultCallback cbCompass = new SensorResultCallback() {

		@Override
		public void result(float[] f) {
			compassData = f.clone();
		}
	};

	private SensorResultCallback cbAccel = new SensorResultCallback() {

		@Override
		public void result(float[] f) {
			accelData = f.clone();
		}
	};

	private SensorResultCallback cbBaro = new SensorResultCallback() {

		@Override
		public void result(float[] f) {
			barodata = f.clone();
		}
	};

	public CMSensors(Context ctx) {
		this.mCtx = ctx;
		compassReader = new CompassReader(CM.CTX, -1, cbCompass);
		compassReader.startReading();
		accelReader = new AccelerometerReader(CM.CTX, -1, cbAccel);
		accelReader.startReading();
		barometerReader = new BarometerReader(CM.CTX, -1, cbBaro);
		barometerReader.startReading();
	}

	public float[] getLastBarometerReading() {
		return barodata;
	}

	public float[] getLastAccelerometerReading() {
		return accelData;
	}

	public float[] getLastCompassReading() {
		return compassData;
	}

	@Override
	public void destroy() {
		compassReader.stopReading();
		barometerReader.stopReading();
		accelReader.stopReading();
	}
}
