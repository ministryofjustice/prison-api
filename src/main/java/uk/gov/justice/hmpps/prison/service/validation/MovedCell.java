package uk.gov.justice.hmpps.prison.service.validation;

/*
    Used to create a case note when making a cell move through whereabouts.
      ADM - Administrative
      BEH - Behaviour
      CLA - Classification or re-classification
      CON - Conflict with other prisoners
      LN -  Local needs
      VP - Vulnerable prisoner
*/
public enum MovedCell {
    ADM, BEH, CLA, CON, LN, VP
}
