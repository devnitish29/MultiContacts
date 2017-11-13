package multiple.contacts.com.multiplecontacts;

import java.io.Serializable;

/**
 * Created by Nitish Singh on 7/3/17.
 */

public class ContactModel implements Serializable {

    String displayName;
    String displayEmail;
    String displayContact;


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayEmail() {
        return displayEmail;
    }

    public void setDisplayEmail(String displayEmail) {
        this.displayEmail = displayEmail;
    }

    public String getDisplayContact() {
        return displayContact;
    }

    public void setDisplayContact(String displayContact) {
        this.displayContact = displayContact;
    }
}
