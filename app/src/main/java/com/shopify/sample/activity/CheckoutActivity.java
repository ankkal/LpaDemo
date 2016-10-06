/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.shopify.sample.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lpademo.R;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.shopify.buy.dataprovider.BuyClientError;
import com.shopify.buy.dataprovider.Callback;
import com.shopify.buy.model.Checkout;
import com.shopify.buy.model.CreditCard;
import com.shopify.buy.model.PaymentToken;
import com.shopify.sample.activity.base.SampleActivity;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


/**
 * The final activity in the app flow. Allows the user to choose between:
 * 1. A native checkout where the payment info is hardcoded and the checkout is completed within the app; or
 * 2. A web checkout where the user enters their payment info and completes the checkout in a web browser;
 */
public class CheckoutActivity extends SampleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.checkout);
        setContentView(R.layout.checkout_activity);

        final boolean didCreateCheckout = !TextUtils.isEmpty(getSampleApplication().getCheckout().getToken());


/*
        Button webCheckoutButton = (Button) findViewById(R.id.web_checkout_button);
        if (didCreateCheckout) {
            webCheckoutButton.setVisibility(View.VISIBLE);
            webCheckoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onWebCheckoutButtonClicked();
                }
            });
        } else {
            webCheckoutButton.setVisibility(View.GONE);
        }

        Button cartPermalinkButton = (Button) findViewById(R.id.cart_permalink_button);
        cartPermalinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCartPermalinkClicked();
            }
        });
*/

        Button razorPayButton = (Button) findViewById(R.id.razorpay_button);
        razorPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });

        Button paytmPayButton = (Button) findViewById(R.id.paytmbutton);
        paytmPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPaytmPayment();
            }
        });

        updateOrderSummary();
    }
    
    /**
     * For our sample native checkout, we use a hardcoded credit card.
     */
    private void onNativeCheckoutButtonClicked() {
/*        getSampleApplication().completeCheckout(new Callback<Checkout>() {
            @Override
            public void success(Checkout checkout) {
                onCheckoutComplete();
            }

            @Override
            public void failure(BuyClientError error) {
                onError(error);
            }
        });*/
        // Create the card to send to Shopify.  This is hardcoded here for simplicity, but the user should be prompted for their credit card information.
        final CreditCard creditCard = new CreditCard();
        creditCard.setFirstName("Dinosaur");
        creditCard.setLastName("Banana");
        creditCard.setMonth("2");
        creditCard.setYear("20");
        creditCard.setVerificationValue("123");
        creditCard.setNumber("4242424242424242");

        showLoadingDialog(R.string.completing_checkout);
        getSampleApplication().storeCreditCard(creditCard, new Callback<PaymentToken>() {
            @Override
            public void success(PaymentToken paymentToken) {
                onCreditCardStored();
            }

            @Override
            public void failure(BuyClientError error) {
                onError(error);
            }
        });
    }

    /**
     * When the credit card has successfully been added, complete the checkout and begin polling.
     */
    private void onCreditCardStored() {
        getSampleApplication().completeCheckout(new Callback<Checkout>() {
            @Override
            public void success(Checkout checkout) {
                onCheckoutComplete();
            }

            @Override
            public void failure(BuyClientError error) {
                onError(error);
            }
        });
    }

    /**
     * Launch the device browser so the user can complete the checkout.
     */
    private void onWebCheckoutButtonClicked() {
        launchBrowser(getSampleApplication().getCheckout().getWebUrl());
    }

    /**
     * Launch the device browser using the cart permalink method
     */
    private void onCartPermalinkClicked() {
        launchBrowser(getSampleApplication().getCartPermalink());
    }

    private void launchBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setData(Uri.parse(url));

        try {
            intent.setPackage("com.android.chrome");
            startActivity(intent);

        } catch (Exception launchChromeException) {
            try {
                // Chrome could not be opened, attempt to us other launcher
                intent.setPackage(null);
                startActivity(intent);

            } catch (Exception launchOtherException) {
                onError(getString(R.string.checkout_error));
            }
        }
    }

    public void startPayment(){
        /**
         * Replace with your public key
         */
        final String public_key = "rzp_live_ILgsfZCZoFIKMb";

        /**
         * You need to pass current activity in order to let razorpay create CheckoutActivity
         */
        final Activity activity = this;

        final  com.razorpay.Checkout co = new com.razorpay.Checkout();
        co.setPublicKey(public_key);

        try{
            JSONObject options = new JSONObject("{" +
                    "description: 'Demoing Charges'," +
                    "image: 'https://rzp-mobile.s3.amazonaws.com/images/rzp.png'," +
                    "currency: 'INR'}"
            );
            final Checkout checkout = getSampleApplication().getCheckout();

            DecimalFormat decimalFormat = new DecimalFormat(".");
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setDecimalSeparatorAlwaysShown(false);
            String amount=decimalFormat.format(Double.valueOf(checkout.getPaymentDue())*100) ;
            options.put("amount", amount);
            options.put("name", "Razorpay Corp");
            options.put("prefill", new JSONObject("{email: 'sm@razorpay.com', contact: '9876543210'}"));
            Log.d("RZP Json is", options.toString());
            co.open(activity, options);

        } catch(Exception e){
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * The name of the function has to be
     *   onPaymentSuccess
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    public void onPaymentSuccess(String razorpayPaymentID){
        try {
       /*     getSampleApplication().completeCheckout(razorpayPaymentID,new Callback<Checkout>() {
                                                        @Override
                                                        public void success(Checkout checkout) {
                                                            onCheckoutComplete();
                                                        }

                                                        @Override
                                                        public void failure(BuyClientError error) {
                                                            onError(error);
                                                        }
                                                    });*/

            findViewById(R.id.discount_row).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.razorpayPaymentID)).setText(razorpayPaymentID);
            ((TextView) findViewById(R.id.razorpayPaymentStatus)).setText("Success");
            Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Log.e("com.merchant", e.getMessage(), e);
        }
    }

    /**
     * The name of the function has to be
     *   onPaymentError
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    public void onPaymentError(int code, String response){
        try {
            ((TextView) findViewById(R.id.razorpayPaymentStatus)).setText("Failed");
            Toast.makeText(this, "Payment failed: " + Integer.toString(code) + " " + response, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Log.e("com.merchant", e.getMessage(), e);
        }
    }

    public void startPaytmPayment() {

        PaytmPGService Service = PaytmPGService.getStagingService();
        Map<String, String> paramMap = new HashMap<String, String>();
        final Checkout checkout1 = getSampleApplication().getCheckout();

        DecimalFormat decimalFormat = new DecimalFormat(".");
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        String amount = checkout1.getPaymentDue();
        // these are mandatory parameters

        paramMap.put("ORDER_ID", checkout1.getToken());
        paramMap.put("MID", "Variet41832464800424");
        paramMap.put("CUST_ID", checkout1.getCustomerId().toString());
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
        paramMap.put("WEBSITE", "APP_URL");
        paramMap.put("TXN_AMOUNT", amount);
        paramMap.put("THEME", "merchant");
        paramMap.put("EMAIL", "ankit81008@gmail.com");
        paramMap.put("MOBILE_NO", "9886000725");
        PaytmOrder Order = new PaytmOrder(paramMap);

        PaytmMerchant Merchant = new PaytmMerchant(
                "http://www.3deestudio.com/myshop/paytm/generateChecksum.php",
                "http://www.3deestudio.com/myshop/paytm/verifyChecksum.php");

        Service.initialize(Order, Merchant, null);

        Service.startPaymentTransaction(this, true, true,
                new PaytmPaymentTransactionCallback() {
                    @Override
                    public void someUIErrorOccurred(String inErrorMessage) {
                        // Some UI Error Occurred in Payment Gateway Activity.
                        // // This may be due to initialization of views in
                        // Payment Gateway Activity or may be due to //
                        // initialization of webview. // Error Message details
                        // the error occurred.
                    }

                    @Override
                    public void onTransactionSuccess(Bundle inResponse) {
                        // After successful transaction this method gets called.
                        // // Response bundle contains the merchant response
                        // parameters.
                        Log.d("LOG", "Payment Transaction is successful " + inResponse.toString());
                        Toast.makeText(getApplicationContext(), "Payment Transaction is successful ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onTransactionFailure(String inErrorMessage,
                                                     Bundle inResponse) {
                        // This method gets called if transaction failed. //
                        // Here in this case transaction is completed, but with
                        // a failure. // Error Message describes the reason for
                        // failure. // Response bundle contains the merchant
                        // response parameters.
                        Log.d("LOG", "Payment Transaction Failed " + inErrorMessage);
                        Toast.makeText(getBaseContext(), "Payment Transaction Failed ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void networkNotAvailable() { // If network is not
                        // available, then this
                        // method gets called.
                    }

                    @Override
                    public void clientAuthenticationFailed(String inErrorMessage) {
                        // This method gets called if client authentication
                        // failed. // Failure may be due to following reasons //
                        // 1. Server error or downtime. // 2. Server unable to
                        // generate checksum or checksum response is not in
                        // proper format. // 3. Server failed to authenticate
                        // that client. That is value of payt_STATUS is 2. //
                        // Error Message describes the reason for failure.
                    }

                    @Override
                    public void onErrorLoadingWebPage(int iniErrorCode,
                                                      String inErrorMessage, String inFailingUrl) {

                    }

                    // had to be added: NOTE
                    @Override
                    public void onBackPressedCancelTransaction() {
                        // TODO Auto-generated method stub
                    }

                });

    }}
