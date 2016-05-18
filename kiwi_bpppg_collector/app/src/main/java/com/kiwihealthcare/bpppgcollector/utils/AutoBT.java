package com.kiwihealthcare.bpppgcollector.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kiwihealthcare.bpppgcollector.BPActivity;
import com.kiwihealthcare.bpppgcollector.R;

public class AutoBT {
	String TAG = "BTManager";
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP服务UUID号
	boolean needCloseBT = false;
	// 要被调用的组件
	public Queue<Integer> ampQueue = new LinkedList<Integer>();
	public long timeStamp = 0;
	public long timeAdd = 50;
	// 界面组件
	BPActivity main;
	// 用于蓝牙
	BluetoothDevice _device = null; // 蓝牙设备
	BluetoothSocket _socket = null; // 蓝牙通信socket
	private InputStream is; // 输入流，用来接收蓝牙数据
	public volatile boolean bRun = false;
	private int state = 0;
	private BluetoothAdapter _bluetooth;
	private static final int MSG_DEVICE_CLOSED = 0;
	private static final int MSG_DEVICE_CONNECTED = 1;
	// 用于弹出的dialog
	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private View dialogView;
	private Dialog scanDialog;
	private boolean inUse = false;
	private boolean needScan = true; // 当手动取消

	// private boolean closeAll = false;

	public AutoBT(BPActivity main, boolean inUse) {// 初始化蓝牙适配器
		this.inUse = inUse;
		if (!inUse)
			return;
		this.main = main;
		this._bluetooth = BluetoothAdapter.getDefaultAdapter(); // 获取本地蓝牙适配器，即蓝牙设备
		if (_bluetooth == null) {// 无法打开蓝牙
			Toast.makeText(main, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG)
					.show();
			return;
		}
		new Thread() {
			public void run() {
				if (_bluetooth.isEnabled() == false) {
					_bluetooth.enable();
					needCloseBT = true;
				} else {
					needCloseBT = false;
				}
			}

		}.start();
		setDialog();
		// autoConnect(); //这个放到resume中去了
		// 注册接收查找到设备action接收器
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		main.registerReceiver(mReceiver, filter);
		// 注册查找结束action接收器
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		main.registerReceiver(mReceiver, filter);
	}

