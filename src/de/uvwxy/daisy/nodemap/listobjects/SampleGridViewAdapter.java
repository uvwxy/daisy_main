package de.uvwxy.daisy.nodemap.listobjects;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import de.uvwxy.daisy.nodemap.R;
import de.uvwxy.helper.BitmapTools;

public class SampleGridViewAdapter extends BaseAdapter {
	private final Context context;
	private final Transformation cropSquare = new CropSquareTransformation();
	private List<String> filePaths = null;

	public SampleGridViewAdapter(Context context, List<String> filePaths) {
		this.context = context;
		this.filePaths = filePaths;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SquaredImageView view = (SquaredImageView) convertView;
		if (view == null) {
			view = new SquaredImageView(context);
		}

		// Get the image URL for the current position.
		String path = getItem(position);

		// Trigger the download of the URL asynchronously into the image view.
		Picasso.with(context) //
				.load(new File(path)) //
				.error(R.drawable.missing_image) //
				.resize(320, 240)// 800x600 previously
				//.transform(cropSquare) //
				.skipCache() //
				.into(view);

		return view;
	}

	@Override
	public int getCount() {
		return filePaths.size();
	}

	@Override
	public String getItem(int position) {
		return filePaths.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	class SquaredImageView extends ImageView {
		public SquaredImageView(Context context) {
			super(context);
		}

		public SquaredImageView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
		}
	}

	class CropSquareTransformation implements Transformation {
		@Override
		public Bitmap transform(Bitmap source) {
			int size = BitmapTools.dipToPixels(context, 48);//Math.min(source.getWidth(), source.getHeight());

			int x = (source.getWidth() - size) / 2;
			int y = (source.getHeight() - size) / 2;

			Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
			if (squaredBitmap != source) {
				source.recycle();
			}
			return squaredBitmap;
		}

		@Override
		public String key() {
			return "square()";
		}
	}
}
