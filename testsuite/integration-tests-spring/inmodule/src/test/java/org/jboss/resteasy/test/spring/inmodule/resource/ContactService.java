package org.jboss.resteasy.test.spring.inmodule.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * Simple Service object. Really, this class isn't needed in this case. However
 * Controller/Service/Repository layering is a pretty common design pattern in
 * Spring projects. While this example doesn't have a Repository/DAO, the
 * ContactService class will show how to integrate Controllers with the layers
 * below in a Spring/RESTEasy application.
 *
 */
@Service
public class ContactService {
    private Map<String, Contact> contactMap = new ConcurrentHashMap<String, Contact>();

    public void save(Contact contact) {
        contactMap.put(contact.getLastName(), contact);
    }

    public Contact getContact(String lastName) {
        return contactMap.get(lastName);
    }

    public Contacts getAll() {
        return new Contacts(contactMap.values());
    }
}
