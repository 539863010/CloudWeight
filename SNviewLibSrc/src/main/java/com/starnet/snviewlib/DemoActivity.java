package com.starnet.snviewlib;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.starnet.snview.R;
import com.starnet.snview.sdk.SNviewer;


public class DemoActivity extends Activity {

	SNviewer m_viedoViewer;
	Boolean m_isPaly=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏  
		
		
		setContentView(R.layout.activity_demo);
		FrameLayout rootView=(FrameLayout)findViewById(R.id.videoView);
		m_viedoViewer=new SNviewer(this);
		rootView.addView(m_viedoViewer);
		
		//开始预览
		Button startButton=(Button)findViewById(R.id.btn_play);
		startButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String ip=((EditText)findViewById(R.id.editIP)).getText().toString();
				String port=((EditText)findViewById(R.id.editPort)).getText().toString();
				String userName=((EditText)findViewById(R.id.editUser)).getText().toString();
				String password=((EditText)findViewById(R.id.editPassword)).getText().toString();
				String channel=((EditText)findViewById(R.id.editChannel)).getText().toString();
				String recordName=((EditText)findViewById(R.id.editRecordName)).getText().toString();
				m_viedoViewer.setDeviceParams(ip, port, userName, password, Integer.parseInt(channel), recordName);
		
				m_viedoViewer.play();
				m_isPaly=true;
			}
		});
		
		//关闭预览
		Button stopButton=(Button)findViewById(R.id.btn_stop);
		stopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (m_viedoViewer != null) {
					m_viedoViewer.stop();
					m_isPaly=false;
				}
			}
		});
		
		//放大
		Button zoomInButton=(Button)findViewById(R.id.btn_zoom_in);
		zoomInButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				if (m_viedoViewer != null) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//放大
						m_viedoViewer.zoomIn();
						break;
					case MotionEvent.ACTION_UP:
						//停止
						m_viedoViewer.stopMove();
						break;
					}
				}
					
				

				return false;
			}
		}) ;
		
		//缩小
		Button zoomOutButton=(Button)findViewById(R.id.btn_zoom_out);
		zoomOutButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				if (m_viedoViewer != null) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//缩小
						m_viedoViewer.zoomOut();
						break;
					case MotionEvent.ACTION_UP:
						//停止
						m_viedoViewer.stopMove();
						break;
					}
				}
				return false;
			}
		}) ;
		
		//方向移动
		
		OnTouchListener derctionListener= new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// TODO Auto-generated method stub
				if (m_viedoViewer != null) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						int id = view.getId();
						if (id == R.id.btn_left_up) {
							//左上
							m_viedoViewer.moveLeftUp();
						} else if (id == R.id.btn_up) {
							//上
							m_viedoViewer.moveUp();
						} else if (id == R.id.btn_right_up) {
							//右上
							m_viedoViewer.moveRightUp();
						} else if (id == R.id.btn_left) {
							//左
							m_viedoViewer.moveLeft();
						} else if (id == R.id.btn_right) {
							//右
							m_viedoViewer.moveRight();
						} else if (id == R.id.btn_left_down) {
							//左下
							m_viedoViewer.moveLeftDown();
						} else if (id == R.id.btn_down) {
							//下
							m_viedoViewer.moveDown();
						} else if (id == R.id.btn_right_down) {
							//右下
							m_viedoViewer.moveRightDown();
						} else {
						}
						break;
					case MotionEvent.ACTION_UP:
						//停止
						m_viedoViewer.stopMove();
						break;
					}
				}
				return false;
			}
		};
		// 左上
		((Button) findViewById(R.id.btn_left_up))
				.setOnTouchListener(derctionListener);
		// 上
		((Button) findViewById(R.id.btn_up))
				.setOnTouchListener(derctionListener);
		// 右上
		((Button) findViewById(R.id.btn_right_up))
				.setOnTouchListener(derctionListener);
		// 左
		((Button) findViewById(R.id.btn_left))
				.setOnTouchListener(derctionListener);
		// 右
		((Button) findViewById(R.id.btn_right))
				.setOnTouchListener(derctionListener);
		// 左下
		((Button) findViewById(R.id.btn_left_down))
				.setOnTouchListener(derctionListener);
		// 下
		((Button) findViewById(R.id.btn_down))
				.setOnTouchListener(derctionListener);
		// 右下
		((Button) findViewById(R.id.btn_right_down))
				.setOnTouchListener(derctionListener);
		
		
		//预置点-定位
		((Button) findViewById(R.id.btn_goto_point)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (m_viedoViewer != null){
					int pointNum=Integer.parseInt(((EditText)findViewById(R.id.editPrePoint))
							.getText().toString());
					m_viedoViewer.gotoPresetPoint(pointNum);
				}
			}
		});
		
		// 预置点-设置
		((Button) findViewById(R.id.btn_set_point))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (m_viedoViewer != null) {
							int pointNum = Integer
									.parseInt(((EditText) findViewById(R.id.editPrePoint))
											.getText().toString());
							m_viedoViewer.setPresetPoint(pointNum);
						}
					}
				});
		
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//关闭
		if (m_viedoViewer != null) {
			m_viedoViewer.stop();
			//m_isPaly=false;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//重新恢复播放
		if(m_isPaly){
			m_viedoViewer.play();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
	}
	
	

}