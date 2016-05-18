package com.kiwihealthcare.bpppgcollector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.kiwihealthcare.Ccardiacfinger.algorithm.PicProcess;
import com.kiwihealthcare.bpppgcollector.po.AT;
import com.kiwihealthcare.bpppgcollector.utils.AutoBT;
import com.kiwihealthcare.bpppgcollector.utils.CNweatherUtils;
import com.kiwihealthcare.bpppgcollector.utils.DateUtils;
import com.kiwihealthcare.bpppgcollector.view.WaveDraw;

public class BPActivity extends Activity {

	private static final int PICKFILE_RESULT_CODE = 2;
	private static final int INI_VERSION = 1;
	private TextView weatherText;
	private EditText systolicEdit;
	private EditText diastolicEdit;
	private TextView sysUnitText;
	private TextView diaUnitText;
	private EditText heartEdit;
	private EditText noteEdit;
	private ImageButton historyButton;
	private ImageButton resetButton;
	private Button saveButton;
	private Button dateButton;
	private Button timeButton;
	private EditText nameEdit;
	private EditText ageEdit;
	private EditText recTimeEdit;
	private ImageView btImageView;

	private EditText phoneEdit;
	private Spinner genderSpinner;
	private Button addNoteButton;
	private Button startCamRecButton;

	private Button startOxiRecButton;
	private Button connectBluetoothButton;
	private ToggleButton flashToggleButton;
	// private TextView camRecState;
	// private TextView oxiRecState;
	private TextView camRecFinishState;
	private TextView oxiRecFinishState;
	private String note;
	private String pathString = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/BPrecord/";
	private File recPath = new File(pathString);
	private File proFile = new File(pathString + "users.idx");
	private File recFile;
	private Wini proIni;
	private Wini recIni;

	private RadioGroup cdHistoryRadioGroup;
	private RadioButton cdHistoryYesRadioButton;
	private RadioButton cdHistoryNoRadioButton;

	private CheckBox camCheckBox;
	private CheckBox oxiCheckBox;

	int[] btAmp = new int[128];
	long[] btTime = new long[128];
	boolean onPreview = false; // 是否正在预览

	int camHeight = 0; // 采样图像的高
	int camWidth = 0; // 采样图像的宽

	byte[] mCallbackBuf;
	boolean canOpenFlash = true; // 默认打开闪光灯
	// boolean oxiThreadStarted = false;

	FrameLayout sv_hidden;
	private MyCameraSurfaceView myCameraSurfaceView;
	SurfaceView camWaveSurfaceView;
	SurfaceView oxiWaveSurfaceView;

	private WaveDraw camWaveDraw;
	private WaveDraw oxiWaveDraw;

	private PicProcess picProcess;
	int hnum = 32, wnum = 32;
	int hbeg, wbeg;

	Camera myCamera = null; // 管理相机
	private int cachedYear;
	private int cachedMonthOfYear;
	private int cachedDayOfMonth;
	private int cachedHourOfDay;
	private int cachedMinute;
	private String cachedWeatherString;

	ArrayList<AT> camPPGList = new ArrayList<AT>();
	ArrayList<AT> oxiPPGList = new ArrayList<AT>();
	boolean oxiRecordFinished = false;
	boolean camRecordFinished = false;
	boolean camRecordEnabled = false;
	boolean oxiRecordEnabled = false;
	private int recTime = 120;
	long cam_start_time = -1;
	long oxi_start_time = -1;
	private static int NOTE_REQUEST = 1;
	private Set<String> nameSet = null;

	// private WeatherItem2 cachedWeatherItem;
	private CNweatherUtils cnWeather = new CNweatherUtils();

	AutoBT bt;

	private BPActivity thisRef = this;

	OxiThread oxiThread;// = new OxiThread();

	class OxiThread extends Thread {
		// public volatile boolean running = true;

		@Override
		public void run() {

			while (!Thread.currentThread().isInterrupted()) {
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
					return;
				}
				processOxi();
			}

			return;
		}

		// public void restart() {
		// run();
		// }

