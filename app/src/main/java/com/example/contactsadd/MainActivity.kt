package com.example.contactsadd

import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts.Photo
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var button: FloatingActionButton
    private lateinit var saveButton: FloatingActionButton
    private val TAG = "CONTACT_ADD_TAG"
    private lateinit var contactPermissions: Array<String>
    private var image_uri:Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get contact add permission
        contactPermissions = arrayOf(android.Manifest.permission.WRITE_CONTACTS)
        title = "My Contacts"
        imageView = findViewById(R.id.ivProfile)
        button = findViewById(R.id.btnAddPhoto)

        // Create view gallery intent
        val gallery = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                imageView.setImageURI(it)
                image_uri = it
            })

        // Set floating action button click listener to gallery intent
        button.setOnClickListener {
            gallery.launch("image/*")
        }

        // Get save button id and set click listener
        saveButton = findViewById(R.id.btnSaveContact)
        saveButton.setOnClickListener{
            // Check if we have permissions.
            if (isWriteContactPermissionEnabled()){
                saveContact()
            } else{
                requestWriteContact()
            }
        }
    }
    private fun saveContact() {
        Log.d(TAG, "saveContact: ")

        // Get values inside edit text ids
        val firstNameid = findViewById<EditText>(R.id.etFirstName)
        val lastNameid = findViewById<EditText>(R.id.etLastName)
        val mobilePhoneid = findViewById<EditText>(R.id.etMobile)
        val altPhoneid = findViewById<EditText>(R.id.etMobileAlt)
        val emailid = findViewById<EditText>(R.id.etEmail)

        // Set text values of each edit text to variable.
        val firstName = firstNameid.text.toString().trim()
        val lastName = lastNameid.text.toString().trim()
        val mobilePhone = mobilePhoneid.text.toString().trim()
        val altPhone = altPhoneid.text.toString().trim()
        val email = emailid.text.toString().trim()

        Log.d(TAG, "saveContact: First Name $firstName")
        Log.d(TAG, "saveContact: Last Name $lastName")
        Log.d(TAG, "saveContact: Mobile Phone $mobilePhone")
        Log.d(TAG, "saveContact: Alt Phone $altPhone")
        Log.d(TAG, "saveContact: Email $email")

        // CPO operation to add to contacts.
        val cpo = ArrayList<ContentProviderOperation>()

        // Create contact ID
        val rawContactId = cpo.size
        cpo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )


        // Add first and last name
        cpo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(
                    ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
                    rawContactId
                )
                .withValue(
                    ContactsContract.RawContacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                .build()
        )

        // Add phone numbers
        if (mobilePhoneid != null) {
            cpo.add(ContentProviderOperation.
            newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,mobilePhone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());
        }

        // Add alternate/home phone.
        if (altPhone != null) {
            cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, altPhone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                .build());
        }

        // add email
        if (email != null) {
            cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build());
        }

        // Create bitmap of contact image to be sent to contacts.
        val bitmap = getScreenViewBitmap(imageView)
        val baos = ByteArrayOutputStream()

        // Add image to CPO
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, baos.toByteArray()).build()
            );
        }

            //save contact by applying CPO.

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, cpo)
                Log.d(TAG, "saveContact: Saved")
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.d(TAG, "saveContact: Failed to save ${e.message}")
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
            }


    }


    // Function to get bitmap from Image
    private fun getScreenViewBitmap(view: View): Bitmap {
        val specSize = MeasureSpec.makeMeasureSpec(
            0 /* any */, MeasureSpec.UNSPECIFIED)
        view.measure(specSize, specSize)
        val bitmap = Bitmap.createBitmap(view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(view.left, view.top, view.right, view.bottom)
        view.draw(canvas)
        return bitmap
    }

    private fun isWriteContactPermissionEnabled(): Boolean{
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWriteContact(){
        ActivityCompat.requestPermissions(this, contactPermissions, 100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()){
            if (requestCode == 100){
                val writeContactPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (writeContactPermission){
                    saveContact()
                } else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}