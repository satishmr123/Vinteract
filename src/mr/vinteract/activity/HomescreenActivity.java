

package mr.vinteract.activity;

import mr.vinteract.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class HomescreenActivity extends Activity {
	
	Button interact_Button;
	ImageView logoImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.homescreen);
		
		interact_Button = (Button) findViewById(R.id.Interact_mode);
		interact_Button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent VintractIntent = new Intent(HomescreenActivity.this, VinteractDualScreenActivity.class);
				startActivity(VintractIntent);
			}
		});
		
		logoImage = (ImageView) findViewById(R.id.welcomeImage);
		logoImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent VintractIntent = new Intent(HomescreenActivity.this, VinteractDualScreenActivity.class);
				startActivity(VintractIntent);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
}