package cz.thc.eurosignal.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import cz.thc.eurosignal.R;
import cz.thc.eurosignal.view.SVGDrawView;

public class SignActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.sign);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.activity_sign, menu);
	return true;
    }


    public void onOk(View view) {

	String path = ((SVGDrawView) findViewById(R.id.signDrawView)).path
		.toSVGString();

	System.out.println(path);

	startActivity(new Intent(this, MainActivity.class));

    }

}
