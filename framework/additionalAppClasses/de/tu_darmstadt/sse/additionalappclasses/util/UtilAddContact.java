package de.tu_darmstadt.sse.additionalappclasses.util;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import de.tu_darmstadt.sse.additionalappclasses.hooking.Hooker;

public class UtilAddContact {
	
	public static void writePhoneContact(String displayName, String number)
	{
		Context contetx 	= Hooker.applicationContext; //Application's context or Activity's context
		String strDisplayName 	=  displayName; // Name of the Person to add
		String strNumber 	=  number; //number of the person to add with the Contact
			
		ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
	        int contactIndex = cntProOper.size();//ContactSize
	 
	        //Newly Inserted contact
	        // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
	        cntProOper.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)//Step1
	                .withValue(RawContacts.ACCOUNT_TYPE, null)
	                .withValue(RawContacts.ACCOUNT_NAME, null).build());
	 
	        //Display name will be inserted in ContactsContract.Data table
	        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)//Step2
	                .withValueBackReference(Data.RAW_CONTACT_ID,contactIndex)
	                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
	                .withValue(StructuredName.DISPLAY_NAME, strDisplayName) // Name of the contact
	                .build());
	        //Mobile number will be inserted in ContactsContract.Data table
	        cntProOper.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)//Step 3
	                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,contactIndex)
	                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
	                .withValue(Phone.NUMBER, strNumber) // Number to be added
	                .withValue(Phone.TYPE, Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
	        try
	        {
	                // We will do batch operation to insert all above data 
	        	//Contains the output of the app of a ContentProviderOperation. 
	        	//It is sure to have exactly one of uri or count set
	            ContentProviderResult[] contentProresult = null;
	            contentProresult = contetx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list 
	        }
	        catch (RemoteException exp)
	        { 
	            //logs;
	        }
	        catch (OperationApplicationException exp) 
	        {
	            //logs
	        }       
	}
}
