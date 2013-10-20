package de.uvwxy.daisy.nodemap.guiviews;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location;
import com.squareup.otto.Subscribe;

import de.uvwxy.camera.CameraHelper;
import de.uvwxy.camera.CameraPreview;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.guicontent.CMBalloon;
import de.uvwxy.daisy.nodemap.listobjects.SampleGridViewAdapter;
import de.uvwxy.daisy.proto.Messages.BalloonSettings;
import de.uvwxy.daisy.proto.Messages.BalloonStats;
import de.uvwxy.daisy.proto.Messages.CamSize;
import de.uvwxy.daisy.protocol.DaisyData;
import de.uvwxy.daisy.protocol.DaisyProtocolRoutinesBalloon;
import de.uvwxy.helper.FileTools;
import de.uvwxy.helper.IntentTools;

public class CVBalloon extends CV {
	private static final String CAPTURE_DELAY = "Capture Delay: ";
	private static final int CAPTURE_DELAY_MAX = 60;

	CameraPreview mPreview = null;
	Camera mCamera = null;
	private GridView gvBalloonImages = null;
	private SampleGridViewAdapter gvAdapter = null;
	private FrameLayout previewLayout;
	private SeekBar sbBalloonCaptureDelay;
	private TextView tvBalloonCaptureDelay;
	private ArrayList<String> absolutePaths = new ArrayList<String>();

	private ToggleButton tbtnBalloonCapture = null;

	public CVBalloon(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.content_view_ballon, container, false);
		previewLayout = (FrameLayout) rootView.findViewById(R.id.camera_preview);

		tbtnBalloonCapture = (ToggleButton) rootView.findViewById(R.id.tbtnBalloonCapture);

		sbBalloonCaptureDelay = (SeekBar) rootView.findViewById(R.id.sbBalloonCaptureDelay);
		tvBalloonCaptureDelay = (TextView) rootView.findViewById(R.id.tvBalloonCaptureDelay);

		sbBalloonCaptureDelay.setMax(CAPTURE_DELAY_MAX);
		sbBalloonCaptureDelay.setProgress(CMBalloon.getCaptureDelay());
		tvBalloonCaptureDelay.setText(CAPTURE_DELAY + CMBalloon.getCaptureDelay());

