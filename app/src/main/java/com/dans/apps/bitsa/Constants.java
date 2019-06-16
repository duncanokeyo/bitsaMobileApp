package com.dans.apps.bitsa;

public class Constants {

    public static final String PRIVACY_POLICY_URL = "";
    public static final String NOTIFICATION_CHANNEL_ID = "com.bitsanotifications";

    public interface MpesaEndPoints{
        String BaseCallbackUrl="https://us-central1-bitsa-8978e.cloudfunctions.net/mpesa/";
        String AccessTokenGenerator = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
        String ProcessRequest = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
    }

    public interface MpesaAuthFields{
        String ACCESS_TOKEN="access_token";
        String EXPIRES_IN="expires_in";
    }

    public interface MpesaProcessRequestParams{
        String BUSINESS_SHORT_CODE="BusinessShortCode";
        String PASSWORD = "Password";
        String TIMESTAMP="Timestamp";
        String TRANSACTION_TYPE="TransactionType";
        String AMOUNT="Amount";
        String PARTYA="PartyA";
        String PARTYB="PartyB";
        String PHONE_NUMBER="PhoneNumber";
        String CALLBACK_URL="CallBackURL";
        String ACCOUNT_REFERENCE="AccountReference";
        String TRANSACTION_DESCRIPTION="TransactionDesc";
    }

    public interface PATHS{
        String users = "users";
        String announcements = "announcements";
        String suggestions = "suggestions";
        String bitsaProjects = "bitsaProjects";
        String seniorYearProjects = "seniorYearProjects";
        String studentProjects = "studentProjects";
        String transactions = "transactions";
        String mpesaTransactionDetails = "/info/transactions/mpesa";
        String semesterDetails = "/info/semester";
        String contacts = "/info/contacts";
        String roles = "/info/roles";
        String contribution = "contributions";
    }

    public interface PROJECT_TYPES {
        int BITSA_PROJECT = 1;
        int PERSONAL_PROJECTS = 2;
        int SENIOR_PROJECTS = 3;
    }


    public interface TOPICS{
        String GENERAL_NOTIFICATIONS = "general_notifications";
    }
    public interface ROLES{
        int NO_ROLE = -1;
    }

    public interface TRANSACTION_TYPE{
        int UNKOWN = -1;
        int CONTRIBUTION = 1;
        int CLUB_PAYMENT = 2;

    }

    public static final String KEY_USER_TYPE = "user_type";

    public interface USER_TYPE {
        int VISITOR = 0;
        int HOD = 1;
        int CLUB_SPONSOR = 2;
        int SUPER_ADMIN = 22;
        int STUDENT = 20;
        int CLUB_OFFICIAL = 21;
    }

    public static String [] PROJECT_STATUS_ARRAY={"idea","ongoing","complete","failed"};
    public static String [] STUDENT_MAJOR_ARRAY={"Software Engineering","Networking","BBIT","Diploma"};
    public interface PROJECT_STATUS {

        int IDEA =0;
        int ONGOING = 1;
        int COMPLETE = 2;
        int FAILED = 3;
    }



}
