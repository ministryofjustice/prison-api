package net.syscon.elite.service;

import net.syscon.elite.api.model.AlertSubtype;
import net.syscon.elite.api.model.AlertType;

import java.util.List;

public interface AlertService {
    List<AlertType> getAlertTypes();
    List<AlertSubtype> getAlertSubtypes(String parentCode);
}
