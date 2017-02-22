package com.mare.curtaineffect;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.mare.curtaineffect.R;

public class MainActivity extends Activity {
	private ViewGroup mCurtain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mCurtain =  (ViewGroup) findViewById(R.id.topcurtain);
		CurtainView handler = (CurtainView) findViewById(R.id.curtain_button);
		handler.setCurtain(mCurtain);
		
	}

}
