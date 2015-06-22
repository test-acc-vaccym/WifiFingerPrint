

package org.es25.wififingerprint;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.FileNotFoundException;
import java.util.Set;
import org.es25.wififingerprint.struct1.LocationMap;
import org.es25.wififingerprint.struct1.Station;


public class WifiFingerPrintLearning extends IntentService {

	private static WifiManager wifimgr;
	private static final String LOC_MAP_FILE = "RssiLearningMap.csv";
	private static final String TAG = "WifiFingerPrintLearning";
	private static int count = 0;

	private LocationMap locationMap = null;


	/**
	 * Construct that bastard.
	 */
	public WifiFingerPrintLearning() {
		super("org.es25.wififingerprint.WifiFingerPrintLearning");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			locationMap = RssiUtils.loadMap(openFileInput(LOC_MAP_FILE));
		} catch (FileNotFoundException ex) {
			locationMap = new LocationMap();
		}

		System.out.println();
		System.out.println(
				"II CURRENTLY KNOWN LOCATIONS\n====================================================================");
		System.out.println(locationMap.getNames());
		System.out.println();

		Log.d(TAG, "========START WIFI-ING============");
		wifimgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifimgr.setWifiEnabled(true);

		////  HERE WE'LL GET A FILTERED AND NORMALISED SCAN.
		////  NOTE: her we will need a location name from the gui.
		////        ==> till we have this name, it will be simulated.
		/////////////////////////////////////////////////////////////////////
		Log.d(TAG, "========START WIFI-SCAN============");
		wifimgr.startScan();
		Set<Station> stations = RssiUtils.filterScan(wifimgr.getScanResults());

		// simulated location name: (which we want from user input for a bunch of scans)
		String name = String.format("Location_#%d", count);
		count++;
		// end name simulation

		System.out.println();
		System.out.println(
				"II SCANNED STATIONS: (for " + name + "):\n====================================================================");
		System.out.println(stations);
		System.out.println();

		// writing new scan and location to the location map.
		locationMap.add(name, stations);

		Log.d(TAG, "========START Writing============");
		try {
			RssiUtils.storeMap(
					locationMap,
					openFileOutput(LOC_MAP_FILE, Context.MODE_PRIVATE));
		} catch (FileNotFoundException ex) {
			System.out.println("ERROR on LocMap writing !! - " + ex.getMessage());
		}
	}


	@Override
	public void onDestroy() {
		// TODO write the location map bacK to file!!!
	}
}
