package rogueone.rogueonemobliecomputing;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rogueone.rogueonemobliecomputing.Interfaces.APIClient;
import rogueone.rogueonemobliecomputing.Models.LocationEntry;
import rogueone.rogueonemobliecomputing.Models.MyLocation;

public abstract class OptionsMenuActivity extends AppCompatActivity implements AddressResultReceiver.Receiver,
ActivityCompat.OnRequestPermissionsResultCallback,SensorEventListener{
    protected static APIClient client;
    protected static String token;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    protected Location mLastKnownLocation;
    private AddressResultReceiver mResultReceiver;
    private FusedLocationProviderClient mFusedLocationClient;
    protected View.OnClickListener homeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("token", token);
            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mSensorManager = (SensorManager) getSystemService(getApplicationContext().SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mSensor != null){
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            return logout();
        }
        if (id == R.id.connect) {
            return connectWithUsers();
        }
        if (id == R.id.friends) {
            return showFriends();
        }
        if (id == R.id.pending) {
            return showPending();
        }

        return super.onOptionsItemSelected(item);
    }

    protected abstract void startDialog();

    protected abstract void closeDialog();

    protected abstract void showErrorToast(Throwable t);

    public boolean logout() {
        startDialog();
        Call<ResponseBody> call = client.logout();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    closeDialog();
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    sp.edit().clear().commit();
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                } else {
                    try {
                        showErrorToast(new Throwable(response.errorBody().string()));
                        closeDialog();
                    } catch (IOException e) {
                        e.printStackTrace();
                        closeDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showErrorToast(t);
                closeDialog();
            }
        });
        return true;
    }

    public boolean showFriends() {
        startDialog();
        Call<List<String>> call = client.getFriendList();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                closeDialog();
                Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
                intent.putExtra("friendList", (Serializable) response.body());
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                closeDialog();
                showErrorToast(t);
            }
        });
        return true;
    }

    public boolean showPending() {
        startDialog();
        Call<List<String>> call = client.getPendingRequests();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                closeDialog();
                Intent intent = new Intent(getApplicationContext(), PendingActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("requestList", (Serializable) response.body());
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                closeDialog();
                showErrorToast(t);
            }
        });
        return true;
    }

    public boolean connectWithUsers() {
        startDialog();
        Call<List<String>> call = client.getAppUsers();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                closeDialog();
                Intent intent = new Intent(getApplicationContext(), ConnectActivity.class);
                List<String> users = response.body();
                intent.putExtra("userList", (Serializable) users);
                intent.putExtra("token", token);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                closeDialog();
                showErrorToast(t);
            }
        });
        return true;
    }

    protected void startIntentService() {
        mResultReceiver = new AddressResultReceiver(new Handler(),this);
        mResultReceiver.setReceiver(this);
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastKnownLocation);
        startService(intent);
    }

    public boolean createCheckIn() {
        startDialog();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        getLocation();
        return true;
    }
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        // TODO Auto-generated method stub
        final String address = resultData.getString(Constants.RESULT_DATA_KEY);
        Call<List<String>> trips = client.getTripNames();
        trips.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    closeDialog();
                    Intent intent = new Intent(getApplicationContext(), LocationEntryActivity.class);
                    List<String> trips = response.body();
                    intent.putExtra("Address",address);
                    MyLocation loc = new MyLocation(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
                    intent.putExtra("location",(Serializable)loc);
                    intent.putExtra("tripList", (Serializable) trips);
                    intent.putExtra("token", token);
                    startActivity(intent);
                } else {
                    closeDialog();
                    try {
                        showErrorToast(new Throwable(response.errorBody().string()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                closeDialog();
                showErrorToast(t);
            }
        });
    }
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},Constants.LOCATION_PERMISSION_CODE);
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastKnownLocation = location;

                        // In some rare cases the location returned can be null
                        if (mLastKnownLocation == null) {
                            return;
                        }

                        if (!Geocoder.isPresent()) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.no_geocoder_available,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        startIntentService();
                    }
                });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if(requestCode==Constants.LOCATION_PERMISSION_CODE){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / 9.8f;
        float gY = y / 9.8f;
        float gZ = z / 9.8f;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);
        if(gForce > 5){
            Toast.makeText(this,"crash detected sending an emergency check-in",Toast.LENGTH_LONG).show();
            sendEmergencyCheckIn();
        }
    }

    private void sendEmergencyCheckIn() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},Constants.LOCATION_PERMISSION_CODE);
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastKnownLocation = location;

                        // In some rare cases the location returned can be null
                        if (mLastKnownLocation == null) {
                            return;
                        }

                        if (!Geocoder.isPresent()) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.no_geocoder_available,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        LocationEntry entry = new LocationEntry();
                        entry.Comments = "Emergency Check-In";
                        entry.Latitude = mLastKnownLocation.getLatitude();
                        entry.Longitude = mLastKnownLocation.getLongitude();
                        entry.Address = "Emergency Check-In";
                        entry.Description = "Emergency Check-In";
                        Call<ResponseBody> call = client.sendEmergencyCheckin(entry);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if(response.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),"Emergency Check-In saved",Toast.LENGTH_LONG).show();
                                }else{
                                    try {
                                        showErrorToast(new Throwable(response.errorBody().string()));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                showErrorToast(t);
                            }
                        });
                    }
                });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e("Error",paramThrowable.getStackTrace().toString());
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
    }
}
