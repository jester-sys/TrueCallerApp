package com.jaixlabs.calleridapp.helpers



import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ContactUtils {

    interface OnContactNameRetrievedListener {
        fun onContactNameRetrieved(contactName: String)
    }

    companion object {
        private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

        fun getContactNameByPhoneNumber(
            context: Context,
            phoneNumber: String,
            listener: OnContactNameRetrievedListener
        ) {
            executorService.execute {
                var contactName = ""

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )

                    val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

                    cursor?.use {
                        while (it.moveToNext()) {
                            val storedNumber =
                                it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            if (PhoneNumberUtils.compare(storedNumber, phoneNumber)) {
                                contactName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                                break // Exit the loop as we found the contact
                            }
                        }
                    }
                }

                // Run the listener callback on the main thread
                Handler(Looper.getMainLooper()).post {
                    listener.onContactNameRetrieved(contactName)
                }
            }
        }
    }
}
