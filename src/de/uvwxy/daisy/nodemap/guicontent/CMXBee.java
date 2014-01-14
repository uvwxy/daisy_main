package de.uvwxy.daisy.nodemap.guicontent;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import de.uvwxy.daisy.proto.Messages.Location;
import de.uvwxy.daisy.proto.Messages.NodeCommunicationData;
import de.uvwxy.daisy.sensornode.SensorNetworkMessageParser;
import de.uvwxy.usb.UARTDevice;
import de.uvwxy.xbee.XBeeDevice;
import de.uvwxy.xbee.XBeeDevice_UART;
import de.uvwxy.xbee.apimode.Frame;
import de.uvwxy.xbee.apimode.FrameCallback;
import de.uvwxy.xbee.apimode.FrameParserEscaped;
import de.uvwxy.xbee.apimode.messages.APIMessage;
import de.uvwxy.xbee.commands.ATCommand;

public class CMXBee implements IDestroy {
	public XBeeDevice xBeeDevice = null;
	FrameParserEscaped inputParser = null;

	private FrameCallback frameCallback = new FrameCallback() {

		@Override
		public void onFrame(Frame f) {
			final APIMessage receivedAPIMessage = f.getAPIMessage();

			if (receivedAPIMessage == null) {
				return;
			}

			Handler h = new Handler(CM.CTX.getMainLooper());
			h.post(new Runnable() {

				@Override
				public void run() {
					// updates the xbee ui
			
						Log.i("APIMESSAGE_", "POSTING MsgATCommandResponse");
						CM.BUS.post( receivedAPIMessage);
				
				}
			});

			Log.i("APIMESSAGE_", BaseEncoding.base16().encode(receivedAPIMessage.getCmdData()));

			// add communication data into daisy
			final NodeCommunicationData.Builder ncd = SensorNetworkMessageParser.apiMessageToNodeCommunicationData(
					CM.DAISY, receivedAPIMessage);

			if (ncd == null) {
				return;
			}

			Log.i("APIMESSAGE_", "NCD OK");
			Location lastLoc = CM.LOC.getLastProtoLocation();
			if (lastLoc != null) {
				ncd.setLocation(lastLoc);
			}
			ncd.setTag(CM.DAISY.getNextTag());

			h.post(new Runnable() {

				@Override
				public void run() {
					Log.i("APIMESSAGE_", "POSTING NCD");
					// inserts the nodecommunication data into daisy and all
					// interested
					// listener
					CM.BUS.post(ncd.build());
				}
			});

		}

	};

	public boolean create(Context ctx, int baud) {
		try {
			if (xBeeDevice == null) {
				xBeeDevice = (XBeeDevice) new XBeeDevice_UART(ctx);
			}
			((XBeeDevice_UART) xBeeDevice).setFrameCallback(frameCallback);

			inputParser = new FrameParserEscaped();

			((UARTDevice) xBeeDevice).populateDevices();

			Thread.sleep(3000);

			if (((UARTDevice) xBeeDevice).bindToFirst()) {
				Log.i("XBEE", "CONNECTION SUCCESS");
				Toast.makeText(CM.CTX, "Connected to XBee device", Toast.LENGTH_LONG).show();

				((XBeeDevice_UART) xBeeDevice).setBaud(baud);
				xBeeDevice.init();

				Log.i("XBEE", "Connection initialized");

				// xBeeDevice.setAPIMode2();
				// Log.i("XBEE", "Setting API mode 2");
				return true;
			} else {
				Toast.makeText(CM.CTX, "Error: Failed to connect to XBee device.", Toast.LENGTH_LONG).show();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void sendAPICommand(ATCommand cmd, String hex) {
		Preconditions.checkNotNull(xBeeDevice);
		Preconditions.checkNotNull(cmd);
		// bytes can be null

		xBeeDevice.sendAPICommand(cmd, BaseEncoding.base16().decode(hex));
	}

	public void sendAPIMessage(APIMessage apiMsg) {
		Preconditions.checkNotNull(xBeeDevice);

		xBeeDevice.sendAPIMessage(apiMsg);

	}

	public void setChannel(String base16) {// 0xc
		xBeeDevice.sendAPICommand(ATCommand.CMD_CH, BaseEncoding.base16().decode(base16));
	}

	public void setID(String base16) { // 0x2, 0x2
		xBeeDevice.sendAPICommand(ATCommand.CMD_ID, BaseEncoding.base16().decode(base16));
	}

	public void close() {
		xBeeDevice.close();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
