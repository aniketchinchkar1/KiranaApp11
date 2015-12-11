package com.example.learna_ndroid.kiranaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asynctask.AsyncManager;
import com.classes.BroadCastReciever;
import com.databasehelper.DatabaseHelper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OTPValidationActivity extends Activity implements View.OnClickListener{

    private static EditText mEditOTP;
    private TextView TextTimer;
    private static Button mBtnSubmitOTP,mBtnResendOTP;

    LinearLayout linearProgressbarcontain;
    public static ProgressBar AOTPprogressBar;
    public static String strOtp;

    String strOTP;
    private final BroadcastReceiver mybroadcast = new BroadCastReciever();


    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mybroadcast, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mybroadcast);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpvalidation);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));

        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
                title.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mEditOTP=(EditText)findViewById(R.id.edt_otp_text);
        TextTimer=(TextView)findViewById(R.id.timer);
        mBtnSubmitOTP=(Button)findViewById(R.id.btn_submit);
        mBtnResendOTP=(Button)findViewById(R.id.btn_resendotp);

        linearProgressbarcontain=(LinearLayout)findViewById(R.id.linearProgressbarcontain);
        AOTPprogressBar=(ProgressBar)findViewById(R.id.AOTPprogressBar);
        mEditOTP.setEnabled(true);
        AOTPprogressBar.setVisibility(View.VISIBLE);

      /*  SpannableString content = new SpannableString("Resend OTP");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        TextTimer.setText(content);*/

        StartTimer();

        /*new CountDownTimer(30000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {
                TextTimer.setText(""+String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                TextTimer.setText("done!");
            }
        }.start();*/

        mBtnSubmitOTP.setOnClickListener(this);
        mBtnResendOTP.setOnClickListener(this);
        mEditOTP.setOnClickListener(this);

     //   mEditOTP.setText(ActivationActivity.strOtp);
     //   mBtnSubmitOTP.performClick();
      //  setOtpToTextview("8796703978","otp:876543");

    }

   public static void  setOtpToTextview(String senderNum, String message)
   {
       try
       {
           if(senderNum.equalsIgnoreCase("8796703978"))
           {
               strOtp=extractDigits(message);
               mEditOTP.setText(strOtp);
               mEditOTP.setEnabled(false);
               mEditOTP.setGravity(Gravity.CENTER);
               AOTPprogressBar.setVisibility(View.INVISIBLE);
               mBtnSubmitOTP.performClick();
           }
       }
       catch (Exception e)
       {
           e.printStackTrace();
       }
   }

    public  static String extractDigits(final String in)
    {
        final Pattern p = Pattern.compile("(\\d{6})");
        final Matcher m = p.matcher( in );
        if ( m.find() )
        {
            return m.group( 0 );
        }
        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_otpvalidation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }
        else if(id==R.id.home)
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_submit:
                strOTP=mEditOTP.getText().toString();
                if(strOTP==null || strOTP.equalsIgnoreCase(""))
                {
                    Toast.makeText(OTPValidationActivity.this,"Please Enter OTP.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    new AsynckValidateOtp().execute();
                }
                break;

            case R.id.btn_resendotp:
                mBtnSubmitOTP.setVisibility(View.VISIBLE);
                mBtnResendOTP.setVisibility(View.GONE);
                AOTPprogressBar.setVisibility(View.VISIBLE);
                StartTimer();
                break;

            case R.id.edt_otp_text:
                AOTPprogressBar.setVisibility(View.INVISIBLE);
                break;
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(OTPValidationActivity.this,ActivationActivity.class);
        startActivity(intent);
        finish();
    }

    public void StartTimer(){

        new CountDownTimer(60000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {
                TextTimer.setText(""+String.format("%d sec",

                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                TextTimer.setText("00 sec");
                AOTPprogressBar.setVisibility(View.INVISIBLE);
                mBtnSubmitOTP.setVisibility(View.GONE);
                mBtnResendOTP.setVisibility(View.VISIBLE);

            }
        }.start();

    }



    private class AsynckValidateOtp extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog=new ProgressDialog(OTPValidationActivity.this);
        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(OTPValidationActivity.this, "","");
            progressDialog.setContentView(R.layout.custom_progressbar);
        }
        @Override
        protected String doInBackground(String... params)
        {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://retailware.biz/websites/kirana/webservice/VerifyActivationCode.php");
                JSONObject json = new JSONObject();

                json.put("UserName", "ajit");
                json.put("Password", "ajit99");
                json.put("otp", strOTP);

                JSONArray postjson = new JSONArray();
                postjson.put(json);
                httppost.setHeader("json", json.toString());
                httppost.getParams().setParameter("jsonpost", postjson);
                HttpResponse response = httpclient.execute(httppost);
                if (response != null) {
                    JSONObject result = AsyncManager.getJsonObject(response.getEntity().getContent());
                    JSONArray jArray = result.getJSONArray("result");
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject e = jArray.getJSONObject(i);
                        JSONObject jObject = new JSONObject(e.getString("post"));
                        return jObject.getString("status");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result)
        {
            progressDialog.dismiss();
            if(result!=null && result.equals("SUCCESS"))
            {
                //
                try
                {
                    DatabaseHelper db=new DatabaseHelper(OTPValidationActivity.this);
                    String strUpdateClientMaster="Update clientMaster set ClientID='"+result+"' where ClientID='"+0+"'";
                    db.UpdateTable(strUpdateClientMaster);
                    db.CloseDB();

                    Intent intent = new Intent(OTPValidationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if(result!=null && result.equals("Sorry OTP not found"))
            {
                Toast.makeText(OTPValidationActivity.this,""+result.toString(),Toast.LENGTH_LONG).show();

                DatabaseHelper db=new DatabaseHelper(OTPValidationActivity.this);
                String strDeleteClientMaster="Delete from clientMaster where ClientID='"+0+"'";
                db.DeleteData(strDeleteClientMaster);
                db.CloseDB();
            }
            else if(result!=null && result.equals("FAILED"))
            {
                Toast.makeText(OTPValidationActivity.this,""+result.toString(),Toast.LENGTH_LONG).show();

                DatabaseHelper db=new DatabaseHelper(OTPValidationActivity.this);
                String strDeleteClientMaster="Delete from clientMaster where ClientID='"+0+"'";
                db.DeleteData(strDeleteClientMaster);
                db.CloseDB();
            }
        }
    }
}
