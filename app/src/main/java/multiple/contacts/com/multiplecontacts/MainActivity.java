package multiple.contacts.com.multiplecontacts;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button addNewButton;
    ListView contactsListView;
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> numberList = new ArrayList<>();
    ArrayList<ContactModel> modelList = new ArrayList<>();
    ContactListAdapter adapter;
    private static final int PICK_FROM_FILE = 2;
    private Uri mImageCaptureUri;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNewButton = (Button) findViewById(R.id.btn_AddNew);
        contactsListView = (ListView) findViewById(R.id.list_contacts);
        adapter = new ContactListAdapter(nameList, numberList, MainActivity.this);
        contactsListView.setAdapter(adapter);
        addNewButton.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            fetchContacts();
        }

    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_AddNew) {

            openFileChooser();

        }


    }


    public void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/vnd.ms-excel"});
        startActivityForResult(intent, PICK_FROM_FILE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        String path = "";


        if (data != null) {
            mImageCaptureUri = data.getData();
            String filePath = mImageCaptureUri.getPath();
            try {
                readFromExcel(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readFromExcel(String FilePath) throws IOException {

        File myFile = new File(FilePath);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(myFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
                // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);

        // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = mySheet.iterator();

        // Traversing over each row of XLSX file
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            // For each row, iterate through each columns
            ContactModel model = new ContactModel();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {

                Cell cell = cellIterator.next();
                cell.setCellType(Cell.CELL_TYPE_STRING);


                if (cell.getCellType() == Cell.CELL_TYPE_STRING || cell.getCellType() == Cell.CELL_TYPE_BLANK) {


                    if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                        if (cell.getColumnIndex() == 0) {
                            model.setDisplayName("");
                        } else if (cell.getColumnIndex() == 1) {
                            model.setDisplayEmail("");
                        } else if (cell.getColumnIndex() == 2) {
                            model.setDisplayContact("");
                        }
                    } else {
                        if (cell.getColumnIndex() == 0) {
                            model.setDisplayName(cell.getStringCellValue());
                        } else if (cell.getColumnIndex() == 1) {
                            model.setDisplayEmail(cell.getStringCellValue());
                        } else if (cell.getColumnIndex() == 2) {
                            model.setDisplayContact(cell.getStringCellValue());
                        }
                    }


                }


            }
            System.out.println("");
            modelList.add(model);
        }

        CreateContact createContact = new CreateContact(MainActivity.this);
        createContact.execute(modelList);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                fetchContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchContacts() {


        nameList.clear();
        numberList.clear();

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                if (hasPhoneNumber > 0) {


                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        nameList.add(name);
                        numberList.add(phoneNumber);
                    }
                    phoneCursor.close();
                }
            }
        }

        cursor.close();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

    }


    public class CreateContact extends AsyncTask<ArrayList<ContactModel>, Integer, Void> {
        Context mContext;
        /*ProgressDialog progressDialog;*/


        public CreateContact(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected Void doInBackground(ArrayList<ContactModel>... params) {

            ArrayList<ContactModel> modelList = params[0];

            for (int i = 0; i < modelList.size(); i++) {


                Log.e("NITISH", "Name  " + modelList.get(i).getDisplayName() + "  Email  " + modelList.get(i).getDisplayEmail() + "   Contact   " + modelList.get(i).getDisplayContact());

                String strDisplayName = ""; // Name of the Person to add
                String strNumber = ""; //number of the person to add with the Contact
                String strDisplayEmail = ""; //number of the person to add with the Contact


                if (modelList.get(i).getDisplayName() == null) {
                    strDisplayName = "";
                } else {
                    strDisplayName = modelList.get(i).getDisplayName();
                }

                if (modelList.get(i).getDisplayEmail() == null) {
                    strDisplayEmail = "";
                } else {
                    strDisplayEmail = modelList.get(i).getDisplayEmail();
                }


                if (modelList.get(i).getDisplayContact() == null) {
                    strNumber = "";
                } else {
                    strNumber = modelList.get(i).getDisplayContact();

                }


                ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
                int contactIndex = cntProOper.size();//ContactSize


                //Newly Inserted contact
                // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
                cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)//Step1
                        .withYieldAllowed(true)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

                //Display name will be inserted in ContactsContract.Data table
                cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step2
                        .withYieldAllowed(true)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, strDisplayName) // Name of the contact
                        .build());

                //Display Email will be inserted in ContactsContract.Data table
                cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step2
                        .withYieldAllowed(true)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, strDisplayEmail) // Name of the contact
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME) // Name of the contact
                        .build());

                //Mobile number will be inserted in ContactsContract.Data table
                cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step 3
                        .withYieldAllowed(true)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, strNumber) // Number to be added
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
                try {
                    // We will do batch operation to insert all above data
                    //Contains the output of the app of a ContentProviderOperation.
                    //It is sure to have exactly one of uri or count set
                    ContentProviderResult[] contentProresult = null;


                    contentProresult = mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list
                } catch (RemoteException exp) {
                    //logs;
                } catch (OperationApplicationException exp) {
                    //logs
                }

                publishProgress(i);

            }


            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            mProgressDialog.setProgress(values[0]);


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Creating Contacts .....Please wait !!!");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(modelList.size());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

        }


        protected void onPostExecute(Void res) {
            mProgressDialog.dismiss();
            fetchContacts();
        }
    }


}
