package de.uvwxy.daisy.nodemap.listobjects;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.guiviews.CVXBee;
import de.uvwxy.daisy.proto.Messages.NodeCommunicationData;
import de.uvwxy.daisy.sensornode.SensorNetworkMessageParser;
import de.uvwxy.xbee.apimode.messages.APIMessage;

public class APIMessageArrayAdapter extends ArrayAdapter<APIMessage> {
	private final CVXBee cvXBee;
	private Context context;
	private LayoutInflater inflater;
	private ArrayList<APIMessage> messageList;

	public APIMessageArrayAdapter(CVXBee cvXBee, Context context, List<APIMessage> objects) {
		super(context, R.layout.chat_item_msg, objects);
		this.cvXBee = cvXBee;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.messageList = (ArrayList<APIMessage>) objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View entry = inflater.inflate(R.layout.chat_item_msg, parent, false);

		TextView tvTimeStamp = (TextView) entry.findViewById(R.id.tvTimeStamp);
		TextView tvMessage = (TextView) entry.findViewById(R.id.tvMessage);
		// ImageView ivIcon = (ImageView) entry.findViewById(R.id.ivIcon);

		final APIMessage msg = messageList.get(position);

		String line1 = "API Message:";
		String line2 = (msg.getMessage() != null ? msg.getMessage() : "[empty]");

		tvMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(context, msg.getInfo(), Toast.LENGTH_LONG).show();
			}
		});

		NodeCommunicationData.Builder ncd = SensorNetworkMessageParser.apiMessageToNodeCommunicationData(CM.DAISY, msg);

		if (ncd != null) {
			line2 += "\n" + ncd.buildPartial().toString();
		}

		tvTimeStamp.setText(line1);
		tvMessage.setText(line2);
		tvMessage.setTextSize(10);

		// r = context.getResources();
		// float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64,
		// r.getDisplayMetrics());
		// ivIcon.setImageBitmap(ToolBox.createIcon((int)px,(int)px,
		// item.getColor()));

		return entry;
	}

}
