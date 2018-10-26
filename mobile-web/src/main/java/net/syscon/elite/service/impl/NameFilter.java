package net.syscon.elite.service.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class NameFilter {
    private String searchTerm;
    private String firstName;
    private String surname;

    public NameFilter() {
    }

    public NameFilter(String searchTerm) {
        this.searchTerm = searchTerm;
        extractNames(searchTerm);
    }

    private void extractNames(String searchTerm) {
        String[] nameSplit = StringUtils.split(searchTerm, " ");
        if (nameSplit.length > 1) {
            surname = nameSplit[0];
            firstName = nameSplit[1];
        }
    }

    public boolean isFullNameSearch(){
        return surname!=null;
    }

    public boolean isProvided(){
        return StringUtils.isNotBlank(searchTerm);
    }
}
