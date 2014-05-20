package com.spout.phonegap.plugins.baidulocation;


import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class BaiduLocation extends CordovaPlugin {

	private static final String STOP_ACTION = "stop";
	private static final String GET_ACTION = "getCurrentPosition";
	public LocationClient locationClient = null;
	public JSONObject jsonObj = new JSONObject();
	public boolean result = false;
	public CallbackContext callbackContext;

	public BDLocationListener myListener;

	private static final Map<Integer, String> ERROR_MESSAGE_MAP = new HashMap<Integer, String>();

	private static final String DEFAULT_ERROR_MESSAGE = "服务端定位失败";

	static {
		ERROR_MESSAGE_MAP.put(61, "GPS定位结果");
		ERROR_MESSAGE_MAP.put(62, "扫描整合定位依据失败。此时定位结果无效");
		ERROR_MESSAGE_MAP.put(63, "网络异常，没有成功向服务器发起请求。此时定位结果无效");
		ERROR_MESSAGE_MAP.put(65, "定位缓存的结果");
		ERROR_MESSAGE_MAP.put(66, "离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果");
		ERROR_MESSAGE_MAP.put(67, "离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果");
		ERROR_MESSAGE_MAP.put(68, "网络连接失败时，查找本地离线定位时对应的返回结果。");
		ERROR_MESSAGE_MAP.put(161, "表示网络定位结果");
	};

	public String getErrorMessage(int locationType) {
		String result = ERROR_MESSAGE_MAP.get(locationType);
		if (result == null) {
			result = DEFAULT_ERROR_MESSAGE;
		}
		return result;
	}

	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) {
		setCallbackContext(callbackContext);
		if (GET_ACTION.equals(action)) {
			cordova.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					locationClient = new LocationClient(cordova.getActivity());
					locationClient.setAK("BfkPvjDGHC0ATZhIr6wxnHh9");//设置百度的ak
					myListener = new MyLocationListener();
					locationClient.registerLocationListener(myListener);
					LocationClientOption option = new LocationClientOption();
					option.setOpenGps(true);
					option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
					option.setProdName("BaiduLoc");
					option.disableCache(true);// 禁止启用缓存定位
					locationClient.setLocOption(option);

					locationClient.start();
					locationClient.requestLocation();

				}

			});
			return true;
		} else if (STOP_ACTION.equals(action)) {
			locationClient.stop();
			callbackContext.success(200);
			return true;
		} else {
			callbackContext
					.error(PluginResult.Status.INVALID_ACTION.toString());
		}

		while (result == false) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			try {
				JSONObject coords = new JSONObject();
				coords.put("latitude", location.getLatitude());
				coords.put("longitude", location.getLongitude());
				coords.put("radius", location.getRadius());

				jsonObj.put("coords", coords);

				int locationType = location.getLocType();

				jsonObj.put("locationType", locationType);
				jsonObj.put("code", locationType);
				jsonObj.put("message", getErrorMessage(locationType));

				switch (location.getLocType()) {

				case BDLocation.TypeGpsLocation:
					coords.put("speed", location.getSpeed());
					coords.put("altitude", location.getAltitude());
					jsonObj.put("SatelliteNumber",
							location.getSatelliteNumber());
					break;

				case BDLocation.TypeNetWorkLocation:
					jsonObj.put("addr", location.getAddrStr());
					break;
				}

				Log.d("BaiduLocationPlugin", "run: " + jsonObj.toString());
				callbackContext.success(jsonObj);
				result = true;
			} catch (JSONException e) {
				callbackContext.error(e.getMessage());
				result = true;
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onDestroy() {
		if (locationClient != null && locationClient.isStarted()) {
			locationClient.stop();
			locationClient = null;
		}
		super.onDestroy();
	}

	private void logMsg(String s) {
		System.out.println(s);
	}

	public CallbackContext getCallbackContext() {
		return callbackContext;
	}

	public void setCallbackContext(CallbackContext callbackContext) {
		this.callbackContext = callbackContext;
	}
}