		private void processOxi() {
			if (oxiRecordEnabled) {
				long timeStamp;
				if (oxiRecordFinished == false) {
					timeStamp = System.currentTimeMillis();
					if (bt.isRunning()) {
						if (oxi_start_time == -1) {
							oxi_start_time = timeStamp;
						}

						final String cdTime = String
								.valueOf((int) (recTime - (timeStamp - oxi_start_time) / 1000));
						runOnUiThread(new Runnable() {

							@Override
							public void run() {

								oxiRecFinishState.setText(cdTime);
							}
						});

						if (timeStamp - oxi_start_time > recTime * 1000) {
							thisRef.oxiRecordFinished = true;

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									oxiRecFinishState.setText(getResources()
											.getString(R.string.finished));
								}
							});

							oxiWaveDraw.stop();
							interrupt();

							// if (camRecordFinished || (!camRecordEnabled)) {
							// thisRef.stop();
							// return;
							// }
						}

						int n = bt.getData(btAmp, btTime);
						for (int i = 0; i < n; i++) {
							int tmp = 30 + btAmp[i];
							oxiPPGList.add(new AT((float) tmp, btTime[i]));
							oxiWaveDraw.add(30 + 100 - (float) tmp);
							// oxiRecState.setText(String.valueOf(tmp));
							// Log.d("btAmp", String.valueOf(tmp));
						}

					} else {
						oxi_start_time = -1;
						oxiPPGList.clear();

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								btImageView.setVisibility(View.INVISIBLE);
							}
						});

						oxiWaveDraw.restart();
					}
				}
			}
		}
	};

	TextWatcher setTimeTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if (recTimeEdit.getText().toString() == null
					|| recTimeEdit.getText().toString().length() == 0
					|| !TextUtils.isDigitsOnly(recTimeEdit.getText())) {
				recTime = 90;
			} else {
				recTime = Integer.parseInt(recTimeEdit.getText().toString());
			}

		}

	};

	boolean nameExists = false;

	TextWatcher nameTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			String nameString = nameEdit.getText().toString();
			if (nameSet != null && nameString != null
					&& nameString.length() != 0) {
				if (nameSet.contains(nameString.trim())) {
					nameExists = true;

				} else {
					nameExists = false;
				}
			}
		}

	};

	// private LocationItem2 cachedLocationItem;
	// private WeatherAndLocationItem cachedWeatherAndLocationItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_bp);
		initCachedValue();
		init();
		initBT();
		initContentView();
		myCamera = getCameraInstance();
	}

	private void initContentView() {
		weatherText = (TextView) findViewById(R.id.add_weather_text);
		dateButton = (Button) findViewById(R.id.add_date_button);
		timeButton = (Button) findViewById(R.id.add_time_button);
		nameEdit = (EditText) findViewById(R.id.add_name_edit);
		ageEdit = (EditText) findViewById(R.id.add_age_edit);
		genderSpinner = (Spinner) findViewById(R.id.gender_spinner);
		systolicEdit = (EditText) findViewById(R.id.add_systolic_edit);
		sysUnitText = (TextView) findViewById(R.id.add_systolic_unit_text);
		diastolicEdit = (EditText) findViewById(R.id.add_diastolic_edit);
		diaUnitText = (TextView) findViewById(R.id.add_diastolic_unit_text);

		heartEdit = (EditText) findViewById(R.id.add_heart_edit);
		noteEdit = (EditText) findViewById(R.id.add_note_edit);
		addNoteButton = (Button) findViewById(R.id.add_add_note_button);
		// flashToggleButton = (ToggleButton)
		// findViewById(R.id.flash_toggleButton);
		startCamRecButton = (Button) findViewById(R.id.start_camera_record_button);
		// camRecState = (TextView) findViewById(R.id.camera_record_state);
		// oxiRecState = (TextView) findViewById(R.id.bluetooth_record_state);
		camRecFinishState = (TextView) findViewById(R.id.camera_record_finish_state);
		oxiRecFinishState = (TextView) findViewById(R.id.bluetooth_record_finish_state);
		// resetButton = (Button) findViewById(R.id.add_reset_button);
		resetButton = (ImageButton) findViewById(R.id.reset_image_button);
		historyButton = (ImageButton) findViewById(R.id.history_image_button);
		saveButton = (Button) findViewById(R.id.add_save_button);
		recTimeEdit = (EditText) findViewById(R.id.rec_time_edit);
		recTimeEdit.setText(Integer.toString(recTime));
		sysUnitText.setText(getResources().getString(
				R.string.blood_pressure_unit_mmhg));
		diaUnitText.setText(getResources().getString(
				R.string.blood_pressure_unit_mmhg));
		cdHistoryRadioGroup = (RadioGroup) findViewById(R.id.cd_history_radioGroup);
		cdHistoryYesRadioButton = (RadioButton) findViewById(R.id.cd_yes);
		cdHistoryNoRadioButton = (RadioButton) findViewById(R.id.cd_no);

		historyButton.setOnClickListener(onHistoryButtonClickListener);
		dateButton.setText(DateUtils.getStringWithStyle3(cachedYear,
				cachedMonthOfYear, cachedDayOfMonth));
		timeButton.setText(DateUtils.getStringWithStyle4(cachedHourOfDay,
				cachedMinute));

		dateButton.setOnClickListener(onDateButtonClickListener);
		timeButton.setOnClickListener(onTimeButtonClickListener);
		addNoteButton.setOnClickListener(onAddNoteClickListener);
		camCheckBox = (CheckBox) findViewById(R.id.cam_checkBox);
		oxiCheckBox = (CheckBox) findViewById(R.id.oxi_checkBox);

		camWaveSurfaceView = (SurfaceView) findViewById(R.id.cam_sfv);
		oxiWaveSurfaceView = (SurfaceView) findViewById(R.id.oxi_sfv);

		camWaveSurfaceView.setZOrderOnTop(true);
		oxiWaveSurfaceView.setZOrderOnTop(true);
		camWaveSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		oxiWaveSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		camWaveDraw = new WaveDraw();
		oxiWaveDraw = new WaveDraw();

		camCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					camRecordEnabled = true;
				} else {
					camRecordEnabled = false;
				}

			}
		});

		oxiCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					oxiRecordEnabled = true;
					bt.resume();
				} else {

					oxiRecordEnabled = false;
					bt.pause();
				}

			}
		});

		// flashToggleButton
		// .setOnCheckedChangeListener(new OnCheckedChangeListener() {
		//
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView,
		// boolean isChecked) {
		// if (isChecked) {
		// canOpenFlash = true;
		// } else {
		// canOpenFlash = false;
		// }
		//
		// }
		// });
		startCamRecButton.setOnClickListener(onStartCamButtonClickListener);

		// connectBluetoothButton.setOnClickListener(onConnectBTButtonClickListener);
		sv_hidden = (FrameLayout) findViewById(R.id.sfv0);
		saveButton.setOnClickListener(onSaveButtonClickListener);
		resetButton.setOnClickListener(onResetButtonClickListener);
		recTimeEdit.addTextChangedListener(setTimeTextWatcher);
		nameEdit.addTextChangedListener(nameTextWatcher);
		nameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (nameExists) {
						String nameString = nameEdit.getText().toString();
						String genderString = proIni.get(nameString, "Gender");
						String phoneString = proIni.get(nameString, "Phone");
						String ageString = proIni.get(nameString, "Age");
						String cdHistoryString = proIni.get(nameString,
								"CardiHistory");
						phoneEdit.setText(phoneString);
						ageEdit.setText(ageString);
						if (Integer.parseInt(genderString) == 1) {
							genderSpinner.setSelection(0);
						} else {
							genderSpinner.setSelection(1);
						}
						if (Integer.parseInt(cdHistoryString) == 1) {
							cdHistoryRadioGroup.check(cdHistoryYesRadioButton
									.getId());
						} else {
							cdHistoryRadioGroup.check(cdHistoryNoRadioButton
									.getId());
						}
					}

				}

			}
		});
		phoneEdit = (EditText) findViewById(R.id.add_phone_edit);

		btImageView = (ImageView) findViewById(R.id.bt_img);

	}

	private void initCachedValue() {
		Calendar calendar = Calendar.getInstance();
		cachedYear = calendar.get(Calendar.YEAR);
		cachedMonthOfYear = calendar.get(Calendar.MONTH);
		cachedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		cachedHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		cachedMinute = calendar.get(Calendar.MINUTE);

		new GetWeatherTask().execute();
		// cachedWeatherString = cnWeather.getWeatherString();

	}

	private void initBT() {
		bt = new AutoBT(this, true);
	}

	private void init() {

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		picProcess = PicProcess.getInstance();
		try {
			if (!recPath.exists()) {
				try {
					recPath.mkdirs();
				} catch (Exception e) {
					ToastManager.show(getApplicationContext(),
							"init Create folder failed!");
				}
			}
			if (proFile.exists()) {
				proIni = new Wini(proFile);
				nameSet = proIni.keySet();
			} else {
				nameSet = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void setCamera() {
		myCamera = getCameraInstance();

		Parameters params = myCamera.getParameters();
		List<Camera.Size> sizes = params.getSupportedPictureSizes();
		int minWidth = Integer.MAX_VALUE, minHeight = Integer.MAX_VALUE;
		for (int i = 0; i < sizes.size(); i++) {
			Log.i("PictureSize", "Supported Size: " + sizes.get(i).width
					+ " height : " + sizes.get(i).height);
			if (sizes.get(i).width < minWidth) {
				minWidth = sizes.get(i).width;
				minHeight = sizes.get(i).height;
			}
		}
		Log.i("PictureSize", "Min Size: " + minWidth + " height : " + minHeight);

		List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
		for (int i = 0; i < fpsRanges.size(); i++) {
			Log.i("Fps ranges:", String.valueOf(fpsRanges.get(i)[0] / 1000)
					+ ' ' + String.valueOf(fpsRanges.get(i)[1] / 1000));
		}

		params.setPictureSize(minWidth, minHeight);
		params.setPreviewSize(minWidth, minHeight);
		myCamera.setParameters(params);

		camHeight = myCamera.getParameters().getPreviewSize().height;
		camWidth = myCamera.getParameters().getPreviewSize().width;
		Log.i("SavedPictureSize", "Size: " + camWidth + " height : "
				+ camHeight);
		mCallbackBuf = new byte[camHeight * camWidth * 3 / 2]; // 用于previewcallback
		hbeg = (camHeight - hnum) / 2;
		wbeg = (camWidth - wnum) / 2;
		myCamera.setPreviewCallbackWithBuffer(new MyPreview());
		myCamera.addCallbackBuffer(mCallbackBuf);

		sv_hidden.removeAllViews(); // 是不是应该先清空
		myCameraSurfaceView = new MyCameraSurfaceView(getApplicationContext(),
				myCamera);

		sv_hidden.addView(myCameraSurfaceView);
		// sv_hidden.setVisibility(View.INVISIBLE);

	}

	public void btConnected() {
		btImageView.setImageDrawable(getResources().getDrawable(
				R.drawable.bluetooth));
		oxiCheckBox.setChecked(true);
	}

	public void btDisconnected() {
		btImageView.setImageDrawable(getResources().getDrawable(
				R.drawable.bluetoothnon));
		oxiCheckBox.setChecked(false);
	}

	private void startPreview() {
		if (myCamera != null) {
			myCamera.stopPreview();
		} else {
			myCamera = getCameraInstance();
		}
		Parameters parameters = myCamera.getParameters();
		if (canOpenFlash) {
			if (camRecordEnabled) {
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			}

		} else {
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		}

		parameters.setPreviewFormat(ImageFormat.NV21);
		myCamera.setParameters(parameters);
		myCamera.setPreviewCallbackWithBuffer(new MyPreview());
		myCamera.addCallbackBuffer(mCallbackBuf);
		myCamera.startPreview();
	}

	public void start() {
		//
		// if (pm == null)
		// pm = new ProcessManager(mainActivity);

		if (onPreview) {
			onPreview = false;
			stop();
		}
		if (!onPreview) {
			// record = new Record();
			onPreview = true;

			startTimer.start(); // 最后开始

		}
	}

	public void stop() {
		// 计算出稳定值
		if (camRecordEnabled) {
			camWaveDraw.stop();
			// camWaveSurfaceView.setVisibility(View.INVISIBLE);
		}
		if (oxiRecordEnabled) {
			oxiWaveDraw.stop();
			if (oxiThread != null) {
				oxiThread.interrupt();
			}

			// oxiWaveSurfaceView.setVisibility(View.INVISIBLE);
		}
		if (onPreview) {
			// stopDraw1=true;
			// stopDraw2=true;

			// sv_hidden.setVisibility(View.INVISIBLE);
			sv_hidden.removeAllViews();
			myCamera.stopPreview();

			myCamera.setPreviewCallback(null);
			myCamera.lock();
			myCamera.release();
			myCamera = null;
			onPreview = false;

			/*
			 * cDataProcess.stopHeart(); cDataProcess.stopResp();
			 * cDataProcess.clearBuf();
			 * 
			 * dataProcess.stopHeart(); dataProcess.stopResp();
			 * dataProcess.clearBuf();
			 */
			// ccc.Deinitialize();
			// clearList(heartList);
			// clearList(respList);
			// heartnum=0;
			// respnum=0;
			// record.clear();
			// pm.reset();
		}
		startCamRecButton.setEnabled(true);
		startTimer.cancel();

	}

	CountDownTimer startTimer = new CountDownTimer(1000, 1000) {
		@Override
		public void onFinish() {
			startPreview();
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}
	};

	class MyPreview implements PreviewCallback {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {

			/*
			 * if(heartnum>=heartStop){ message.post(mainActivity.changeState);
			 * return; }
			 */
			long timeStamp = System.currentTimeMillis();
			if (camRecordEnabled) {

				if (camRecordFinished == false) {
					picProcess.calPicData(data, camWidth, camHeight);
					if (!picProcess.hasFinger()) {// 没有手指
						cam_start_time = -1;
						camPPGList.clear();
						camWaveDraw.restart();
						ToastManager.show(getApplicationContext(),
								getResources().getString(R.string.help));
					} else {
						if (cam_start_time == -1) {
							cam_start_time = timeStamp;
						}
						camRecFinishState
								.setText(String
										.valueOf((int) (recTime - (timeStamp - cam_start_time) / 1000)));
						if (timeStamp - cam_start_time > recTime * 1000) {
							// stop();
							camRecordFinished = true;
							camRecFinishState.setText(getResources().getString(
									R.string.finished));
							camWaveDraw.stop();
							if (oxiRecordFinished || (!oxiRecordEnabled)) {
								stop();
								return;
							}
						}
						float amp = 256 - picProcess.getAmp();
						camWaveDraw.add(256 - amp);
						camPPGList.add(new AT(amp, timeStamp));
						// camRecState.setText(String.valueOf(amp));
						// Log.d("amp", String.valueOf(amp));
					}
				}
			} else {
				sv_hidden.setVisibility(View.INVISIBLE);
			}

			camera.addCallbackBuffer(data);
		}
	}

	private Camera getCameraInstance() {
		if (myCamera != null) {
			myCamera.release();
		}
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.e("Camera", "Camera init failed!");
		}
		return c; // returns null if camera is unavailable
	}

	private View.OnClickListener onDateButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			getDateFromDatePicker();
		}
	};

	private View.OnClickListener onTimeButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			getTimeFromTimePicker();
		}
	};

	private View.OnClickListener onAddNoteClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			note = noteEdit.getText().toString();
			Intent intent = new Intent();
			intent.setClassName("com.kiwihealthcare.bpppgcollector",
					"com.kiwihealthcare.bpppgcollector.controller.activity.AddNoteActivity");
			intent.putExtra("note", note);
			startActivityForResult(intent, NOTE_REQUEST);
		}
	};

	private View.OnClickListener onStartCamButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (camRecordEnabled || oxiRecordEnabled) {

				if (camRecordEnabled) {
					setCamera();
					camPPGList.clear();
					startCamWaveDraw();
					camWaveSurfaceView.setVisibility(View.VISIBLE);
					sv_hidden.setVisibility(View.VISIBLE);
					start();
				}
				if (oxiRecordEnabled) {
					oxiPPGList.clear();
					oxiThread = new OxiThread();
					// if (!oxiThreadStarted) {
					// oxiThread.start();
					// oxiThreadStarted = true;
					// } else {
					// oxiThread.restart();
					// }
					oxiThread.start();
					startOxiWaveDraw();
					oxiWaveSurfaceView.setVisibility(View.VISIBLE);
				}

				startCamRecButton.setEnabled(false);
			}

		}
	};

	private View.OnClickListener onResetButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			reset();
		}
	};

	public View.OnClickListener onSaveButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			saveData();
		}
	};

	private View.OnClickListener onHistoryButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (recPath.exists()) {
				Uri s = Uri.fromFile(recPath);
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("file/*");
				startActivityForResult(intent, PICKFILE_RESULT_CODE);

			}
		}
	};

	// private View.OnClickListener onConnectBTButtonClickListener = new
	// View.OnClickListener() {
	// @Override
	// public void onClick(View v) {
	//
	// }
	// };

	@Override
	protected void onResume() {
		super.onResume();
		// setCamera();
		bt.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		camWaveDraw.stop();
		oxiWaveDraw.stop();
		sv_hidden.removeAllViews();
		if (myCamera != null) {
			stop();
			if (myCamera != null) {
				myCamera.release();
			}
		}

		if (oxiThread != null) {
			oxiThread.interrupt();
		}
		bt.pause();
	}

	@Override
	protected void onStop() {
		if (myCamera != null) {
			stop();
			if (myCamera != null) {
				myCamera.release();
			}
		}
		bt.clear();
		super.onStop();
	}

	private void getDateFromDatePicker() {
		DatePickerDialog dialog = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						cachedYear = year;
						cachedMonthOfYear = monthOfYear;
						cachedDayOfMonth = dayOfMonth;
						dateButton.setText(DateUtils
								.getStringWithStyle3(cachedYear,
										cachedMonthOfYear, cachedDayOfMonth));
					}
				}, cachedYear, cachedMonthOfYear, cachedDayOfMonth);
		dialog.show();
	}

	private void getTimeFromTimePicker() {
		TimePickerDialog dialog = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						cachedHourOfDay = hourOfDay;
						cachedMinute = minute;
						timeButton.setText(DateUtils.getStringWithStyle4(
								cachedHourOfDay, cachedMinute));
					}
				}, cachedHourOfDay, cachedMinute, true);
		dialog.show();
	}

	private void saveData() {
		if (TextUtils.isEmpty(nameEdit.getText().toString())) {
			ToastManager.show(getApplicationContext(), getResources()
					.getString(R.string.name_empty));
			return;
		}
		if (TextUtils.isEmpty(ageEdit.getText().toString())) {
			ToastManager.show(getApplicationContext(), getResources()
					.getString(R.string.age_empty));
			return;
		}
		if (TextUtils.isEmpty(systolicEdit.getText().toString())
				|| TextUtils.isEmpty(diastolicEdit.getText().toString())) {
			ToastManager.show(getApplicationContext(), getResources()
					.getString(R.string.bp_empty));
			return;
		}
		if (TextUtils.isEmpty(heartEdit.getText().toString())) {
			ToastManager.show(getApplicationContext(), getResources()
					.getString(R.string.heart_rate_empty));
			return;
		}

		String gender;
		if (genderSpinner.getSelectedItemPosition() == 0) {
			gender = "1";
		} else {
			gender = "2";
		}

		String recFileName = dateButton.getText().toString() + "-"
				+ cachedHourOfDay + "-" + cachedMinute + "-"
				+ nameEdit.getText().toString() + ".rcd";
		recFileName = recFileName.trim();

		// Log.d("Rec File name", recFileName);
		int cdHistory;
		if (cdHistoryYesRadioButton.isChecked()) {
			cdHistory = 1;
		} else {
			cdHistory = 0;
		}

		String nameString = nameEdit.getText().toString().trim();
		try {
			if (!recPath.exists()) {
				try {
					recPath.mkdirs();
				} catch (Exception e) {
					ToastManager.show(getApplicationContext(),
							"Create folder failed!");
				}
			}
			// proFile = new File("/sdcard/Crecord/users.idx");

			if (!proFile.exists()) {
				try {
					proFile.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
					ToastManager.show(getApplicationContext(),
							"Create user file failed!");
					return;
				}

			}

			proIni = new Wini(proFile);
			if (nameSet == null || nameSet.contains(nameString) == false) {
				proIni.put(nameString, "Age", ageEdit.getText().toString());
				proIni.put(nameString, "Gender", gender);
				proIni.put(nameString, "Phone", phoneEdit.getText().toString());
				proIni.put(nameString, "CardiHistory", cdHistory);
				proIni.put(nameString, "Version", INI_VERSION);
			}

			recFile = new File(pathString + recFileName);
			try {
				recFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				ToastManager.show(getApplicationContext(),
						"Create record file failed!");
				return;
			}

			recIni = new Wini(recFile);

			recIni.put("profile", "Name", nameString);
			recIni.put("profile", "Age", ageEdit.getText().toString());
			recIni.put("profile", "Phone", phoneEdit.getText().toString());
			recIni.put("profile", "Gender", gender);
			recIni.put("profile", "CardiHistory", cdHistory);
			recIni.put("profile", "Version", INI_VERSION);

			if (cnWeather != null && cnWeather.getTemp1() != null
					&& cnWeather.getTemp2() != null
					&& cnWeather.getWeather() != null) {
				recIni.put("weather", "Status", cnWeather.getWeather());
				recIni.put("weather", "Temp1", cnWeather.getTemp1());
				recIni.put("weather", "Temp2", cnWeather.getTemp2());
			}

			recIni.put("bp", "Systolic", systolicEdit.getText().toString());
			recIni.put("bp", "Diastolic", diastolicEdit.getText().toString());
			recIni.put("bp", "Note", noteEdit.getText().toString());
			recIni.put("bp", "HeartRate", heartEdit.getText().toString());

			int camRecLength = camPPGList.size();
			int oxiRecLength = oxiPPGList.size();
			if (camRecLength != 0) {
				StringBuilder camTimeStringBuilder = new StringBuilder();
				StringBuilder camAmpStringBuilder = new StringBuilder();
				for (AT at : camPPGList) {
					camTimeStringBuilder.append(at.getTime());
					camTimeStringBuilder.append(',');
					camAmpStringBuilder.append(at.getAmp());
					camAmpStringBuilder.append(',');
				}
				recIni.put("cam_PPG", "Length", camRecLength);
				recIni.put("cam_PPG", "TimeStampArray",
						camTimeStringBuilder.toString());
				recIni.put("cam_PPG", "AmpArray",
						camAmpStringBuilder.toString());

			}

			if (oxiRecLength != 0) {
				StringBuilder oxiTimeStringBuilder = new StringBuilder();
				StringBuilder oxiAmpStringBuilder = new StringBuilder();
				for (AT at : oxiPPGList) {
					oxiTimeStringBuilder.append(at.getTime());
					oxiTimeStringBuilder.append(',');
					oxiAmpStringBuilder.append(at.getAmp());
					oxiAmpStringBuilder.append(',');
				}
				recIni.put("oxi_PPG", "Length", oxiRecLength);
				recIni.put("oxi_PPG", "TimeStampArray",
						oxiTimeStringBuilder.toString());
				recIni.put("oxi_PPG", "AmpArray",
						oxiAmpStringBuilder.toString());
			}
			// if (proIni.get(nameString, "RecordFiles") ==
			// null) {
			// proIni.put(nameString, "RecordFiles",
			// recFileName);
			// } else {
			// Log.d("ini", "Got append");
			Section proSec = proIni.get(nameString);
			if (proSec.containsKey("RecordFiles")) {
				// Log.d("ini", "contains recordfiles");
				// List<String> recordFiles = proSec.getAll("RecordFiles");
				//
				// recordFiles.add(recFileName);
				//
				// proSec.putAll("RecordFiles", recordFiles);

				String recordFiles = proSec.get("RecordFiles");
				recordFiles = recordFiles + "," + recFileName;
				proSec.put("RecordFiles", recordFiles);

			} else {
				Log.d("ini", "no recordfiles");
				proIni.put(nameString, "RecordFiles", recFileName);
			}
			nameSet = proIni.keySet();
			try {
				proIni.store();
				recIni.store();
			} catch (Exception e) {
				ToastManager.show(getApplicationContext(),
						"Store ini file failed!");
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		reset();
		ToastManager.show(getApplicationContext(),
				getResources().getString(R.string.save_success) + ":"
						+ pathString + recFileName);

	}

	private void reset() {
		if (oxiRecordEnabled) {
			if (oxiThread != null) {
				oxiThread.interrupt();
			}

		}

		Calendar calendar = Calendar.getInstance();
		cachedYear = calendar.get(Calendar.YEAR);
		cachedMonthOfYear = calendar.get(Calendar.MONTH);
		cachedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		cachedHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		cachedMinute = calendar.get(Calendar.MINUTE);
		dateButton.setText(DateUtils.getStringWithStyle3(cachedYear,
				cachedMonthOfYear, cachedDayOfMonth));
		timeButton.setText(DateUtils.getStringWithStyle4(cachedHourOfDay,
				cachedMinute));
		systolicEdit.setText("");
		diastolicEdit.setText("");
		heartEdit.setText("");

		noteEdit.setText("");
		nameEdit.setText("");
		nameEdit.setSelected(true);
		ageEdit.setText("");
		phoneEdit.setText("");

		camPPGList.clear();
		oxiPPGList.clear();
		cam_start_time = -1;
		oxi_start_time = -1;
		camRecordFinished = false;
		oxiRecordFinished = false;
		// camRecState.setText("");
		camRecFinishState.setText("");
		// oxiRecState.setText("");
		oxiRecFinishState.setText("");
		genderSpinner.setSelection(0);
		cdHistoryRadioGroup.check(cdHistoryNoRadioButton.getId());
		startCamRecButton.setEnabled(true);
		oxiThread = null;
		this.stop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == NOTE_REQUEST) {
			note = data.getStringExtra("noteRes").toString();
			noteEdit.setText(note);
			return;
		} else if (resultCode == Activity.RESULT_OK
				&& requestCode == PICKFILE_RESULT_CODE) {
			if (resultCode == RESULT_OK) {
				String filePath = data.getData().getPath();
				File file = new File(filePath);
				Intent intent = new Intent(Intent.ACTION_EDIT);
				Uri uri = Uri.fromFile(file);
				intent.setDataAndType(uri, "text/plain");
				startActivity(intent);
				return;
			}
		}
	}

	private void startCamWaveDraw() {
		float xLen = camWaveSurfaceView.getWidth();
		float yLen = camWaveSurfaceView.getHeight();
		Paint p = new Paint(); // 笔触
		p.setAntiAlias(true); // 反锯齿
		p.setStrokeWidth(2.5f);
		p.setColor(Color.BLUE);
		camWaveDraw.start(camWaveSurfaceView, p, xLen, yLen);
	}

	private void startOxiWaveDraw() {
		float xLen = oxiWaveSurfaceView.getWidth();
		float yLen = oxiWaveSurfaceView.getHeight();
		Paint p = new Paint(); // 笔触
		p.setAntiAlias(true); // 反锯齿
		p.setStrokeWidth(2.5f);
		p.setColor(Color.BLUE);
		oxiWaveDraw.start(oxiWaveSurfaceView, p, xLen, yLen);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.b, menu);
		return true;
	}

	private class GetWeatherTask extends AsyncTask<Void, Integer, String> {
		@Override
		protected String doInBackground(Void... voids) {
			cnWeather.init();
			String res = cnWeather.getWeatherString();
			return res;
		}

		@Override
		protected void onPostExecute(String result) {
			cachedWeatherString = result;
			weatherText.setText(cachedWeatherString);
		}

	}

	public class MyCameraSurfaceView extends SurfaceView implements
			SurfaceHolder.Callback {

		private SurfaceHolder mHolder;
		private Camera mCamera;

		public MyCameraSurfaceView(Context context, Camera camera) {
			super(context);
			mCamera = camera;
			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format,
				int weight, int height) {
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				// ignore: tried to stop a non-existent preview
			}

			// make any resize, rotate or reformatting changes here

			// start preview with new settings
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();

			} catch (Exception e) {
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw
			// the preview.

			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (IOException e) {
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		}
	}
}
