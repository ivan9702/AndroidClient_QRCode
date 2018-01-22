package com.startek.fm220;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;


public class GridViewActivity extends Activity {
	private static final String TAG = "GridViewActivity";

	private GridView gridView;
	private PhotoUpImageBucket photoUpImageBucket;
	private GridViewAdapter adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		setContentView(R.layout.activity_gridview);

		Intent intent = getIntent();
		photoUpImageBucket = (PhotoUpImageBucket) intent.getSerializableExtra("imagelist");
		final String[] frameMacAddress = intent.getStringArrayExtra("frameList");

		//TextView title = (TextView)findViewById(R.id.album_name);
		//title.setText(photoUpImageBucket.getBucketName());

		gridView = (GridView) findViewById(R.id.main_page_gridview);
		adapter = new GridViewAdapter(photoUpImageBucket.getImageList(), GridViewActivity.this);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String path = adapter.takepath(position);
				Log.d("FM220", "path: "+path);
				Intent intent = new Intent(GridViewActivity.this,ImageDetailsActivity.class);
				intent.putExtra("image_path", path);
				//intent.putExtra("frameList", frameMacAddress);
				//Intent intent = new Intent(getContext(), ImageDetailsActivity.class);
				//intent.putExtra("image_path", getImagePath(mImageUrl));
				//getContext().startActivity(intent);
				//
				startActivity(intent);
			}
		});
/*
		mPref = Preferences.getInstance();

		mFrameBar = (FrameBar)findViewById(R.id.frame_bar);
		mFrameBar.update(Arrays.asList(frameMacAddress), true);*/
	}

	@Override
	protected void onResume() {
		super.onResume();

		/*if (!mPref.getSkipTutor()) {
			showTutorial();
		} else {
			dismissTutorial();
		}*/
	}

	public void onBackClick(View v) {
		finish();
	}


}
