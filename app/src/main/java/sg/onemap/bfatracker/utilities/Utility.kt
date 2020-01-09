package sg.onemap.bfatracker.utilities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import sg.onemap.bfatracker.models.TokenResponse

class Utility(context: Context) {
    private val PREFS_NAME = "APP_CACHE"
    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    object SharedPrefConstants {
        const val PRIVATE_TOKEN = "PRIVATE_TOKEN"
        const val PRIVATE_TOKEN_EXPIRYTIMESTAMP = "PRIVATE_TOKEN_EXPIRYTIMESTAMP"
        const val FIXED_LATITUDE = 1.273750
        const val FIXED_LONGITUDE = 103.801514
        const val TRACK_PRIMARYKEY = "TRACK_PRIMARYKEY"
        const val TRACK_TITLE = "TRACK_TITLE"
    }

    fun save(KEY_NAME: String, value: String) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(KEY_NAME, value)
        editor.commit()
    }

    fun getValueString(KEY_NAME: String): String? {
        return sharedPref.getString(KEY_NAME, null)
    }

    fun saveToken(tokenResponse : TokenResponse){
        try {
            save(SharedPrefConstants.PRIVATE_TOKEN, tokenResponse?.access_token!!)
            save(SharedPrefConstants.PRIVATE_TOKEN_EXPIRYTIMESTAMP, tokenResponse?.expiry_timestamp!!)
        } catch (ex:Exception) {
            Log.e("error", "Error occured in saveToken : "+ex.message.toString())
        }
    }

    fun checkTokenExpiryDate():Boolean {
        var expired:Boolean = true
        try {
            var cacheExpiry = getValueString(SharedPrefConstants.PRIVATE_TOKEN_EXPIRYTIMESTAMP)

            cacheExpiry?.let {
                expired = false
                var currentTimestamp = System.currentTimeMillis()/1000
                var expiryTimestampLng = cacheExpiry?.toLong()!!

                if(currentTimestamp != null && expiryTimestampLng != null){
                    if(currentTimestamp > expiryTimestampLng) {
                        expired = true
                    }
                }
            }
        } catch (ex:Exception) {
            Log.e("error", "Error occured in checkTokenExpiryDate : "+ex.message.toString())
        }

        return expired
    }

    fun hideSoftKeyBoard(context: Context, activity : Activity) {
        try {
            var view = activity.getCurrentFocus()
            if (view == null) {
                view = View(activity)
            }
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            // TODO: handle exception
            e.printStackTrace()
        }
    }
}