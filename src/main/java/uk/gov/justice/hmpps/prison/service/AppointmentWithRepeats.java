package uk.gov.justice.hmpps.prison.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@RequiredArgsConstructor(staticName="of")
public class AppointmentWithRepeats {

    private final AppointmentDetails mainAppointment;
    private final List<AppointmentDetails> repeatAppointments;

    public static AppointmentWithRepeats of(AppointmentDetails mainAppointment) {
        return new AppointmentWithRepeats(mainAppointment, Collections.emptyList());
    }

    public Integer getAppointmentCount() {
        return repeatAppointments.size() + 1;
    }

    public List<AppointmentDetails> getAllAppointments() {
        final var allAppointments = new ArrayList<AppointmentDetails>();
        allAppointments.add(mainAppointment);
        allAppointments.addAll(repeatAppointments);
        return Collections.unmodifiableList(allAppointments);
    }
}
