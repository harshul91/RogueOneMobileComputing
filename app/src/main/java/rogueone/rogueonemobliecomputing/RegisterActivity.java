package rogueone.rogueonemobliecomputing;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rogueone.rogueonemobliecomputing.Interfaces.RogueOneInterface;
import rogueone.rogueonemobliecomputing.Interfaces.ServiceGenerator;
import rogueone.rogueonemobliecomputing.Models.Register;

public class RegisterActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://rogueonemobilecomputing.azurewebsites.net/";
    @BindView(R.id.email)
    EditText _email;
    @BindView(R.id.password)
    EditText _password;
    @BindView(R.id.confirm_password)
    EditText _confirm_password;
    @BindView(R.id.save)
    Button _save;
    @BindView(R.id.login)
    TextView _login;
    public OnClickListener saveData = new OnClickListener() {
        @Override
        public void onClick(View v) {
            final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating an Account...");
            progressDialog.show();
            Register register =new Register(_email.getText().toString(),_password.getText().toString(),_confirm_password.getText().toString());
            RogueOneInterface registrationService = ServiceGenerator.createService(RogueOneInterface.class,getApplicationContext());
            Call reg = registrationService.registerUser(register);
            String body = null;
            reg.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    progressDialog.dismiss();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    finish();
                    startActivity(new Intent(getBaseContext(),LoginActivity.class));
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    public OnClickListener loginListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        _save.setOnClickListener(saveData);
        _login.setOnClickListener(loginListener);
    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }
}
