package com.example.jayesh.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONObject;

import java.util.Arrays;

public class FragFb extends Fragment {

    CallbackManager callbackManager;
    TextView tv;
    ShareDialog shareDialog;

    private OnFragmentInteractionListener mListener;

    public FragFb() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*FacebookSdk.sdkInitialize(getActivity());
        AppEventsLogger.activateApp(getActivity());*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_fb, container, false);

        tv = (TextView) view.findViewById(R.id.tv);
        view.findViewById(R.id.btnFb).setOnClickListener(onClickListener);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setFragment(this);
        loginButton.setReadPermissions("email");
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, facebookCallback);

        shareDialog = new ShareDialog(getActivity());

        return view;
    }

    View.OnClickListener  onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            LoginManager loginManager = LoginManager.getInstance();
            loginManager.registerCallback(callbackManager, facebookCallback);
            loginManager.logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email"));

        }
    };

    protected void getUserInfo(LoginResult login_result) {

        GraphRequest data_request = GraphRequest.newMeRequest(
                login_result.getAccessToken(),

                new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(
                            JSONObject json_object,
                            GraphResponse response) {

                        Log.e("resp", json_object.toString());

                        tv.setText(json_object.toString());

                        if (ShareDialog.canShow(ShareLinkContent.class)) {

                            Log.e("image pass facebook", "" + "hello test");

                            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                    .setContentTitle("Test4Grade")
                                    .setContentDescription("the link to be shared")
                                    .setContentUrl(Uri.parse("https://test4grade.com"))
                                    .build();

                            /*.setImageUrl(Uri.parse("android.resource://" + getPackageName() + "/"
                                    + R.drawable.account_add))*/

                            shareDialog.show(linkContent);  // Show facebook ShareDialog
                        }
                    }
                });

        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email,picture.width(120).height(120)");
        data_request.setParameters(permission_param);
        data_request.executeAsync();
    }

    FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {

            Log.e("key hask", loginResult.getAccessToken().toString());
            Log.e("tocken", loginResult.getAccessToken().getToken());

            getUserInfo(loginResult);
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException error) {
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
