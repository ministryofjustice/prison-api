package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.Contact;
import net.syscon.elite.api.model.OffenderRelationship;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.ContactService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@SpringBootTest
public class ContactRelationshipIntTest {

    private static final long BOOKING1_ID = -1L;
    private static final long BOOKING2_ID = -2L;

    @Autowired
    private ContactService contactService;

    @Autowired
    private BookingService bookingService;

    @Test
    @WithUserDetails("ITAG_USER")
    public void testCreateRelationshipWithMultipleOffendersAndLinkedRelationships() {
        List<Contact> contactList = contactService.getRelationships(BOOKING1_ID, "COM");
        assertThat(contactList).isEmpty();

        Contact relationship = contactService.createRelationship(BOOKING1_ID,
                OffenderRelationship.builder()
                        .firstName("TESTFIRST")
                        .lastName("TESTLAST")
                        .externalRef("EX123")
                        .relationshipType("COM")
                        .build());

        assertThat(relationship).isNotNull();
        assertThat(relationship.getRelationshipId()).isNotNull();
        assertThat(relationship.getPersonId()).isNotNull();

        contactList = contactService.getRelationships(BOOKING1_ID, "COM");

        assertThat(contactList).isNotEmpty();
        assertThat(contactList).hasSize(1);
        assertThat(contactList.get(0).getFirstName()).isEqualTo("TESTFIRST");
        assertThat(contactList.get(0).getLastName()).isEqualTo("TESTLAST");

        List<OffenderSummary> offenders = bookingService.getBookingsByExternalRefAndType("EX123", "COM");
        assertThat(offenders).hasSize(1);
        assertThat(offenders.get(0).getBookingId()).isEqualTo(BOOKING1_ID);


        offenders = bookingService.getBookingsByPersonIdAndType(relationship.getPersonId(), "COM");
        assertThat(offenders).hasSize(1);
        assertThat(offenders.get(0).getBookingId()).isEqualTo(BOOKING1_ID);

        // update relationship
        Contact updatedRelationship = contactService.createRelationship(BOOKING1_ID,
                OffenderRelationship.builder()
                        .firstName("NewFirstName")
                        .lastName("NewLastName")
                        .personId(relationship.getPersonId())
                        .relationshipType("COM")
                        .build());

        assertThat(updatedRelationship.getPersonId()).isEqualTo(relationship.getPersonId());
        assertThat(updatedRelationship.getRelationshipId()).isEqualTo(relationship.getRelationshipId());

        contactList = contactService.getRelationships(BOOKING1_ID, "COM");
        assertThat(contactList.get(0).getFirstName()).isEqualTo("NewFirstName");
        assertThat(contactList.get(0).getLastName()).isEqualTo("NewLastName");

        Contact secondRelationship = contactService.createRelationship(BOOKING2_ID,
                OffenderRelationship.builder()
                        .firstName("SECONDTEST")
                        .lastName("SECONDTESTLAST")
                        .relationshipType("COM")
                        .build());

        Contact updatedSecondRelationship = contactService.createRelationship(BOOKING2_ID,
                OffenderRelationship.builder()
                        .firstName("AnotherFirstName")
                        .lastName("AnotherLastName")
                        .relationshipType("COM")
                        .build());

        contactList = contactService.getRelationships(BOOKING2_ID, "COM");
        assertThat(contactList.get(0).getFirstName()).isEqualTo("AnotherFirstName");
        assertThat(contactList.get(0).getLastName()).isEqualTo("AnotherLastName");

        assertThat(updatedSecondRelationship.getPersonId()).isNotEqualTo(secondRelationship.getPersonId());
        assertThat(updatedSecondRelationship.getRelationshipId()).isEqualTo(secondRelationship.getRelationshipId());

        Contact newUpdatedSecondRelationship = contactService.createRelationship(BOOKING2_ID,
                OffenderRelationship.builder()
                        .firstName("MoreAnotherFirstName")
                        .lastName("MoreAnotherLastName")
                        .externalRef("EX123")
                        .personId(updatedRelationship.getPersonId())
                        .relationshipType("COM")
                        .build());

        assertThat(newUpdatedSecondRelationship.getPersonId()).isEqualTo(relationship.getPersonId());
        assertThat(newUpdatedSecondRelationship.getRelationshipId()).isEqualTo(updatedSecondRelationship.getRelationshipId());

        contactList = contactService.getRelationships(BOOKING2_ID, "COM");
        assertThat(contactList.get(0).getFirstName()).isEqualTo("MoreAnotherFirstName");
        assertThat(contactList.get(0).getLastName()).isEqualTo("MoreAnotherLastName");

        offenders = bookingService.getBookingsByExternalRefAndType("EX123", "COM");
        assertThat(offenders).hasSize(2);
    }
}