	public boolean isRunning() {
		if (!inUse)
			return false;
		return bRun;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void resetData() {
		if (!inUse)
			return;
		ampQueue.clear();
	}

	public int getData(int[] amp, long[] time) {
		if (!inUse)
			return 0;
		if (ampQueue.isEmpty())
			return 0;
		int maxLen = Math.min(amp.length, time.length);
		int n = 0;
		for (int i = 0; i < maxLen; i++) {
			if (ampQueue.isEmpty()) {
				break;
			}
			amp[i] = ampQueue.poll();
			timeStamp += timeAdd;
			time[i] = timeStamp;
			n++;
		}
		return n;
	}

	public void pause() {
		needScan = false;
		btStartTimer.cancel();
	}

	public void resume() {
		if (!bRun && inUse) {
			needScan = true;
			btStartTimer.start();
		}
	}

	CountDownTimer btStartTimer = new CountDownTimer(1000, 1000) {

		@Override
		public void onFinish() {
			autoConnect();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub

		}
	};

	public void clear() {
		if (!inUse)
			return;
		// 关闭服务查找
		inUse = false;
		bRun = false;
		needScan = false;
		// 视情况关闭蓝牙
		if (_bluetooth != null) {
			_bluetooth.cancelDiscovery();
			if (needCloseBT) {
				_bluetooth.disable();
			}
		}
		// 注销action接收器
		main.unregisterReceiver(mReceiver);
	}

	// 生成dialog并设置按钮的响应
	private void setDialog() {
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(main,
				R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(main,
				R.layout.device_name);

		LayoutInflater factory = LayoutInflater.from(main);
		dialogView = factory.inflate(R.layout.auto_device_list, null);
		// 设置已配队设备列表
		ListView pairedListView = (ListView) dialogView
				.findViewById(R.id.auto_paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// 设置新查找设备列表
		ListView newDevicesListView = (ListView) dialogView
				.findViewById(R.id.auto_new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		Button cancelButton = (Button) dialogView
				.findViewById(R.id.auto_button_cancel);
		cancelButton.setOnClickListener(cancelListener);

		Button stopButton = (Button) dialogView
				.findViewById(R.id.auto_button_stop);
		stopButton.setOnClickListener(stopListener);

		AlertDialog.Builder builder = new AlertDialog.Builder(main);
		builder.setView(dialogView);
		scanDialog = builder.create();
	}

	// 选择设备响应函数
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// 准备连接设备，关闭服务查找
			_bluetooth.cancelDiscovery();

			// 得到mac地址
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			// 这里可以判断选的蓝牙设备是否正确
			readFromAddress(address);
			scanDialog.dismiss();
		}
	};
	private OnClickListener cancelListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			needScan = true;
			scanDialog.dismiss();
			autoConnect();
		}
	};
	private OnClickListener stopListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			clear(); // 全部关闭
			scanDialog.dismiss();
		}
	};
	// 查找到设备和搜索完成action监听器
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			// Log.i("receive", "" + action);
			// 查找到设备action
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				needScan = false;
				// Log.i(TAG, "action found");
				// 得到蓝牙设备
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// 如果是已配对的则略过，已得到显示，其余的在添加到列表中进行显示
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				} else { // 添加到已配对设备列表
					mPairedDevicesArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}
				scanDialog.show();
				// 搜索完成action
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				if (needScan) {
					// Log.i("action discovery finish", "hello");
					autoConnect();
				}
			}
			// Log.i("receive2:" + action, "needScan:" + needScan);
		}
	};

	private void readFromAddress(String address) {
		// 得到蓝牙设备句柄
		_device = _bluetooth.getRemoteDevice(address);
		try {
			_socket = _device.createRfcommSocketToServiceRecord(UUID
					.fromString(MY_UUID));
		} catch (IOException e) {
			Toast.makeText(main, "连接失败1！", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		// 用服务号得到socket
		try {
			_socket = _device.createRfcommSocketToServiceRecord(UUID
					.fromString(MY_UUID));
		} catch (IOException e) {
			Toast.makeText(main, "连接失败2！", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		// 连接socket
		// Button btn = (Button) findViewById(R.id.Button03);
		try {
			_socket.connect();
			Toast.makeText(main, "连接" + _device.getName() + "成功！",
					Toast.LENGTH_SHORT).show();
			// btn.setText("断开");
		} catch (IOException e) {
			try {
				Toast.makeText(main, "连接失败3！", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				_socket.close();
				_socket = null;
				needScan = true;
				autoConnect();
			} catch (IOException ee) {
				Toast.makeText(main, "连接失败4！", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			return;
		}
		// 打开接收线程
		try {
			is = _socket.getInputStream(); // 得到蓝牙数据输入流
		} catch (IOException e) {
			Toast.makeText(main, "接收数据失败！", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			return;
		}
		if (bRun == false) {
			mHandler.obtainMessage(MSG_DEVICE_CONNECTED).sendToTarget();
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DEVICE_CLOSED:
				try {
					is.close();
					_socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				_socket = null;
				Toast.makeText(main, "设备断开", Toast.LENGTH_SHORT).show();
				autoConnect();
				main.btDisconnected();
				break;
			case MSG_DEVICE_CONNECTED:

				(new ReadThread()).start();
				main.btConnected();
				break;
			}
		}
	};

	class ReadThread extends Thread {

		public void run() {
			bRun = true;

			byte[] buffer = new byte[1024];
			try {
				while (is.available() == 0) {

				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (bRun && inUse) { // 没有超时就一直循环
				try {
					int haveChecked = 0;
					int num = 0;
					while (num == 0) {
						sleep(100);
						num = is.available();
						haveChecked++;
						if (haveChecked > 20) { // 判断超时
							bRun = false;
							break;
						}
						// Log.i("check", "" + haveChecked);
					}
					if (bRun) {
						num = is.read(buffer);
						for (int i = 0; i < num; i++) {
							changeState(buffer[i]);
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			bRun = false;
			// Log.i("open wait thread", "change bRun " + bRun);
			needScan = true;
			mHandler.obtainMessage(MSG_DEVICE_CLOSED).sendToTarget();
			return;
		}

		private void changeState(byte data) {
			if (state == 0) {
				if (data != (byte) 0x80)
					return;
				else
					state = 1;
			} else if (state == 1) {
				ampQueue.add((0xff & data));
				// Log.i("wave", "" + (0xff & data));
				state = 2;
			} else if (state == 2) {
				state = 3;
			} else if (state == 3) {
				state = 4;
			} else if (state == 4) {
				state = 0;
			}
		}
	}

	private void autoConnect() {
		if (!needScan || !inUse)
			return; // 如果有
		// Log.i(TAG, "enter auto connect");
		Toast.makeText(main, "auto Connect", Toast.LENGTH_SHORT).show();
		if (_bluetooth.isEnabled()) {
			if (_bluetooth.isDiscovering()) {
				_bluetooth.cancelDiscovery();
			}
			// 清空两个adapter, 并重新开始
			mPairedDevicesArrayAdapter.clear();
			mNewDevicesArrayAdapter.clear();
			_bluetooth.startDiscovery();
		} else {
			btStartTimer.start();
		}

	}
}