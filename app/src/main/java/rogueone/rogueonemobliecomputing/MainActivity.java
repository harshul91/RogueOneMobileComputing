package rogueone.rogueonemobliecomputing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.compat.BuildConfig;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rogueone.rogueonemobliecomputing.Interfaces.APIClient;
import rogueone.rogueonemobliecomputing.Interfaces.ServiceGenerator;
import rogueone.rogueonemobliecomputing.Models.LocationEntry;
import rogueone.rogueonemobliecomputing.Models.Trip;

public class MainActivity extends OptionsMenuActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.diary)
    ImageView _diary;
    @BindView(R.id.trips)
    ImageView _trips;
    @BindView(R.id.new_trip)
    Button _newTrip;
    ProgressDialog progressDialog;
    String email_id;
    public OnClickListener diaryListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startDialog();
            final Call<List<LocationEntry>> diary = client.getDiaryEntries();
            diary.enqueue(new Callback<List<LocationEntry>>() {
                @Override
                public void onResponse(Call<List<LocationEntry>> call, Response<List<LocationEntry>> response) {
                    closeDialog();
                    List<LocationEntry> entries = response.body();
                    Intent diaryEntries = new Intent(getApplicationContext(),DiaryActivity.class);
                    diaryEntries.putExtra("entries",(Serializable) entries);
                    diaryEntries.putExtra("token", token);
                    startActivity(diaryEntries);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
                @Override
                public void onFailure(Call<List<LocationEntry>> call, Throwable t) {
                    closeDialog();
                    showErrorToast(t);
                }
            });

        }
    };
    public OnClickListener tripsListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startDialog();
            final Call<List<Trip>> trips = client.getTrips();
            trips.enqueue(new Callback<List<Trip>>() {
                @Override
                public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                    closeDialog();
                    if(response.isSuccessful()){
                        List<Trip> entries = response.body();
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
                            e.printStackTrace();
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

    public OnClickListener newTripListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent create = new Intent(getApplicationContext(),CreateTripActivity.class);
            create.putExtra("token", token);
            startActivity(create);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    };
    private void incognitoMode() {
        Call<ResponseBody> call = client.goIncognito();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"You are now incognito",Toast.LENGTH_LONG).show();
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
    private void setIncognito(final NavigationView nav) {
        Call<ResponseBody> call = client.getIncognito();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    nav.setCheckedItem(R.id.nav_incognito);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
            token = getIntent().getStringExtra("token");
        }catch(Exception e){
            Log.e(BuildConfig.APPLICATION_ID,e.getMessage());
        }
        if(token==null){
            try{
                token = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PACKAGE_NAME+"token","");
                email_id = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PACKAGE_NAME+"email_id","");
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
        progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        token = getIntent().getStringExtra("token");
        email_id = getIntent().getStringExtra("email");
        ButterKnife.bind(this);
        _diary.setOnClickListener(diaryListener);
        _trips.setOnClickListener(tripsListener);
        _newTrip.setOnClickListener(newTripListener);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("LODI");
        setSupportActionBar(toolbar);
        TextView mTitle = (TextView) toolbar.getChildAt(0);
        mTitle.setOnClickListener(homeListener);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Lobster-Regular.ttf");
        mTitle.setTypeface(tf);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCheckIn();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        TextView email = (TextView)header.findViewById(R.id.user_email);
        email.setText(email_id);
        setIncognito(navigationView);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_incognito) {
            incognitoMode();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
