package de.uvwxy.daisy.nodemap.guiviews;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.osmdroid.CustomMap;
import de.uvwxy.daisy.osmdroid.CustomMapLoader;
import de.uvwxy.daisy.osmdroid.ExtractedOverlay;
import de.uvwxy.daisy.osmdroid.ExtractorChatMessage;
import de.uvwxy.daisy.osmdroid.ExtractorImage;
import de.uvwxy.daisy.osmdroid.ExtractorMapAnnotation;
import de.uvwxy.daisy.osmdroid.ExtractorNodeLocationData;
import de.uvwxy.daisy.osmdroid.MapTileProviderArrayTMS;
import de.uvwxy.daisy.osmdroid.NodeCommunicationOverlay;
import de.uvwxy.daisy.osmdroid.UserLocationOverlay;
import de.uvwxy.daisy.proto.Messages;
import de.uvwxy.daisy.proto.ProtoHelper;
import de.uvwxy.daisy.proto.Messages.Annotation;
import de.uvwxy.daisy.proto.Messages.ChatMessage;
import de.uvwxy.daisy.proto.Messages.Image;
import de.uvwxy.daisy.proto.Messages.MapViewConfig;
import de.uvwxy.daisy.proto.Messages.NodeCommunicationData;
import de.uvwxy.daisy.proto.Messages.NodeLocationData;
import de.uvwxy.daisy.protocol.DaisyData;
import de.uvwxy.helper.FileTools;
import de.uvwxy.soundfinder.SoundFinder;

public class CVMap extends CV {

	private View rootView;

	private LinearLayout llOverlays;

	private org.osmdroid.views.MapView osmMap;

	private TilesOverlay baseOverlay;

