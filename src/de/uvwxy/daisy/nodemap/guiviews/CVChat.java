package de.uvwxy.daisy.nodemap.guiviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.squareup.otto.Subscribe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.listobjects.ChatMessageArrayAdapter;
import de.uvwxy.daisy.proto.Messages.ChatMessage;
import de.uvwxy.daisy.proto.ProtoHelper;
import de.uvwxy.daisy.protocol.busmessages.NeedsSync;

public class CVChat extends CV {
	public ArrayList<de.uvwxy.daisy.proto.Messages.ChatMessage> chatMessages = null;

	public ChatMessageArrayAdapter chatMessagesAdapter;

	private EditText tvChatInput;

	public CVChat(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	@Subscribe
	public void addChatMessageToView(ChatMessage m) {
		chatMessages.add(m);
		sortChat();
		chatMessagesAdapter.notifyDataSetChanged();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// refresh all chat messages
		chatMessages = new ArrayList<ChatMessage>();
		chatMessages.addAll(CM.DAISY.getChatMessageDataList());
		sortChat();

		View rootView = inflater.inflate(R.layout.content_view_chat, container, false);
		ListView lvMessages = (ListView) rootView.findViewById(R.id.lvChat);
		tvChatInput = (EditText) rootView.findViewById(R.id.tvChatInput);

		Button btnChatSend = (Button) rootView.findViewById(R.id.btnChatSend);

		btnChatSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendChat();
			}
		});

		chatMessagesAdapter = new ChatMessageArrayAdapter(this, CM.CTX, chatMessages);

		lvMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lvMessages.setStackFromBottom(true);
		lvMessages.setAdapter(chatMessagesAdapter);

		CM.BUS.register(this);

		// userAdapter
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		CM.BUS.unregister(this);
	}

	private void sendChat() {
		if (tvChatInput == null) {
			return;
		}

		if (tvChatInput.getText().toString().equals("")) {
			return;
		}

		Location l = null;

		l = CM.LOC.getLastLocation();

		if (l == null) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(CM.CTX);

			alertDialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					sendChat();
				}
			});

			alertDialog.setNegativeButton("Cancel", null);
			alertDialog.setMessage("Missing location, please try again until this application was able to obtain a fix.");
			alertDialog.setTitle("Location error.");
			alertDialog.setNeutralButton("Fake Location", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Location fake = new Location("Fake");
					sendMessage(fake);
				}
			});
			alertDialog.show();

			return;
		}

		sendMessage(l);

	}

	public void sendMessage(Location l) {

		ChatMessage.Builder cm = ChatMessage.newBuilder();
		cm.setLocation(ProtoHelper.androidLocationToProtoLocation(l));
		cm.setMessage(tvChatInput.getText().toString());
		cm.setTag(CM.DAISY.getNextTag());
		cm.setTimestamp(System.currentTimeMillis());
		chatMessagesAdapter.notifyDataSetChanged();
		tvChatInput.setText("");

		// will receive this chat message via event bus, and post to ui
		CM.BUS.post(cm.build());
		CM.BUS.post(new NeedsSync());
	}

	void sortChat() {
		Collections.sort(chatMessages, new Comparator<ChatMessage>() {

			@Override
			public int compare(ChatMessage lhs, ChatMessage rhs) {
				return Double.compare(lhs.getTimestamp(), rhs.getTimestamp());
			}
		});
	}
}
