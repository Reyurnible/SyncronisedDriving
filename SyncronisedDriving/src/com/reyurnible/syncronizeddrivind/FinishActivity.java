package com.reyurnible.syncronizeddrivind;

import com.reyurnible.system.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FinishActivity extends Activity implements OnClickListener {
	
	private StringBuilder mShareText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finish);
		
		findViewById(R.id.finish_button_close).setOnClickListener(this);
		findViewById(R.id.finish_button_share).setOnClickListener(this);
		
		//0->accel 1->brake 2->hundle 3->total
		TextView[] resultParcentTextView = new TextView[4];
		TextView[] resultGradeTextView = new TextView[4];
		ProgressBar[] resultProgressBar = new ProgressBar[4];
		
		resultParcentTextView[0] = (TextView)findViewById(R.id.finish_textview_accel_parcent);
		resultParcentTextView[1] = (TextView)findViewById(R.id.finish_textview_brake_parcent);
		resultParcentTextView[2] = (TextView)findViewById(R.id.finish_textview_hundle_parcent);
		resultParcentTextView[3] = (TextView)findViewById(R.id.finish_textview_total_parcent);
		
		resultGradeTextView[0] = (TextView)findViewById(R.id.finish_textview_accel_result);
		resultGradeTextView[1] = (TextView)findViewById(R.id.finish_textview_brake_result);
		resultGradeTextView[2] = (TextView)findViewById(R.id.finish_textview_hundle_result);
		resultGradeTextView[3] = (TextView)findViewById(R.id.finish_textview_total_result);
		
		resultProgressBar[0] = (ProgressBar)findViewById(R.id.finish_progressbar_accel);
		resultProgressBar[1] = (ProgressBar)findViewById(R.id.finish_progressbar_brake);
		resultProgressBar[2] = (ProgressBar)findViewById(R.id.finish_progressbar_hundle);
		resultProgressBar[3] = (ProgressBar)findViewById(R.id.finish_progressbar_total);
		
		Intent intent = getIntent();
		int[] point = new int[4];
		point[0] = intent.getIntExtra(Constants.EXTRA_FINISH_INT_ACCEL, 100);
		point[1] = intent.getIntExtra(Constants.EXTRA_FINISH_INT_BRAKE, 100);
		point[2] = intent.getIntExtra(Constants.EXTRA_FINISH_INT_HUNDLE, 100);
		point[3] = (point[0]+point[1]+point[2])/3;
		
		for (int i=0; i<4;i++) {
			resultParcentTextView[i].setText(Integer.toString(point[i])+"%");
			resultGradeTextView[i].setText(getGrade(point[i]));
			resultProgressBar[i].setProgress(point[i]);
		}
		
		mShareText = new StringBuilder();
		mShareText.append("今回のシンクロ率は、");
		mShareText.append("アクセルシンクロ率：");
		mShareText.append(getGrade(point[0]));
		mShareText.append(" ");
		mShareText.append("ブレーキシンクロ率：");
		mShareText.append(getGrade(point[1]));
		mShareText.append(" ");
		mShareText.append("ハンドルシンクロ率：");
		mShareText.append(getGrade(point[2]));
		mShareText.append(" ");
		mShareText.append("トータルシンクロ率：");
		mShareText.append(getGrade(point[3]));
		mShareText.append("でした");
		mShareText.append("#syncroniseddriving");
		
		int minPoint = point[0];
		int minIndex = 0;
		for (int i=1; i<3; i++) {
			if (minPoint > point[i]) {
				minPoint = point[i];
				minIndex = i;
			}
		}
		
		TextView resultText = (TextView) findViewById(R.id.finish_textview_title);
		if (minPoint > 75) {
			resultText.setText("すばらしい運転でした！！いつもこの調子で運転しましょう");
		} else {
			switch(minIndex) {
			case 0:
				resultText.setText("「警告」あなたの運転は、模範運転ではありません。\n直ちに心を入れ替えるか、免許を返上してください。");
				break;
			case 1:
				resultText.setText("同乗者はブレーキのタイミングを遅く感じています。黄色信号は、急いでわたれ！ではありません。\n早めのブレーキを心がけてください。");
				break;
			case 2:
				resultText.setText("わき見運転は、危険です。運転に集中しましょう。");
				break;
			default:
				break;
			}
		}
	}
	
	private String getGrade(int point) {
		if(point>85){
			return "S";
		}else if(point > 70){
			return "A";
		}else if(point > 50){
			return "B";
		}else if(point > 30){
			return "C";
		}else{
			return "D";
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.finish_button_close:
			finish();
			break;
		case R.id.finish_button_share:
			Intent intent = new Intent( Intent.ACTION_SEND );
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, mShareText.toString());
			startActivity(intent);
			break;
		}
		
	}
	
}
