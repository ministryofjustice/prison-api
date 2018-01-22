DECLARE
  caseloads web_user_pkg.caseloadType;
  roles web_user_pkg.roleType;
BEGIN
  caseloads(1) := 'LT1';
  roles(1) := 'LICENCE_CA';
  web_user_pkg.create_elite2_web_user('CA_USER','password123456', 'CA', 'CAUSER', 'ca.user@digital.justice.gov.uk', caseloads, roles);

  caseloads(1) := 'LT1';
  roles(1) := 'LICENCE_RO';
  web_user_pkg.create_elite2_web_user('RO_USER','password123456', 'RO', 'ROUSER', 'ro.user@digital.justice.gov.uk', caseloads, roles);

  caseloads(1) := 'LT1';
  roles(1) := 'LICENCE_DM';
  web_user_pkg.create_elite2_web_user('DM_USER','password123456', 'DM', 'DMUSER', 'dm.user@digital.justice.gov.uk', caseloads, roles);

  caseloads(1) := 'LT1';
  roles(1) := 'CENTRAL_ADMIN';
  web_user_pkg.create_elite2_web_user('LICENCE_ADMIN','password123456', 'ADMIN', 'LICENCE_ADMIN', 'admin.user@digital.justice.gov.uk', caseloads, roles);

  caseloads(1) := 'LT2';
  roles(1) := 'LICENCE_CA';
  web_user_pkg.create_elite2_web_user('CA_USER_TEST','password123456', 'CA', 'CAUSER', 'ca.user@digital.justice.gov.uk', caseloads, roles);

  caseloads(1) := 'LT2';
  roles(1) := 'LICENCE_RO';
  web_user_pkg.create_elite2_web_user('RO_USER_TEST','password123456', 'RO', 'ROUSER', 'ro.user@digital.justice.gov.uk', caseloads, roles);

  caseloads(1) := 'LT2';
  roles(1) := 'LICENCE_DM';
  web_user_pkg.create_elite2_web_user('DM_USER_TEST','password123456', 'DM', 'DMUSER', 'dm.user@digital.justice.gov.uk', caseloads, roles);

  caseloads(1) := 'LT2';
  roles(1) := 'CENTRAL_ADMIN';
  web_user_pkg.create_elite2_web_user('LICENCE_ADMIN_TEST','password123456', 'ADMIN', 'LICENCE_ADMIN', 'admin.user@digital.justice.gov.uk', caseloads, roles);

END;
