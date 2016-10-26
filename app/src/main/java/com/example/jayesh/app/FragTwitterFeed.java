package com.example.jayesh.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.PlusShare;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class FragTwitterFeed extends Fragment {

    TextView tv;
    Twitter twitter;
    RequestToken requestToken;

    public static final int WEBVIEW_REQUEST_CODE = 100;

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    public static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";

    String TWITTER_OAUTH_TOKEN = "";
    String TWITTER_OAUTH_SECRAT = "";

    public static final String TWITTER_CALLBACK_URL = "test4grade";

    public static final String TWITTER_KEY = "MgScJ4jo84rqSboKxsXxoQN89";
    public static final String TWITTER_SECRET = "yvUm961kzu0UjK8QjgzEe9SCfaB5TYaFbD4o4UDreNhvoEAd84";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_frag_twitter_feed, container, false);

        tv = (TextView) view.findViewById(R.id.tv);

        view.findViewById(R.id.btnSend).setOnClickListener(twickClick);
        view.findViewById(R.id.btnGoogleShare).setOnClickListener(twickClick);
        view.findViewById(R.id.btnEmail).setOnClickListener(twickClick);
        view.findViewById(R.id.btnWhatsup).setOnClickListener(twickClick);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        return view;
    }

    View.OnClickListener twickClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.btnSend:
                    new HttpAsynctask_tweet().execute();
                    break;
                case R.id.btnGoogleShare:
                    Intent shareIntent = new PlusShare.Builder(getActivity())
                            .setType("text/plain")
                            .setText("This is title")
                            //.setContentUrl(Uri.parse(items.get(position_extra).guid))
                            .getIntent();
                    startActivityForResult(shareIntent, 0);

                    break;
                case R.id.btnEmail:

                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setType("plain/text");
                    sendIntent.setData(Uri.parse("test@gmail.com"));
                    sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"test@gmail.com"});
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "title");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "hee test");
                    //sendIntent.putExtra(Intent.EXTRA_STREAM, ImageMain); //for image
                    sendIntent.setType("*/*");
                    startActivity(sendIntent);

                    break;
                case R.id.btnWhatsup:
                    Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher), null, null));
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "hiiiii title");
                    intent.putExtra(Intent.EXTRA_TEXT, "http://www.google.com");
                    intent.putExtra(Intent.EXTRA_STREAM, imageUri); //for image
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("result fragment", resultCode + "");

        if (resultCode == Activity.RESULT_OK) {

            try {
                String verifier = data.getExtras().getString(URL_TWITTER_OAUTH_VERIFIER);
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                long userID = accessToken.getUserId();
                final User user = twitter.showUser(userID);
                String username = user.getName();

                TWITTER_OAUTH_TOKEN = accessToken.getToken();
                TWITTER_OAUTH_SECRAT = accessToken.getTokenSecret();

                tv.setText(username);

                //saveTwitterInfo(accessToken);
                new updateTwitterStatus().execute("hi test");

            } catch (Exception e) {
                // Log.e("Twitter Login Failed", e.getMessage());
                e.printStackTrace();
            }
        }
    }


    ProgressDialog dialog;

    class HttpAsynctask_tweet extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(MainActivity.TWITTER_KEY);
            builder.setOAuthConsumerSecret(MainActivity.TWITTER_SECRET);

            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);

                final Intent intent = new Intent(getActivity(), TwitterWebViewActivity.class);
                intent.putExtra(TwitterWebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);

            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
        }
    }


    class updateTwitterStatus extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Void doInBackground(String... args) {

            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(MainActivity.TWITTER_KEY);
                builder.setOAuthConsumerSecret(MainActivity.TWITTER_SECRET);

                AccessToken accessToken = new AccessToken(TWITTER_OAUTH_TOKEN, TWITTER_OAUTH_SECRAT);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                StatusUpdate statusUpdate = new StatusUpdate(status);
                /*InputStream is = getResources().openRawResource(R.drawable.ic_launcher);
                statusUpdate.setMedia("test.jpg", is);*/

                twitter4j.Status response = twitter.updateStatus(statusUpdate);

                Log.d("Status", response.getText());

            } catch (TwitterException e) {
                Log.d("Failed to post!", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getActivity(), "Posted to Twitter!", Toast.LENGTH_SHORT).show();
        }

    }
}
