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
import de.uvwxy.daisy.nodemap.guiviews.CVChat;
import de.uvwxy.daisy.proto.Messages.ChatMessage;
import de.uvwxy.helper.DateTools;

public class ChatMessageArrayAdapter extends ArrayAdapter<ChatMessage> {
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private final CVChat cvChat;
	@SuppressWarnings("unused")
	private Context context;
	private LayoutInflater inflater;
	private ArrayList<ChatMessage> messageList;

	public ChatMessageArrayAdapter(CVChat cvChat, Context context, List<ChatMessage> objects) {
		super(context, R.layout.chat_item_msg, objects);
		this.cvChat = cvChat;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.messageList = (ArrayList<ChatMessage>) objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View entry = inflater.inflate(R.layout.chat_item_msg, parent, false);

		TextView tvTimeStamp = (TextView) entry.findViewById(R.id.tvTimeStamp);
		TextView tvMessage = (TextView) entry.findViewById(R.id.tvMessage);
		// ImageView ivIcon = (ImageView) entry.findViewById(R.id.ivIcon);

		ChatMessage msg = messageList.get(position);

		String line1 = DateTools.getDateTime(CM.CTX, msg.getTimestamp()) + " " + (msg.getTag().getName() != null ? msg.getTag().getName() : "[no name]") + ":";
		String line2 = msg.getMessage() != null ? msg.getMessage() : "[empty]";
		tvTimeStamp.setText(line1);
		tvMessage.setText(line2);

		// r = context.getResources();
		// float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, r.getDisplayMetrics());
		// ivIcon.setImageBitmap(ToolBox.createIcon((int)px,(int)px, item.getColor()));

		return entry;
	}

}