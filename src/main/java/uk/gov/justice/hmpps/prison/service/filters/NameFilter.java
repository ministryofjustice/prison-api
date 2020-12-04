package uk.gov.justice.hmpps.prison.service.filters;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class NameFilter {
    private final String searchTerm;
    private String firstName;
    private String surname;

    public NameFilter(final String searchTerm) {
        this.searchTerm = StringUtils.remove(StringUtils.upperCase(StringUtils.trimToNull(searchTerm)), ',');
        extractNames(this.searchTerm);
    }

    private void extractNames(final String searchTerm) {
        if (searchTerm != null) {
            final var nameSplit = StringUtils.split(searchTerm, " ");
            if (nameSplit.length > 1) {
                surname = nameSplit[0];
                firstName = nameSplit[1];
            }
        }
    }

    public boolean isFullNameSearch() {
        return surname != null;
    }

    public boolean isProvided() {
        return StringUtils.isNotBlank(searchTerm);
    }
}
