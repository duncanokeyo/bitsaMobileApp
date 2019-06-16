package com.dans.apps.bitsa.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dans.apps.bitsa.CreditCardPaymentActivity;
import com.dans.apps.bitsa.MpesaPaymentActivity;
import com.dans.apps.bitsa.R;
import com.dans.apps.bitsa.utils.MpesaUtils;
import com.dans.apps.bitsa.utils.UiUtils;
import com.google.android.material.card.MaterialCardView;

import androidx.fragment.app.Fragment;


public class PaymentFragment extends Fragment implements View.OnClickListener {

    MaterialCardView mpesaCard;
    MaterialCardView creditCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_payment, container, false);
        mpesaCard = view.findViewById(R.id.mpesa_card);
        creditCard = view.findViewById(R.id.credit_card);
        creditCard.setOnClickListener(this);
        mpesaCard.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.credit_card:
            case R.id.mpesa_card:
                if(!UiUtils.isOnline(getActivity())){
                    UiUtils.showNotConnectedAlert(getActivity());
                    return;
                }
                if(view.getId() == R.id.credit_card){
                    Intent intent = new Intent(getActivity(), CreditCardPaymentActivity.class);
                    startActivity(intent);
                }else{
                    //start mpesa payment activity
                    Intent intent = new Intent(getActivity(), MpesaPaymentActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }
}
