package rogueone.rogueonemobliecomputing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rogueone.rogueonemobliecomputing.Interfaces.APIClient;
import rogueone.rogueonemobliecomputing.Interfaces.ServiceGenerator;
import rogueone.rogueonemobliecomputing.Models.Trip;
import rogueone.rogueonemobliecomputing.Models.TripMate;

public class CreateTripActivity extends OptionsMenuActivity {
    ProgressDialog progressDialog;
    @BindView(R.id.trip_name)
    EditText _trip_name;
    @BindView(R.id.destination)
    EditText _destination;
    @BindView(R.id.duration)
    EditText _duration;
    @BindView(R.id.trip_mates)
    ChipsInput _trip_mates;
    @BindView(R.id.trip_description)
    EditText _description;
    @BindView(R.id.name)
    EditText _name;
    @BindView(R.id.phone_number)
    EditText _phone;
    @BindView(R.id.add_trip_mate)
    Button _add_trip_mate;
    @BindView(R.id.create_trip)
    Button _create_trip;
    private View.OnClickListener tripMateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = _name.getText().toString();
            String phoneNumber = _phone.getText().toString();
            if(name!=""&&phoneNumber!=""&&phoneNumber!=null&&name!=null) {
                _trip_mates.addChip(new Chip(name,phoneNumber));
                _name.setText("");
                _phone.setText("");
            }else{
                showErrorToast(new Throwable("Both name and phone are required"));
            }
        }
    };
    private View.OnClickListener createTripListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startDialog();
            Trip trip = getTripData();
            Call<ResponseBody> call = client.createTrip(trip);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    closeDialog();
                    if(response.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Trip Created Successfully",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("token",token);
                        startActivity(intent);
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
                    closeDialog();
                    showErrorToast(t);
                }
            });
        }
    };

    private Trip getTripData() {
        Trip trip = new Trip();
        trip.Description = _description.getText().toString();
        trip.Destination = _destination.getText().toString();
        trip.PlannedDuration = _duration.getText().toString();
        trip.TripName = _trip_name.getText().toString();
        trip.TripMates = new ArrayList<TripMate>();
        for(Chip chip: (List<Chip>)_trip_mates.getSelectedChipList()){
            trip.TripMates.add(new TripMate(chip.getLabel(),chip.getInfo()));
        }
        return trip;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);
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
                .createService(APIClient.class,token,getBaseContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.appbarMain);
        setTitle("LODI");
        setSupportActionBar(toolbar);
        TextView mTitle = (TextView) toolbar.getChildAt(0);
        mTitle.setOnClickListener(homeListener);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Lobster-Regular.ttf");
        mTitle.setTypeface(tf);
        progressDialog = new ProgressDialog(CreateTripActivity.this,
                R.style.AppTheme_Dark_Dialog);
        ButterKnife.bind(this);
        _add_trip_mate.setOnClickListener(tripMateListener);
        _create_trip.setOnClickListener(createTripListener);
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