		sbBalloonCaptureDelay.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				CMBalloon.setCaptureDelay(seekBar.getProgress());
				tvBalloonCaptureDelay.setText(CAPTURE_DELAY + seekBar.getProgress());
			}
		});

		gvBalloonImages = (GridView) rootView.findViewById(R.id.gvBalloonImages);
		gvAdapter = new SampleGridViewAdapter(CM.CTX, absolutePaths);
		gvBalloonImages.setAdapter(gvAdapter);
		gvBalloonImages.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				IntentTools.showImage(CM.CTX, absolutePaths.get(position));
			}
		});

		gvBalloonImages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		gvBalloonImages.setStackFromBottom(true);

		tbtnBalloonCapture.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					startLoopCapture();
				} else {

					stopLoopCapture();
				}
			}
		});

		CM.BUS.register(this);
		reloadImages();
		return rootView;
	}

	private String getBalloonDir() {
		return FileTools.getAndCreateExternalFolder(DaisyData.DAISY_BALLOON_IMAGES_FOLDER
				+ CM.DAISY.getIdAndTimeStamp() + "/");
	}

	private void reloadImages() {
		Handler h = new Handler(CM.CTX.getMainLooper());
		h.post(new Runnable() {
			@Override
			public void run() {
				File f = new File(getBalloonDir());
				String[] dirListing = f.list();
				if (dirListing == null || dirListing.length == 0) {
					return;
				}
				absolutePaths.clear();
				for (String ff : f.list()) {
					absolutePaths.add(getBalloonDir() + ff);
				}

				gvAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		CM.BUS.unregister(this);
	}

	private int newSeq = -1;

	private PictureCallback cbJpeg = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i("BALLOON", "Saving picture..");

			if (newSeq == -1) {
				newSeq = DaisyProtocolRoutinesBalloon.getLargestSquenceNumber(CM.DAISY,
						CM.DAISY.getDeploymentPath(DaisyData.DAISY_BALLOON_IMAGES_FOLDER));

			}

			newSeq++;
			// height, location, etc in file name
			String filename = "balloon_" + newSeq + ".jpg";

			File f = new File(getBalloonDir() + filename);
			File fpb = new File(getBalloonDir() + filename + ".pb");
			Log.i("BALLOON", "Trying to write: " + f.getAbsolutePath());
			BalloonStats.Builder bStats = BalloonStats.newBuilder(DaisyProtocolRoutinesBalloon.getBalloonStats(CM.CTX,
					CM.DAISY));
			de.uvwxy.daisy.proto.Messages.Location lastLocation = CM.LOC.getLastProtoLocation();

			if (lastLocation != null) {
				bStats.setLocation(lastLocation);
			}

			float[] sData = CM.SENS.getLastAccelerometerReading();
			
			if (sData != null) {
				bStats.setAccelerationX(sData[0]);
				bStats.setAccelerationY(sData[1]);
				bStats.setAccelerationZ(sData[2]);
			}
			
			sData = CM.SENS.getLastCompassReading();
			if (sData != null) {
				bStats.setCompassAngleX(sData[0]);
				bStats.setCompassAngleY(sData[1]);
				bStats.setCompassAngleZ(sData[2]);
			}
			
			sData = CM.SENS.getLastBarometerReading();
			if (sData != null) {
				bStats.setBarometricPressure(sData[0]);
			}
			
			try {
				FileOutputStream fos = new FileOutputStream(f, false);
				fos.write(data);
				fos.flush();
				fos.close();
				
				FileOutputStream fospb = new FileOutputStream(fpb, false);
				fospb.write(bStats.build().toByteArray());
				fospb.flush();
				fospb.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (captureAgain) {
				hLoop.postDelayed(captureImage, sbBalloonCaptureDelay.getProgress() * 1000);
				reloadImages();
			}
		}
	};

	Handler hLoop;
	Runnable captureImage = new Runnable() {

		@Override
		public void run() {
			if (captureAgain) {
				Log.i("BALLOON", "Taking picture..");
				mCamera.startPreview();
				mCamera.takePicture(null, null, cbJpeg);
			}
		}
	};

	private boolean captureAgain = false;

	private void startLoopCapture() {
		CM.disableAllMenus();

		mCamera = CameraHelper.getCameraInstance();

		if (mCamera != null) {
			mPreview = new CameraPreview(CM.CTX, mCamera);
			previewLayout.addView(mPreview);
		} else {
			TextView tv = new TextView(CM.CTX);
			tv.setText("Failed to instanciate camera");
			previewLayout.addView(tv);
		}

		mCamera.startPreview();
		if (hLoop == null) {
			hLoop = new Handler(CM.CTX.getMainLooper());
		}
		captureAgain = true;

		setCameraParameters();

		Log.i("BALLOON", "Progress is " + sbBalloonCaptureDelay.getProgress());
		hLoop.postDelayed(captureImage, sbBalloonCaptureDelay.getProgress() * 1000);
		CM.DAISY.setBalloon(true);
		ArrayList<CamSize> list = new ArrayList<CamSize>();

		for (Size s : mCamera.getParameters().getSupportedPictureSizes()) {
			if (s == null) {
				continue;
			}
			list.add(CamSize.newBuilder().setX(s.width).setY(s.height).build());
		}

		CM.DAISY.setCameraResolutions(list);
	}

	private void setCameraParameters() {
		Parameters parameters = mCamera.getParameters();
		parameters.set("jpeg-quality", 85);
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);

		if (mSize == null) {
			mSize = CameraHelper.getHighestResolution(mCamera);
		}
		Camera.Size size = mSize;
		parameters.setPictureSize(size.width, size.height);
		mCamera.setParameters(parameters);
	}

	private void setCameraParameters(int x, int y) {
		Parameters parameters = mCamera.getParameters();
		parameters.set("jpeg-quality", 85);
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);

		parameters.setPictureSize(x, y);
		mCamera.setParameters(parameters);
	}

	private void stopLoopCapture() {
		CM.DAISY.setBalloon(false);
		CM.enableAllMenus();
		mCamera.stopPreview();
		// remove callback to caputre image
		hLoop.removeCallbacks(captureImage);
		CameraHelper.releaseCamera(mCamera);
		captureAgain = false;
	}

	Camera.Size mSize = null;

	@Subscribe
	public void busReceive(BalloonSettings settings) {
		if (settings.hasCaptureDelay()) {
			sbBalloonCaptureDelay.setProgress(settings.getCaptureDelay());
			CMBalloon.setCaptureDelay(sbBalloonCaptureDelay.getProgress());
			tvBalloonCaptureDelay.setText(CAPTURE_DELAY + sbBalloonCaptureDelay.getProgress());
		}
		if (settings.hasImageSize()) {
			setCameraParameters(settings.getImageSize().getX(), settings.getImageSize().getY());
		}
	}

}
