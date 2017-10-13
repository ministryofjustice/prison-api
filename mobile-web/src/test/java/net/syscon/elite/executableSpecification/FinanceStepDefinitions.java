package net.syscon.elite.executableSpecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.ReferenceDomainsSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for finance endpoints:
 * <ul>
 * <li>/prison/{prison_id}/offenders/{noms_id}/accounts</li>
 * /bookings/{booking_id}/balances
 * 
 * noms_id string (required) Example: A1404AE
prison_id  string (required) Example: BMI

agencyid = prison, on offender_bookings, has agency id

returned (eg):

{
  "spends": 5343,
  "cash": 0,
  "savings": 0
}
 * </ul>
 */
public class FinanceStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private ReferenceDomainsSteps referenceDomainsSteps;

 

}
