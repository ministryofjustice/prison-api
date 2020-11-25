package uk.gov.justice.hmpps.prison.service.validation;

/*
    Used as the cell move reason when making a cell move through whereabouts.
    Making a cell move through whereabouts results in a case note being created using type: MOVED_CELL and subType: ADM|BEH|CLA|CON|LN|VP

    These case note types don't show up in classic and can't be selected when creating case notes through the dps frontend.

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
