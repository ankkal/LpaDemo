package com.example.android.lpademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.shopify.buy.dataprovider.BuyClientError;
import com.shopify.buy.dataprovider.Callback;
import com.shopify.buy.model.AccountCredentials;
import com.shopify.buy.model.Customer;
import com.shopify.sample.activity.CollectionListActivity;
import com.shopify.sample.activity.base.SampleActivity;
import com.shopify.sample.application.SampleApplication;
import com.shopify.sample.customer.CustomerLoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends SampleActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private CallbackManager mFacebookCallbackManager;
    private LoginButton mFacebookSignInButton;
    private final String LOG_TAG = LoginActivity.class.getSimpleName();
    private ImageButton mLoginButton;
    private AmazonAuthorizationManager mAuthManager;
    private TextView mProfileText;
    private TextView mLogoutTextView;
    private TextView tv_username;
    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    public static final String EXTRAS_PENDING_ACTIVITY_INTENT = "pending_activity_intent";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        try
        {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
        }
        catch(IllegalArgumentException e)
        {
            showAuthToast("APIKey is incorrect or does not exist.");
            //Log.e(TAG, "Unable to Use Amazon Authorization Manager. APIKey is incorrect or does not exist.", e);
        }
        mProfileText = (TextView) findViewById(R.id.profile_info);
        mLogoutTextView = (TextView) findViewById(R.id.logout);
        if (SampleApplication.getCustomer() == null) {
            Button nativeCheckoutButton = (Button) findViewById(R.id.ShopifyStore);
            nativeCheckoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNativeCheckoutButtonClicked();
                }
            });
        } else {
            //menu.removeItem(R.id.action_login);
        }

        //createCustomer("ankit81008@gmail.com", "pass1234", "Ankit", "kala");
        String logoutText = getString(R.string.logout);
        SpannableString underlinedLogoutText = new SpannableString(logoutText);
        underlinedLogoutText.setSpan(new UnderlineSpan(), 0, logoutText.length(), 0);
        mLogoutTextView.setText(underlinedLogoutText);

        mLogoutTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mAuthManager.clearAuthorizationState(new APIListener() {
                    @Override
                    public void onSuccess(Bundle results) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                               // setLoggedOutState();
                                mLoginButton.setVisibility(Button.VISIBLE);
                                mProfileText.setText(getString(R.string.default_message));

                            }
                        });
                    }

                    @Override
                    public void onError(AuthError authError) {
                        Log.e("Error", "Error clearing authorization state.", authError);
                    }
                });
            }
        });
                    // Find the button with the login_with_amazon ID
        // and set up a click handler
        mLoginButton = (ImageButton) findViewById(R.id.login_with_amazon);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthManager.authorize(
                        new String[]{"profile", "postal_code"},
                        Bundle.EMPTY, new AuthorizeListener());
            }
        });

        tv_username= (TextView) findViewById(R.id.tv_username);

        //Register both button and add click listener
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.btn_logout).setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookSignInButton = (LoginButton)findViewById(R.id.facebook_sign_in_button);
        mFacebookSignInButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        Log.d(LOG_TAG, "1");
            mFacebookSignInButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            //Toast.makeText(getApplicationContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                            AccessToken accessToken = loginResult.getAccessToken();
                            Profile profile = Profile.getCurrentProfile();

                            // Facebook Email address
                            GraphRequest request = GraphRequest.newMeRequest(
                                    loginResult.getAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(
                                                JSONObject object,
                                                GraphResponse response) {
                                            Log.v("LoginActivity Response ", response.toString());

                                            try {
                                                String Name = object.getString("name");

                                                String FEmail = object.getString("email");
                                                Log.v("Email = ", " " + FEmail);
                                                Toast.makeText(getApplicationContext(), "Name " + Name, Toast.LENGTH_LONG).show();


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email,gender, birthday");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }

                        @Override
                        public void onCancel() {
                            // App code
                            Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            // App code
                            Toast.makeText(getApplicationContext(), R.string.login_error, Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        // This MUST be placed after the above two lines.


    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.collection_list_activity, menu);

        if (SampleApplication.getCustomer() == null) {
            menu.removeItem(R.id.action_logout);
        } else {
            menu.removeItem(R.id.action_login);
        }

        return true;
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_login: {
                final Intent intent = new Intent(this, CustomerLoginActivity.class);
                startActivity(intent);
                return true;
            }

            case R.id.action_logout: {
                SampleApplication.setCustomer(null);
                SampleApplication.getBuyClient().logoutCustomer(new Callback<Void>() {
                    @Override
                    public void success(Void body) {
                    }

                    @Override
                    public void failure(BuyClientError error) {
                    }
                });
                invalidateOptionsMenu();
                return true;
            }

            case R.id.action_orders:
               // onOrdersClick();
                return true;

            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        Log.d("data", "9999");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.d("data", "999900000");


            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            int statusCode = result.getStatus().getStatusCode();
            Log.d("statusCode", String.valueOf(statusCode));

            handleSignInResult(result);
        }
    }
    private class AuthorizeListener implements AuthorizationListener {

        /* Authorization was completed successfully. */
        @Override
        public void onSuccess(Bundle response) {
            mAuthManager.getProfile(new ProfileListener());
        }
        /* There was an error during the attempt to authorize the application. */
        @Override
        public void onError(AuthError ae) {
        }
        /* Authorization was cancelled before it could be completed. */
        @Override
        public void onCancel(Bundle cause) {
        }
    }

    private class ProfileListener implements APIListener {

        /* getProfile completed successfully. */
        @Override
        public void onSuccess(Bundle response) {
            // Retrieve the data we need from the Bundle
            Bundle profileBundle = response.getBundle(
                    AuthzConstants.BUNDLE_KEY.PROFILE.val);
            String name = profileBundle.getString(
                    AuthzConstants.PROFILE_KEY.NAME.val);
            String email = profileBundle.getString(
                    AuthzConstants.PROFILE_KEY.EMAIL.val);
            String account = profileBundle.getString(
                    AuthzConstants.PROFILE_KEY.USER_ID.val);
            String zipcode = profileBundle.getString(
            AuthzConstants.PROFILE_KEY.POSTAL_CODE.val);
            //showAuthToast("Logged in with Amazon"+email+"--"+email);
            Log.d("Profile data", email+name);
            StringBuilder profileBuilder = new StringBuilder();
            profileBuilder.append(String.format("Welcome, %s!\n", profileBundle.getString(AuthzConstants.PROFILE_KEY.NAME.val)));
            profileBuilder.append(String.format("Your email is %s\n", profileBundle.getString(AuthzConstants.PROFILE_KEY.EMAIL.val)));
            final String profile = profileBuilder.toString();
            Log.d("Test", "Profile Response: " + profile);
            loginCustomerWithCallback(email,"pass1234");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateProfileView(profile);
                    //setLoggedInState();
                }
            });
        }
        /* There was an error during the attempt to get the profile. */
        @Override
        public void onError(AuthError ae) {
        }
    }

    /**
     * Sets the text in the mProfileText {@link TextView} to the value of the provided String.
     * @param profileInfo the String with which to update the {@link TextView}.
     */
    private void updateProfileView(String profileInfo) {
        Log.d("Inside profile", "Updating profile view");
        mProfileText.setText(profileInfo);
        showLoadingDialog(R.string.loading_data);
//        Intent intent = new Intent("com.shopify.sample.activity.CollectionListActivity");

        // mLoginButton.setVisibility(Button.GONE);

    }
    private class TokenListener implements APIListener{

        /* getToken completed successfully. */
        @Override
        public void onSuccess(Bundle response) {
            final String authzToken =
                    response.getString(AuthzConstants.BUNDLE_KEY.TOKEN.val);
            if (!TextUtils.isEmpty(authzToken))
            {
                // Retrieve the profile data
                mAuthManager.getProfile(new ProfileListener());
            }
        }
        /* There was an error during the attempt to get the token. */
        @Override
        public void onError(AuthError ae) {
        }
    }
    private void showAuthToast(String authToastMessage){
        Toast authToast = Toast.makeText(getApplicationContext(), authToastMessage, Toast.LENGTH_LONG);
        authToast.setGravity(Gravity.CENTER, 0, 0);
        authToast.show();
    }
    @Override
    protected void onStart(){
        super.onStart();
        mAuthManager.getToken(new String[]{"profile", "postal_code"}, new TokenListener());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.sign_in_button:

                signIn();

                break;
            case R.id.btn_logout:

                signOut();

                break;
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        tv_username.setText("");
                    }
                });
    }



    private void handleSignInResult(GoogleSignInResult result) {

        Log.d("handleSignInResult", "Updating profile");

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            Log.d("Inside Gmail profile", "Updating profile view");

            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d("Inside Gmail profile", acct.getDisplayName());

            tv_username.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));


        } else {
            Log.d("Inside Gmail profile", "222");
            // Signed out, show unauthenticated UI.
            // updateUI(false);
        }
    }
    /**
     * For our sample native checkout, we use a hardcoded credit card.
     */
    private void onNativeCheckoutButtonClicked() {

        showLoadingDialog(R.string.loading_data);
//        Intent intent = new Intent("com.shopify.sample.activity.CollectionListActivity");
        final Intent intent = new Intent(this, CollectionListActivity.class);
        startActivity(intent);

    }
    public void createCustomer(final String email, final String password, final String firstName, final String lastName) {

        //        showProgress();
        Log.d("Inside create", "Start");
        // Create credential items with email, first name, last name,
        // password and password confirmation
        final AccountCredentials accountCredentials = new AccountCredentials(email, "password123", "afirstname", "alastname");

        // The customer will be retrieved automatically if the sign up was successful
        SampleApplication.getBuyClient().createCustomer(accountCredentials, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                // save the customer or token for later use
                Log.d("createCustomer succ", "created");

            }

            @Override
            public void failure(BuyClientError error) {
                // handle error
                Log.d("createCustomer fail", error.toString());
            }
        });

}
    public void loginCustomerWithCallback(final String email, final String password) {
        // Create credential items with email and password
        final AccountCredentials accountCredentials = new AccountCredentials(email, "password123");

        SampleApplication.getBuyClient().loginCustomer(accountCredentials, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                // save the customer or token for later use
                onFetchCustomerSuccess(customer);
                Log.d("login succ  is", "success");
            }

            @Override
            public void failure(BuyClientError error) {
                // handle error
                Log.d("login err", error.toString());
                createCustomer(email, "password123", "Test", "Test");
            }
        });

    }
    private void onFetchCustomerSuccess(final Customer customer) {
        SampleApplication.setCustomer(customer);
       /* hideProgress();
        if (attached) {
            view.onLoginCustomerSuccess();
        }*/
    }

}
