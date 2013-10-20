package de.uvwxy.daisy.nodemap.listobjects;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.guiviews.CVLogs;
import de.uvwxy.daisy.proto.Messages.LogMessage;
import de.uvwxy.helper.DateTools;

public class LogMessageArrayAdapter extends ArrayAdapter<LogMessage> {
	/**
		 * 
		 */
	private final CVLogs cvLogs;
	private Context context;
	private LayoutInflater inflater;
	private ArrayList<LogMessage> messageList;

	public LogMessageArrayAdapter(CVLogs cvLogs, Context context, List<LogMessage> objects) {
		super(context, R.layout.chat_item_msg, objects);
		this.cvLogs = cvLogs;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.messageList = (ArrayList<LogMessage>) objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View entry = inflater.inflate(R.layout.chat_item_msg, parent, false);

		TextView tvTimeStamp = (TextView) entry.findViewById(R.id.tvTimeStamp);
		TextView tvMessage = (TextView) entry.findViewById(R.id.tvMessage);
		// ImageView ivIcon = (ImageView) entry.findViewById(R.id.ivIcon);

		LogMessage msg = messageList.get(position);

		String line1 = DateTools.getDateTimeLong(CM.CTX, msg.getTimestamp()) + " " + (msg.getTag() != null ? msg.getTag() : "[no tag]") + ":";
		String line2 = msg.getMessage() != null ? msg.getMessage() : "[empty]";
		tvTimeStamp.setText(line1);
		tvMessage.setText(line2);

		// r = context.getResources();
		// float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, r.getDisplayMetrics());
		// ivIcon.setImageBitmap(ToolBox.createIcon((int)px,(int)px, item.getColor()));

		return entry;
	}

}