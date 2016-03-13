package eu.veldsoft.neurophmlp252625;

import java.util.Arrays;
import java.util.Random;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Random PRNG = new Random();

	private DataSet set = null;

	private MultiLayerPerceptron net = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		new Thread(new Runnable() {
			@Override
			public void run() {
				set = new DataSet(25, 25);
				net = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,
						25, 26, 25);

				set.addRow(new DataSetRow(new double[] { 1, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }));
				set.addRow(new DataSetRow(new double[] { 0, 1, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 }));
				set.addRow(new DataSetRow(new double[] { 0, 0, 1, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 }));
				set.addRow(new DataSetRow(new double[] { 0, 0, 0, 1, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
						new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 }));

			}
		}).start();

		findViewById(R.id.train).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (set == null || net == null) {
					Toast.makeText(MainActivity.this, "" + set + net,
							Toast.LENGTH_LONG).show();
					return;
				}

				net.learnInNewThread(set);
			}
		});

		findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (set == null || net == null) {
					Toast.makeText(MainActivity.this, "" + set + net,
							Toast.LENGTH_LONG).show();
					return;
				}

				switch (PRNG.nextInt(4)) {
				case 0:
					net.setInput(new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
							0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
					net.calculate();
					Toast.makeText(MainActivity.this,
							Arrays.toString(net.getOutput()) + 0, Toast.LENGTH_LONG)
							.show();
					break;
				case 1:
					net.setInput(new double[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
							0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
					net.calculate();
					Toast.makeText(MainActivity.this,
							Arrays.toString(net.getOutput()) + 1, Toast.LENGTH_LONG)
							.show();
					break;
				case 2:
					net.setInput(new double[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
							0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
					net.calculate();
					Toast.makeText(MainActivity.this,
							Arrays.toString(net.getOutput()) + 2, Toast.LENGTH_LONG)
							.show();
					break;
				case 3:
					net.setInput(new double[] { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
							0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
					net.calculate();
					Toast.makeText(MainActivity.this,
							Arrays.toString(net.getOutput()) + 3, Toast.LENGTH_LONG)
							.show();
					break;
				}
			}
		});
	}
}
