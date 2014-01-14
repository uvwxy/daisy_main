package de.uvwxy.daisy.nodemap.guiviews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.squareup.otto.Subscribe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.daisy.nodemap.guicontent.CM;
import de.uvwxy.daisy.nodemap.listobjects.LogMessageArrayAdapter;
import de.uvwxy.daisy.proto.Messages.LogMessage;
import de.uvwxy.helper.IntentTools;

public class CVLogs extends CV {
	private ArrayList<de.uvwxy.daisy.proto.Messages.LogMessage> logs = null;

	private static LogMessageArrayAdapter logMessagesAdapter;

	public CVLogs(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		logs = new ArrayList<LogMessage>();
		logs.addAll(CM.DAISY.getLogMessageDataList());
		sortLogsTimeAscending();

		View rootView = inflater.inflate(R.layout.content_view_logs, container, false);
		ListView lvLogs = (ListView) rootView.findViewById(R.id.lvLogs);
		Button btnLogsSend = (Button) rootView.findViewById(R.id.btnLogsSend);

		btnLogsSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ArrayList<String> filePaths = new ArrayList<String>();
				filePaths.add(CM.DAISY.getDeploymentFileName());
				IntentTools.email(CM.CTX, "daisy_logs@uvwxy.de", "", "Daisy Deployment with local Logs", filePaths);
			}
		});

		logMessagesAdapter = new LogMessageArrayAdapter(this, CM.CTX, logs);

		lvLogs.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lvLogs.setStackFromBottom(true);
		lvLogs.setAdapter(logMessagesAdapter);

		CM.BUS.register(this);

		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		CM.BUS.unregister(this);
	}

	@Subscribe
	public void receiveLog(LogMessage m) {
		logs.add(m);
		sortLogsTimeAscending();
		logMessagesAdapter.notifyDataSetChanged();
	}

	void sortLogsTimeAscending() {
		Collections.sort(logs, new Comparator<LogMessage>() {

			@Override
			public int compare(LogMessage lhs, LogMessage rhs) {
				return Double.compare(lhs.getTimestamp(), rhs.getTimestamp());
			}
		});
	}
}
