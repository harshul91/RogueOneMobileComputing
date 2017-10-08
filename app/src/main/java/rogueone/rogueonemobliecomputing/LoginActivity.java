package rogueone.rogueonemobliecomputing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rogueone.rogueonemobliecomputing.Interfaces.RogueOneInterface;
import rogueone.rogueonemobliecomputing.Interfaces.ServiceGenerator;
import rogueone.rogueonemobliecomputing.Models.Token;
import rogueone.rogueonemobliecomputing.Models.User;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.email)
    EditText _email;
    @BindView(R.id.password)
    EditText _password;
    @BindView(R.id.login)
    Button _login;
    @BindView(R.id.register)
    TextView _register;
    ProgressDialog progressDialog;
    public OnClickListener loginListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
            final User user = new User(_email.getText().toString(),_password.getText().toString());
            RogueOneInterface tokenService = ServiceGenerator.createService(RogueOneInterface.class,getApplicationContext());
            Call<Token> call = tokenService.getToken(user.getUsername(),user.getPassword(),"password");
            call.enqueue(new Callback<Token>() {
                @Override
                public void onResponse(Call<Token> call, Response<Token> response) {
                    if(response.isSuccessful()){
                        Token token = response.body();
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        sp.edit().putString(Constants.PACKAGE_NAME+"token",token.getAccessToken()).apply();
                        sp.edit().putString(Constants.PACKAGE_NAME+"email_id",user.getUsername()).apply();
                        Intent intent = new Intent(getBaseContext(),MainActivity.class);
                        intent.putExtra("token",token.getAccessToken());
                        intent.putExtra("email",user.getUsername());
                        startActivity(intent);
                        finish();
                    }else{
                        progressDialog.dismiss();
                        try {
                            Toast.makeText(getApplicationContext(),response.errorBody().string(),Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(Call<Token> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),t.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });


        }
    };
    public OnClickListener registerListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        _login.setOnClickListener(loginListener);
        _register.setOnClickListener(registerListener);
    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }
}
