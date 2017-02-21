package com.example.curtaineffectdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	private LinearLayout mCurtain;
	private CurtainView mCurtainButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mCurtain = (LinearLayout) findViewById(R.id.topcurtain);
		mCurtainButton = (CurtainView) findViewById(R.id.curtain_button);
		mCurtainButton.setCurtain(mCurtain);
	}

}
