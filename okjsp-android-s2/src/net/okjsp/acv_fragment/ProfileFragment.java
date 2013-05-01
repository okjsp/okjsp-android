package net.okjsp.acv_fragment;

import net.okjsp.Const;
import net.okjsp.R;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ProfileFragment extends Fragment implements Const {
    public static final String TAG = ProfileFragment.class.getSimpleName();
    
    protected static final String SCHEME = "profile";
    protected static final String AUTHORITY = "profile";
    public static final Uri URI = new Uri.Builder().scheme(SCHEME).authority(AUTHORITY).build();
    
    protected View mView;
    protected AlertDialog mLoginDlg = null;
    protected View mLoginView = null;
    protected EditText mUsernameView = null;
    protected EditText mPasswordView = null;
    
    protected String mUsername;
    protected String mPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mView = inflater.inflate(R.layout.fragment_profile, container, false);
    	
    	showLoginDialog();
    	
    	return mView;
    }
    
    protected View getLoginView(Context context, String username, String password) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dlg_login, null);
        if (username != null) {
            ((EditText)v.findViewById(R.id.et_username)).setText(username);
        }

        if (password != null) {
            ((EditText)v.findViewById(R.id.et_password)).setText(password);
        }
        
        return v;
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) 
    public AlertDialog.Builder getAlertDialogBuilder(Context context) {
        return ((android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) ?
                new AlertDialog.Builder(context) :
                new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK));        
    }
    
    protected AlertDialog getLoginDialog(Context context, View view) {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        
        builder = getAlertDialogBuilder(context);
        builder.setView(view);
        builder.setTitle(R.string.login);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton(context.getString(R.string.login), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //listener.onSelected(LOGIN_DLG_ACTION_CANCEL, getUsername(mLoginView), getPassword(mLoginView));
            }
        });
        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        
        return alertDialog;
    }
    
    protected void showLoginDialog() {
        mLoginView = getLoginView(getBaseContext(), mUsername, mPassword);
        mLoginDlg = getLoginDialog(getBaseContext(), mLoginView);
        
        mUsernameView = (EditText)mLoginView.findViewById(R.id.et_username);
        mUsernameView.setOnEditorActionListener(
                new OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        boolean comsumed = false;
                        switch (actionId) {
                            case EditorInfo.IME_ACTION_NEXT:
                                if (mPasswordView != null) {
                                    mPasswordView.requestFocus();
                                    comsumed = true;
                                }
                                break;
                        }
                        return comsumed;
                    }
                });
        mPasswordView = (EditText)mLoginView.findViewById(R.id.et_password);
        mPasswordView.setOnEditorActionListener(
                new OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        boolean comsumed = false;
                        switch (actionId) {
                            case EditorInfo.IME_ACTION_SEND:
                                mLoginDlg.dismiss();
                                comsumed = true;
                                break;
                        }
                        return comsumed;
                    }
                });
        mLoginDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {         
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });        
        mLoginDlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mLoginDlg.show();
    }
    
    protected void doLogin(String username, String password) {
    }
    
    protected Context getBaseContext() {
    	return getActivity();
    }
}