	public CVMap(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	private HashMap<String, Overlay> hashMapLocalArchiveOverlay = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		hashMapLocalArchiveOverlay = new HashMap<String, Overlay>();

		rootView = inflater.inflate(R.layout.content_view_map, container, false);
		Button btnMapScanNode = (Button) rootView.findViewById(R.id.btnMapScanNode);
		Button btnMapAddNote = (Button) rootView.findViewById(R.id.btnMapAddNote);
		Button btnMapAddImage = (Button) rootView.findViewById(R.id.btnMapAddImage);
		CheckBox cbOverlayChat = (CheckBox) rootView.findViewById(R.id.cbOverlayChat);
		CheckBox cbOverlayNodes = (CheckBox) rootView.findViewById(R.id.cbOverlayNodes);
		CheckBox cbOVerlayNotes = (CheckBox) rootView.findViewById(R.id.cbOverlayNotes);
		CheckBox cbOVerlayImages = (CheckBox) rootView.findViewById(R.id.cbOverlayImages);
		CheckBox cbOverlayNetworkTraffic = (CheckBox) rootView.findViewById(R.id.cbOverlayNetworkTraffic);

		cbOverlayChat.setChecked(CM.MAP.showChatOverlay);
		cbOverlayNodes.setChecked(CM.MAP.showNodeLocationOverlay);
		cbOVerlayNotes.setChecked(CM.MAP.showAnnotationOverlay);
		cbOVerlayImages.setChecked(CM.MAP.showImageOverlay);
		cbOverlayNetworkTraffic.setChecked(CM.MAP.showNetworkTraffic);

		cbOverlayChat.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CM.MAP.showChatOverlay = isChecked;
				mChatOverlay.getOverlay().setEnabled(isChecked);
				osmMap.invalidate();
			}
		});
		cbOverlayNodes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CM.MAP.showNodeLocationOverlay = isChecked;
				mNodeLocationOverlay.getOverlay().setEnabled(isChecked);
				osmMap.invalidate();
			}
		});
		cbOVerlayNotes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CM.MAP.showAnnotationOverlay = isChecked;
				mAnnotationOverlay.getOverlay().setEnabled(isChecked);
				osmMap.invalidate();
			}
		});
		cbOVerlayImages.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CM.MAP.showImageOverlay = isChecked;
				mImageOverlay.getOverlay().setEnabled(isChecked);
				osmMap.invalidate();
			}
		});
		cbOverlayNetworkTraffic.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CM.MAP.showNetworkTraffic = isChecked;
				mNCDOverlays.setEnabled(isChecked);
				osmMap.invalidate();
			}
		});

		btnMapScanNode.setOnClickListener(CM.onClickScanNode);
		btnMapAddNote.setOnClickListener(CM.onClickAddNote);
		btnMapAddImage.setOnClickListener(CM.onClickAddImage);

		osmMap = (org.osmdroid.views.MapView) rootView.findViewById(R.id.osmMap);

		// osmMap.setFocusable(true);
		// osmMap.setFocusableInTouchMode(true);
		osmMap.setBuiltInZoomControls(false);
		osmMap.setMultiTouchControls(true);
		osmMap.setUseSafeCanvas(false);
		osmMap.setMaxZoomLevel(20);
		setupMapRadioGroup(rootView);

		// TODO: addLocationOverlay(osmMap);
		// om.add();
		
		CM.BUS.register(this);
		return rootView;
	}

	private ExtractedOverlay<ChatMessage> mChatOverlay;
	private ExtractedOverlay<NodeLocationData> mNodeLocationOverlay;
	private ExtractedOverlay<Annotation> mAnnotationOverlay;
	private ExtractedOverlay<Image> mImageOverlay;
	private NodeCommunicationOverlay mNCDOverlays; // NodeCommunicationDataOverlay
	private UserLocationOverlay mUserLocOverlay;
	private MapEventsOverlay mMapEventsOverlay;

	private MapEventsReceiver mMapEventReceiver = new MapEventsReceiver() {
		
		@Override
		public boolean singleTapUpHelper(IGeoPoint p) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean longPressHelper(IGeoPoint p) {
			Location l = new Location("Dummy");
			l.setLatitude( p.getLatitudeE6()/1000000f);
			l.setLongitude( p.getLongitudeE6()/1000000f);
			
			double dist = 1000;
			if (CM.LOC.getLastLocation() != null) {
				dist = CM.LOC.getLastLocation().distanceTo(l);
			}
			SoundFinder.findNode(CM.ACT,l.getLatitude(),l.getLongitude(), l.getAltitude(), 25, 15, dist);
			return true;
		}
	};

	@Subscribe
	public void busReceive(ChatMessage m) {
		mChatOverlay.replaceObjects(CM.CTX, CM.DAISY.getChatMessageDataList());
		osmMap.refreshDrawableState();
	}

	@Subscribe
	public void busReceive(NodeLocationData m) {
		mNodeLocationOverlay.replaceObjects(CM.CTX, CM.DAISY.getNodeLocationDataListUniqueLatest());
		osmMap.refreshDrawableState();
	}

	@Subscribe
	public void busReceive(Annotation a) {
		mAnnotationOverlay.replaceObjects(CM.CTX, CM.DAISY.getMapAnnotationDataList());
		osmMap.refreshDrawableState();
	}

	@Subscribe
	public void busReceive(Image i) {
		mImageOverlay.replaceObjects(CM.CTX, CM.DAISY.getMapImageDataList());
		osmMap.refreshDrawableState();
	}

	@Subscribe
	public void busReceive(NodeCommunicationData ncd) {
		mNCDOverlays.replaceObjects(CM.CTX, CM.DAISY.getNodeCommunicationDataList());
		osmMap.refreshDrawableState();
	}

	@Subscribe
	public void busReceive(android.location.Location loc) {
		mUserLocOverlay.replaceObjects(loc);
		osmMap.refreshDrawableState();
		Handler h = new Handler(CM.CTX.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				osmMap.invalidate();
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		CM.BUS.unregister(this);
	}

	@Override
	public void onPause() {
		if (CM.DAISY.deplOK()) {
			GeoPoint mapCenter = (GeoPoint) osmMap.getMapCenter();
			Messages.MapViewConfig.Builder mapViewConfigBuilder = Messages.MapViewConfig.newBuilder();
			mapViewConfigBuilder.setZoomLevel(osmMap.getZoomLevel());
			mapViewConfigBuilder.setCenterLatitudeE6(mapCenter.getLatitudeE6());
			mapViewConfigBuilder.setCenterLongitudeE6(mapCenter.getLongitudeE6());
			mapViewConfigBuilder.setCenterAltitude(mapCenter.getAltitude());
			CM.DAISY.setMapViewConfig(mapViewConfigBuilder.build());
		} else {
			Toast.makeText(CM.CTX, "Please load/create a deployment before you continue.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onResume() {
		if (CM.DAISY.deplOK()) {
			MapViewConfig mapViewConfig = CM.DAISY.getMapViewConfig();
			if (mapViewConfig != null) {
				GeoPoint mapCenter = new GeoPoint(mapViewConfig.getCenterLatitudeE6(), mapViewConfig.getCenterLongitudeE6());
				if (osmMap != null) {
					MapController osmMapController = osmMap.getController();
					if (osmMapController != null) {
						osmMapController.setZoom(mapViewConfig.getZoomLevel());
						osmMapController.setCenter(mapCenter);
					}
					baseOverlay = getOnlineMapOverlay(CM.MAP.lastMapConfig);
					setupBaseOverlays(baseOverlay);
					setupCustomMapSelction(rootView);
					setupPOIOverlays();

					osmMap.invalidate();
				}
			}
		} else {
			Toast.makeText(CM.CTX, "Please load/create a deployment before you continue.", Toast.LENGTH_LONG).show();
		}

	}

	public void setupBaseOverlays(TilesOverlay baseOverlay) {
		if (osmMap.getOverlays().size() > 0) {
			osmMap.getOverlays().remove(0);
		}
		osmMap.getOverlays().add(0, baseOverlay);
	}

	private void setupCustomMapSelction(View root) {
		llOverlays = (LinearLayout) root.findViewById(R.id.llOverlays);

		llOverlays.removeAllViews();

		String rootDir = FileTools.getAndCreateExternalFolder(DaisyData.MAP_ARCHIVE_FOLDER);

		ArrayList<CustomMap> maps = CustomMapLoader.getCustomMaps(rootDir);

		if (maps == null) {
			return;
		}

		// delete overlay list and create a new one

		for (int i = 0; i < osmMap.getOverlays().size(); i++) {
			if (hashMapLocalArchiveOverlay.containsValue(osmMap.getOverlays().get(i))) {
				osmMap.getOverlays().remove(i);
			}
		}
		hashMapLocalArchiveOverlay.clear();

		for (final CustomMap map : maps) {
			if (map == null) {
				continue;
			}

			TilesOverlay overlay = getOfflienMapOverlay(rootDir + map.getFileName(), map.getMapName(), false);

			if (overlay == null) {
				continue;
			}

			hashMapLocalArchiveOverlay.put(map.getFileName(), overlay);

			final CheckBox cb = new CheckBox(CM.CTX);
			cb.setText(map.toString());

			osmMap.getOverlays().add(overlay);

			llOverlays.addView(cb);

			if (CM.MAP.selectedCustomMaps.contains(map.getFileName())) {
				cb.setChecked(true);
				overlay.setEnabled(true);
			} else {
				cb.setChecked(false);
				overlay.setEnabled(false);
			}

			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						// ADD MAP
						hashMapLocalArchiveOverlay.get(map.getFileName()).setEnabled(true);

						CM.MAP.selectedCustomMaps.add(map.getFileName());
						osmMap.invalidate();
					} else {
						// REMOVE MAP ( HIDE IT)
						hashMapLocalArchiveOverlay.get(map.getFileName()).setEnabled(false);
						for (int i = 0; i < CM.MAP.selectedCustomMaps.size(); i++) {
							if (map.getFileName().equals(CM.MAP.selectedCustomMaps.get(i))) {
								CM.MAP.selectedCustomMaps.remove(i);
							}
						}
						osmMap.invalidate();
					}
				}
			});
		}
	}

	public void setupPOIOverlays() {
		
		// if we only create the following overlays when they are not null the context is outdated and the bubbles will
		// not open if we tap on the icons after opening the map a second time

		mChatOverlay = new ExtractedOverlay<ChatMessage>(new ExtractorChatMessage(CM.DAISY), CM.CTX, osmMap, CM.ACT, CM.DAISY);

		mNodeLocationOverlay = new ExtractedOverlay<NodeLocationData>(new ExtractorNodeLocationData(CM.DAISY), CM.CTX, osmMap, CM.ACT, CM.DAISY);

		mAnnotationOverlay = new ExtractedOverlay<Messages.Annotation>(new ExtractorMapAnnotation(CM.DAISY), CM.CTX, osmMap, CM.ACT, CM.DAISY);

		mImageOverlay = new ExtractedOverlay<Messages.Image>(new ExtractorImage(CM.DAISY), CM.CTX, osmMap, CM.ACT, CM.DAISY);

		mNCDOverlays = new NodeCommunicationOverlay(CM.CTX, CM.DAISY);

		mUserLocOverlay = new UserLocationOverlay(CM.CTX);
		
		mMapEventsOverlay = new MapEventsOverlay(CM.CTX, mMapEventReceiver);

		mChatOverlay.replaceObjects(CM.CTX, CM.DAISY.getChatMessageDataList());
		mNodeLocationOverlay.replaceObjects(CM.CTX, CM.DAISY.getNodeLocationDataListUniqueLatest());
		mAnnotationOverlay.replaceObjects(CM.CTX, CM.DAISY.getMapAnnotationDataList());
		mImageOverlay.replaceObjects(CM.CTX, CM.DAISY.getMapImageDataList());
		mNCDOverlays.replaceObjects(CM.CTX, CM.DAISY.getNodeCommunicationDataList());
		mUserLocOverlay.replaceObjects(CM.LOC.getLastLocation());

		mChatOverlay.getOverlay().setEnabled(CM.MAP.showChatOverlay);
		mNodeLocationOverlay.getOverlay().setEnabled(CM.MAP.showNodeLocationOverlay);
		mAnnotationOverlay.getOverlay().setEnabled(CM.MAP.showAnnotationOverlay);
		mImageOverlay.getOverlay().setEnabled(CM.MAP.showImageOverlay);
		mNCDOverlays.setEnabled(CM.MAP.showNetworkTraffic);
		mUserLocOverlay.setEnabled(true);

		osmMap.getOverlays().clear();
		setupCustomMapSelction(rootView);
		// network traffic first, looks better
		osmMap.getOverlays().add(mNCDOverlays);
		osmMap.getOverlays().add(mChatOverlay.getOverlay());
		osmMap.getOverlays().add(mNodeLocationOverlay.getOverlay());
		osmMap.getOverlays().add(mAnnotationOverlay.getOverlay());
		osmMap.getOverlays().add(mImageOverlay.getOverlay());
		osmMap.getOverlays().add(mUserLocOverlay);
		osmMap.getOverlays().add(mMapEventsOverlay);
		
	}

	private void setupMapRadioGroup(View root) {
		final RadioGroup rg = (RadioGroup) root.findViewById(R.id.rgMaps);
		rg.removeAllViews();
		ArrayList<ITileSource> list = TileSourceFactory.getTileSources();
		for (int i = 0; i < list.size(); i++) {
			final ITileSource s = list.get(i);
			final RadioButton rb = new RadioButton(CM.CTX);
			rb.setText(s.name());

			rb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switchOnlineMapOverlay(s.name());
				}
			});
			rg.addView(rb);

			if (CM.MAP.lastMapConfig != null && CM.MAP.lastMapConfig.equals(s.name())) {
				rb.setChecked(true);
			}
		}

	}

	public void addMiniMapOverlay() {
		final MinimapOverlay miniMapOverlay = new MinimapOverlay(CM.CTX, osmMap.getTileRequestCompleteHandler());
		osmMap.getOverlays().add(miniMapOverlay);
	}

	private MapTileProviderArray loadMapFromArchive(String pathToArchive, String name) {
		File archiveFile = new File(pathToArchive);
		IArchiveFile archive = null;
		if (archiveFile.exists()) {
			Log.i("MAP", "Archive exists: " + archiveFile.getAbsolutePath());

			archive = ArchiveFileFactory.getArchiveFile(archiveFile);
			Log.i("MAP", "Loaded a " + name + " map, is ok: " + (archive != null));

		} else {
			Log.i("MAP", "Archive does NOT exist: " + archiveFile.getAbsolutePath());

		}

		if (archive == null) {
			return null;
		}

		XYTileSource TILERENDERER = new XYTileSource(name, ResourceProxy.string.offline_mode, 0, 18, 256, ".png", "http://127.0.0.1");
		SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(CM.CTX);
		IArchiveFile[] archives = { archive };
		MapTileModuleProviderBase moduleProvider = new MapTileFileArchiveProvider(simpleReceiver, TILERENDERER, archives);
		MapTileProviderArray mapProvider = null;
		// test if .tms file exists for TMS file format indication
		File tmsFile = new File(pathToArchive + ".tms");
		if (tmsFile.exists()) {
			mapProvider = new MapTileProviderArrayTMS(TILERENDERER, null, new MapTileModuleProviderBase[] { moduleProvider });
		}else {
			mapProvider = new MapTileProviderArray(TILERENDERER, null, new MapTileModuleProviderBase[] { moduleProvider });
		}
		
		return mapProvider;
	}

	private TilesOverlay getOnlineMapOverlay(String name) {
		MapTileProviderBasic baseProvider = new MapTileProviderBasic(CM.CTX, TileSourceFactory.getTileSource(name));
		TilesOverlay baseOverlay = new TilesOverlay(baseProvider, CM.CTX);
		baseOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
		return baseOverlay;
	}

	private void switchOnlineMapOverlay(String name) {
		Log.i("MAP", "Switching to " + name);
		baseOverlay = getOnlineMapOverlay(name);

		if (baseOverlay != null) {
			setupBaseOverlays(baseOverlay);
			Log.i("MAP", "Switched to " + name);
			CM.MAP.lastMapConfig = name;
		} else {
			Log.i("MAP", "" + name + " + not found..");

		}

	}

	private TilesOverlay getOfflienMapOverlay(String path, String mapName, boolean enable) {
		Log.i("MAP", "Reading " + path);
		MapTileProviderArray mapProvider = loadMapFromArchive(path, mapName);

		if (mapProvider != null) {
			TilesOverlay overlay = new TilesOverlay(mapProvider, CM.CTX);
			overlay.setLoadingBackgroundColor(Color.TRANSPARENT);
			return overlay;
		}

		return null;
	}

}
