package rogueone.rogueonemobliecomputing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rogueone.rogueonemobliecomputing.Interfaces.APIClient;
import rogueone.rogueonemobliecomputing.Interfaces.ServiceGenerator;
import rogueone.rogueonemobliecomputing.Models.LocationEntry;
import rogueone.rogueonemobliecomputing.Models.Trip;

public class ProfileActivity extends OptionsMenuActivity {
    String Username;
    @BindView(R.id.user_diary)
    ImageView _diary;
    @BindView(R.id.user_trips)
    ImageView _trips;
    @BindView(R.id.emergency_coordinates)
    TextView _emergency;
    ProgressDialog progressDialog;
    public View.OnClickListener diaryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startDialog();
            final Call<List<LocationEntry>> diary = client.getDiaryEntries(Username);
            diary.enqueue(new Callback<List<LocationEntry>>() {
                @Override
                public void onResponse(Call<List<LocationEntry>> call, Response<List<LocationEntry>> response) {
                    closeDialog();
                    if(response.isSuccessful()){
                        List<LocationEntry> entries = response.body();
                        if(entries==null){
                            showErrorToast(new Throwable("The User is in Incognito Mode or doesnt have any entries"));
                            return;
                        }
                        Intent diaryEntries = new Intent(getApplicationContext(),DiaryActivity.class);
                        diaryEntries.putExtra("entries",(Serializable) entries);
                        diaryEntries.putExtra("token", token);
                        startActivity(diaryEntries);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }else{
                        try {
                            showErrorToast(new Throwable(response.errorBody().string()));
                        } catch (IOException e) {
                            showErrorToast(new Throwable("The User is in Incognito Mode"));
                        }
                    }
                }
                @Override
                public void onFailure(Call<List<LocationEntry>> call, Throwable t) {
                    closeDialog();
                    showErrorToast(t);
                }
            });
        }
    };
    public View.OnClickListener tripListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startDialog();
            final Call<List<Trip>> trips = client.getTrips(Username);
            trips.enqueue(new Callback<List<Trip>>() {
                @Override
                public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                    closeDialog();
                    if(response.isSuccessful()){
                        List<Trip> entries = response.body();
                        if(entries==null){
                            showErrorToast(new Throwable("The User is in Incognito Mode or doesnt have any entries"));
                            return;
                        }
                        Intent tripEntries = new Intent(getApplicationContext(),TripActivity.class);
                        tripEntries.putExtra("entries",(Serializable)entries);
                        tripEntries.putExtra("token", token);
                        startActivity(tripEntries);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }else{
                        try {
                            showErrorToast(new Throwable(response.errorBody().string()));
                        } catch (IOException e) {
                            showErrorToast(new Throwable("The User is in Incognito Mode"));
                        }
                    }
                }
                @Override
                public void onFailure(Call<List<Trip>> call, Throwable t) {
                    closeDialog();
                    showErrorToast(t);
                }
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        try{
            token = getIntent().getStringExtra("token");
        }catch(Exception e){
            Log.e(android.support.compat.BuildConfig.APPLICATION_ID,e.getMessage());
        }
        if(token==null){
            try{
                token = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PACKAGE_NAME+"token","");
            }catch(Exception e){
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
            if(token.equals("")||token==null){
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }

        }
        client = ServiceGenerator
                .createService(APIClient.class, token, getBaseContext());
        progressDialog = new ProgressDialog(ProfileActivity.this,
                R.style.AppTheme_Dark_Dialog);
        token = getIntent().getStringExtra("token");
        Username = getIntent().getStringExtra("userEmail");
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbarMain);
        setTitle("LODI");
        setSupportActionBar(toolbar);
        TextView mTitle = (TextView) toolbar.getChildAt(0);
        mTitle.setOnClickListener(homeListener);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Lobster-Regular.ttf");
        mTitle.setTypeface(tf);
        _diary.setOnClickListener(diaryListener);
        _trips.setOnClickListener(tripListener);
        setEmergencyCheckIn();
    }

    private void setEmergencyCheckIn() {
        Call<LocationEntry> entry = client.getEmergencyCheckIn(Username);
        entry.enqueue(new Callback<LocationEntry>() {
            @Override
            public void onResponse(Call<LocationEntry> call, Response<LocationEntry> response) {
                if(response.isSuccessful()){
                    LocationEntry emergency = response.body();
                    if(emergency==null){return;}
                    _emergency.setText("Latitude: "+emergency.Latitude+" Longitude: "+emergency.Longitude);
                }else{
                    try {
                        showErrorToast(new Throwable(response.errorBody().string()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<LocationEntry> call, Throwable t) {
                showErrorToast(t);
            }
        });
    }

    @Override
    public void startDialog(){
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }
    @Override
    public void closeDialog(){
        progressDialog.dismiss();
    }
    @Override
    public void showErrorToast(Throwable t){
        Toast.makeText(getApplicationContext(),t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
    }
}
