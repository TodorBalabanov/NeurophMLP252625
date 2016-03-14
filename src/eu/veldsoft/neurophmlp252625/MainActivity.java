package eu.veldsoft.neurophmlp252625;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static class DatabaseOpenHelper extends SQLiteOpenHelper {
		private double[] stringToArray(String string) {
			double result[] = null;

			String values[] = string.split("\\s+");
			result = new double[values.length];
			for (int i = 0; i < values.length; i++) {
				result[i] = Double.parseDouble(values[i]);
			}

			return result;
		}

		public DatabaseOpenHelper(Context context) {
			super(context, "training", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE training_set (_id INTEGER PRIMARY KEY, input TEXT, output TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

		public void addExample(double input[], double output[]) {
			String in = "";
			for (int i = 0; i < input.length; i++) {
				in += input[i];
				in += " ";
			}
			in = in.trim();

			String out = "";
			for (int i = 0; i < output.length; i++) {
				out += output[i];
				out += " ";
			}
			out = out.trim();

			SQLiteDatabase db = this.getWritableDatabase();
			db.execSQL("INSERT INTO training_set (input, output) VALUES ('"
					+ in + "','" + out + "');");
			db.close();
		}

		public void getAllExamples(double inputs[][], double outputs[][]) {
			inputs = new double[0][0];
			outputs = new double[0][0];

			Vector<Object> rows = new Vector<Object>();

			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery("SELECT * FROM training_set;", null);

			while (cursor.moveToNext() == true) {
				rows.add(new String[] { cursor.getString(1),
						cursor.getString(2) });
			}

			cursor.close();
			db.close();

			inputs = new double[rows.size()][];
			outputs = new double[rows.size()][];
			for (int i = 0; i < rows.size(); i++) {
				inputs[i] = stringToArray(((String[]) rows.elementAt(i))[0]);
				outputs[i] = stringToArray(((String[]) rows.elementAt(i))[1]);
			}
		}

		public void getRandomExample(double input[], double output[]) {
			input = new double[0];
			output = new double[0];

			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(
					"SELECT * FROM training_set ORDER BY RANDOM() LIMIT 1;",
					null);
			if (cursor.moveToFirst()) {
				input = stringToArray(cursor.getString(1));
				output = stringToArray(cursor.getString(2));
			}
			cursor.close();
			db.close();
		}
	};

	private Random PRNG = new Random();

	private DataSet set = null;

	private MultiLayerPerceptron net = null;

	private void normalize(double values[], double min, double max) {
		if (min > max) {
			double buffer = min;
			min = max;
			max = buffer;
		}

		for (int i = 0; i < values.length; i++) {
			values[i] = (values[i] - min) / (max - min);
		}
	}

	private void invert(double values[]) {
		for (int i = 0; i < values.length; i++) {
			values[i] = 1 - values[i];
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DatabaseOpenHelper db = new DatabaseOpenHelper(MainActivity.this);
		db.addExample(new double[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, new double[] { 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
		db.addExample(new double[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, new double[] { 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 });
		db.addExample(new double[] { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, new double[] { 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 });
		db.addExample(new double[] { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, new double[] { 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 });

		new Thread(new Runnable() {
			@Override
			public void run() {
				set = new DataSet(25, 25);
				net = new MultiLayerPerceptron(TransferFunctionType.SIGMOID,
						25, 26, 25);

				DatabaseOpenHelper db = new DatabaseOpenHelper(
						MainActivity.this);

				double inputs[][] = null;
				double outputs[][] = null;
				db.getAllExamples(inputs, outputs);
				if (inputs == null || outputs == null) {
					return;
				}

				for (int i = 0; i < inputs.length && i < outputs.length; i++) {
					set.addRow(new DataSetRow(inputs[i], outputs[i]));
				}
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

				DatabaseOpenHelper db = new DatabaseOpenHelper(
						MainActivity.this);

				double input[] = {};
				double output[] = {};
				db.getRandomExample(input, output);
				if (input == null || output == null) {
					System.err.println("Invalid example!");
					return;
				}
				if(input.length != net.getInputsCount()) {
					System.err.println("Invalid example input size!");
					return;
				}

				net.setInput(input);
				net.calculate();
				Toast.makeText(
						MainActivity.this,
						Arrays.toString(output)
								+ Arrays.toString(net.getOutput()),
						Toast.LENGTH_LONG).show();
			}
		});
	}
}
