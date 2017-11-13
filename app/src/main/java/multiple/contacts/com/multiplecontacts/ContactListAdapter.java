package multiple.contacts.com.multiplecontacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Nitish Singh on 7/3/17.
 */

public class ContactListAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    Context mContext;
    ArrayList<String> nameList;
    ArrayList<String> numberList;

    public ContactListAdapter(ArrayList<String> nameList, ArrayList<String> numberList, Context context) {
        this.mContext = context;
        this.nameList = nameList;
        this.numberList = numberList;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return numberList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContactsViewHolder contactsViewHolder = new ContactsViewHolder();
        View view = convertView;
        int type = getItemViewType(position);
        if(convertView == null){

            view = inflater.inflate(R.layout.item_contacts, parent, false);
            contactsViewHolder.txtName = (TextView) view.findViewById(R.id.txtName);
            contactsViewHolder.txtNumber = (TextView) view.findViewById(R.id.txtNo);
            view.setTag(contactsViewHolder);

        } else {
            contactsViewHolder = (ContactsViewHolder) view.getTag();
        }

        if(numberList.size() > 0 || nameList.size() > 0 ){

            contactsViewHolder.txtName.setText(nameList.get(position));
            contactsViewHolder.txtNumber.setText(numberList.get(position));

        }


        return view;
    }



    static class ContactsViewHolder {

        TextView txtName;
        TextView txtNumber;
    }
}
