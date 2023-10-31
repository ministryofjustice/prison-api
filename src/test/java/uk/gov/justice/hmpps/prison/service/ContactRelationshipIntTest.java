package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Contact;
import uk.gov.justice.hmpps.prison.api.model.OffenderRelationship;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ContactRelationshipIntTest {

    private static final long BOOKING1_ID = -1L;
    private static final long BOOKING2_ID = -2L;

    @Autowired
    private ContactService contactService;

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"CONTACT_CREATE"})
    public void testCreateRelationshipWithMultipleOffendersAndLinkedRelationships() {
        var contactList = contactService.getRelationships(BOOKING1_ID, "COM", false);
        assertThat(contactList).hasSize(4);

        final var relationship = contactService.createRelationship(BOOKING1_ID,
                OffenderRelationship.builder()
                        .firstName("TESTFIRST")
                        .lastName("TESTLAST")
                        .externalRef("EX123")
                        .relationshipType("COM")
                        .build());

        assertThat(relationship).isNotNull();
        assertThat(relationship.getRelationshipId()).isNotNull();
        assertThat(relationship.getPersonId()).isNotNull();

        contactList = contactService.getRelationships(BOOKING1_ID, "COM", false);
        assertThat(contactList).hasSize(5);

        contactList = contactService.getRelationships(BOOKING1_ID, "COM", true);

        assertThat(contactList).isNotEmpty();
        assertThat(contactList).hasSize(1);
        assertThat(contactList.get(0).getFirstName()).isEqualTo("TESTFIRST");
        assertThat(contactList.get(0).getLastName()).isEqualTo("TESTLAST");

        // update relationship
        final var updatedRelationship = contactService.createRelationship(BOOKING1_ID,
                OffenderRelationship.builder()
                        .firstName("NewFirstName")
                        .lastName("NewLastName")
                        .personId(relationship.getPersonId())
                        .relationshipType("COM")
                        .build());

        assertThat(updatedRelationship.getPersonId()).isEqualTo(relationship.getPersonId());
        assertThat(updatedRelationship.getRelationshipId()).isEqualTo(relationship.getRelationshipId());

        contactList = contactService.getRelationships(BOOKING1_ID, "COM", true);
        assertThat(contactList.get(0).getFirstName()).isEqualTo("NewFirstName");
        assertThat(contactList.get(0).getLastName()).isEqualTo("NewLastName");

        final var secondRelationship = contactService.createRelationship(BOOKING2_ID,
                OffenderRelationship.builder()
                        .firstName("SECONDTEST")
                        .lastName("SECONDTESTLAST")
                        .relationshipType("COM")
                        .build());

        final var updatedSecondRelationship = contactService.createRelationship(BOOKING2_ID,
                OffenderRelationship.builder()
                        .firstName("AnotherFirstName")
                        .lastName("AnotherLastName")
                        .relationshipType("COM")
                        .build());

        contactList = contactService.getRelationships(BOOKING2_ID, "COM", true);
        assertThat(contactList.get(0).getFirstName()).isEqualTo("AnotherFirstName");
        assertThat(contactList.get(0).getLastName()).isEqualTo("AnotherLastName");

        assertThat(updatedSecondRelationship.getPersonId()).isNotEqualTo(secondRelationship.getPersonId());
        assertThat(updatedSecondRelationship.getRelationshipId()).isNotEqualTo(secondRelationship.getRelationshipId());

        final var newUpdatedSecondRelationship = contactService.createRelationship(BOOKING2_ID,
                OffenderRelationship.builder()
                        .firstName("MoreAnotherFirstName")
                        .lastName("MoreAnotherLastName")
                        .externalRef("EX123")
                        .personId(updatedRelationship.getPersonId())
                        .relationshipType("COM")
                        .build());

        assertThat(newUpdatedSecondRelationship.getPersonId()).isEqualTo(relationship.getPersonId());
        assertThat(newUpdatedSecondRelationship.getRelationshipId()).isNotEqualTo(updatedSecondRelationship.getRelationshipId());

        contactList = contactService.getRelationships(BOOKING2_ID, "COM", true);
        assertThat(contactList.get(0).getFirstName()).isEqualTo("MoreAnotherFirstName");
        assertThat(contactList.get(0).getLastName()).isEqualTo("MoreAnotherLastName");

        final var makeInactiveActiveRel = contactService.createRelationship(BOOKING2_ID,
                OffenderRelationship.builder()
                        .firstName("AnotherFirstName")
                        .lastName("AnotherLastName")
                        .personId(updatedSecondRelationship.getPersonId())
                        .relationshipType("COM")
                        .build());

        final var allRelationships = contactService.getRelationships(BOOKING2_ID, "COM", false);
        assertThat(allRelationships).hasSize(3);
        assertThat(allRelationships.stream().filter(Contact::isActiveFlag).count()).isEqualTo(1);
        assertThat(allRelationships.stream().filter(c -> c.getRelationshipId().equals(makeInactiveActiveRel.getRelationshipId())).count()).isEqualTo(1);
    }
}
