package com.beloushkin.firemessage

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beloushkin.firemessage.util.FirestoreUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import androidx.annotation.NonNull
import com.beloushkin.firemessage.service.MyFirebaseMessagingService
import com.google.android.gms.tasks.OnFailureListener



class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1

    private val signInProviders = listOf(AuthUI.IdpConfig.EmailBuilder()
        .setAllowNewAccounts(true)
        .setRequireName(true)
        .build())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        account_sign_in.setOnClickListener{
            val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(signInProviders)
                .setLogo(R.drawable.ic_fire_emoji)
                .build()
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val progressDialog = indeterminateProgressDialog("Setting up your account")
                //TODO: Initialize current user in Firestore
                FirestoreUtil.initCurrentUserIfFirstTime {
                    startActivity(intentFor<MainActivity>().newTask().clearTask())

                    lateinit var registrationToken:String
                    FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                       MyFirebaseMessagingService.addTokenToFirestore(it.token)
                    }
                    progressDialog.dismiss()
                }
            } else if (resultCode == Activity.RESULT_CANCELED)  {
                if (response == null) return
                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK -> longSnackbar(constraint_layout, "No network...")
                    ErrorCodes.UNKNOWN_ERROR ->longSnackbar(constraint_layout, "Unknown error...")
                }
            }

        }
    }
}
