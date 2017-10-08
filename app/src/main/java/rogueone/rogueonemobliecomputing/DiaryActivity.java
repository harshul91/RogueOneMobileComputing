package rogueone.rogueonemobliecomputing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rogueone.rogueonemobliecomputing.Interfaces.APIClient;
import rogueone.rogueonemobliecomputing.Interfaces.ServiceGenerator;
import rogueone.rogueonemobliecomputing.Models.LocationEntry;

public class DiaryActivity extends OptionsMenuActivity {
    ProgressDialog progressDialog;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    @BindView(R.id.my_entries)
    RecyclerView _recyView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
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
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(DiaryActivity.this,
                R.style.AppTheme_Dark_Dialog);
        List<LocationEntry> locations = (List<LocationEntry>)getIntent().getSerializableExtra("entries");
        mLayoutManager = new LinearLayoutManager(this);
        _recyView.setLayoutManager(mLayoutManager);
        mAdapter = new DiaryAdapter(locations,client,getApplicationContext());
        _recyView.setAdapter(mAdapter);
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}
