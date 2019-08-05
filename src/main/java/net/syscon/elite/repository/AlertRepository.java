package net.syscon.elite.repository;

import net.syscon.elite.api.model.AlertSubtype;
import net.syscon.elite.api.model.AlertType;

import java.util.List;

/**
 * Alert API repository interface.
 */
public interface AlertRepository {

    List<AlertType> getAlertTypes();
    List<AlertSubtype> getAlertSubtypes(String parentCode);
}