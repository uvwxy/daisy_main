package de.uvwxy.daisy.nodemap.guicontent;

import android.os.Handler;
import de.uvwxy.daisy.proto.Messages.LogMessage;

public class CMLogs implements IDestroy {

	/**
	 * This function remains, until CMLogs is deleted. Many references to this
	 * method still exist.
	 * 
	 * @param tag
	 * @param message
	 */

	public void add(final String tag, final String message) {
		final LogMessage m = LogMessage.newBuilder().setTag(tag).setMessage(message).setTimestamp(System.currentTimeMillis()).build();

		// the bus runs on the main looper thread

		Handler h = new Handler(CM.CTX.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				CM.BUS.post(m);
			}
		});
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}