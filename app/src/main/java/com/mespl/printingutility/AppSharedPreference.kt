package com.mespl.printingutility

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by Aman on 14/2/19.
 */
object AppSharedPreference {

    fun getInt(context: Context?, key: PREF_KEY): Int {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val value = sharedPref.getInt(key.KEY, 0)
        return value
    }

    fun putInt(context: Context?, key: PREF_KEY, value: Int) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putInt(key.KEY, value)

        // Commit the edits!
        editor.commit()
    }

    fun putLong(context: Context?, key: PREF_KEY, value: Long) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putLong(key.KEY, value)
        editor.commit()
    }

    fun putFloat(context: Context?, key: PREF_KEY, value: Float) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putFloat(key.KEY, value)
        editor.commit()
    }

    fun getFloat(context: Context?, key: PREF_KEY): Float {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val value = sharedPref.getFloat(key.KEY, 0f)
        return value
    }

    fun getLong(context: Context?, key: PREF_KEY): Long {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val value = sharedPref.getLong(key.KEY, 0)
        return value
    }

    fun putString(context: Context?, key: PREF_KEY, value: String?) {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putString(key.KEY, value)

        // Commit the edits!
        editor.commit()
    }


    // save setting data
    /* public static void putString(Context context,PREF_SETT_KEY keysett,String value){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keysett.SETT_KEY,value);
        editor.commit();
    }*/
    fun getString(context: Context?, key: PREF_KEY): String? {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val value = sharedPref.getString(key.KEY, null)
        return value
    }


    fun putBoolean(context: Context?, key: PREF_KEY, value: Boolean) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putBoolean(key.KEY, value)
        editor.commit()
    }

    fun getBoolean(context: Context?, key: PREF_KEY): Boolean {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val value = sharedPref.getBoolean(key.KEY, false)
        return value
    }

    fun putString(context: Context?, key: String?, value: String?) {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putString(key, value)

        editor.commit()
    }

    fun getString(context: Context?, key: String?): String? {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val value = sharedPref.getString(key, null)

        return value
    }

    fun clearAllPreferences(context: Context?) {
        val sharedPref = PreferenceManager
            .getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.clear()
        editor.commit()
    }


    enum class PREF_KEY(key: String) {
        MAC("MAC"),
        AUTO_RECONNECT("AUTO_RECONNECT");

        val KEY: String? = key

    }


}
