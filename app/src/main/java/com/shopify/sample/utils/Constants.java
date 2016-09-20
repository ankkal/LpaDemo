package com.shopify.sample.utils;

import com.citrus.sdk.Environment;

/**
 * Created by salil on 13/6/15.
 */
public class Constants {

    //    public static String BILL_URL = "https://salty-plateau-1529.herokuapp.com/billGenerator.production.wallet.php";
    public static String BILL_URL = "https://salty-plateau-1529.herokuapp.com/billGenerator.sandbox.php";
    //public static String BILL_URL_BN = "https://salty-plateau-1529.herokuapp.com/billGenerator.sandbox.bn.php";
    public static String BILL_URL_BC = "https://salty-plateau-1529.herokuapp.com/billGenerator.sandbox.bc.php";
    public static String RETURN_URL_LOAD_MONEY = "https://salty-plateau-1529.herokuapp.com/redirectUrlLoadCash.php";

    // Sand wallet PG
    public static String SIGNUP_ID = "9hh5re3r5q-signup";
    public static String SIGNUP_SECRET = "3be4d7bf59c109e76a3619a33c1da9a8";
    public static String SIGNIN_ID = "9hh5re3r5q-signin";
    public static String SIGNIN_SECRET = "ffcfaaf6e6e78c2f654791d9d6cb7f09";
    public static String VANITY = "nativeSDK";
    public static Environment environment = Environment.SANDBOX;

    public static boolean enableLogging = true;
    public static boolean ENABLE_ONE_TAP_PAYMENT = true;

    public static String colorPrimaryDark = "#E7961D";
    public static String colorPrimary = "#F9A323";
    public static String textColor = "#ffffff";

    public static String INTENT_EXTRA_ENVIRONMENT_CHANGED = "INTENT_EXTRA_ENVIRONMENT_CHANGED";
    public static String INTENT_EXTRA_MERCHANT_DETAILS_CHANGED = "INTENT_EXTRA_MERCHANT_DETAILS_CHANGED";
}
