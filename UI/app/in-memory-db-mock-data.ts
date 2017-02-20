import { InMemoryDbService } from 'angular-in-memory-web-api';
export class InMemoryDBMockData implements InMemoryDbService {
  createDb() {

    let agencies = [
      { id: 1, agencyId: 'SDP', description: 'Sam Dennison Prison', agencyType: 'INST'},
      { id: 2, agencyId: 'JSP', description: 'Jefferson Smith Pretrial', agencyType: 'INST'}
    ];

    let counts = [
      {
        id: 0,
        locationId: 0,
        status: 'Completed',
        initialCount: {
          conductUserId: '',
          countReason: '',
          count: 0
        }
      }
    ];

    let inmates = [
    	{
    		id: 38268,
        inmateId: 38268,
    		bookingNo: '2015-037258',
    		offenderNo: '16480',
    		firstName: 'Giovani',
    		middleName: 'Louis',
    		lastName: 'Jenkins',
    		alertCodes: [ 'V', 'R', 'X'],
    		dateOfBirth: '1982-07-21',
    		age: 35,
        currentLocationId: 7018,
        assignedLivingUnitId: 7018,
    		physicalAttributes: {
    			gender: 'M',
    			ethnicity: 'BLA',
    			heightInches: 74,
    			heightMeters: 1.87,
    			weightPounds: 198,
    			weightKg: 90
    		},
    		physicalCharacteristics: [
    			{
    				characteristic: 'Eye Colour',
    				detail: 'Brown'
    			},
    			{
    				characteristic: 'Hair Colour',
    				detail: 'Grey'
    			}
    		],
    		physicalMarks: [
    			{
    				type: 'tatoo',
    				side: 'right',
    				bodyPart: 'upper arm',
    				orientation: 'front',
    				comment: 'eagle in flight'
    			},
    			{
    				type: 'scar',
    				side: 'left',
    				bodyPart: 'lower leg',
    				orientation: 'back',
    				comment: 'across calf mid leg'
    			}
    		]
    	},
    	{
        id: 38269,
        inmateId: 38269,
        bookingNo: '2015-037259',
        offenderNo: '39105',
        firstName: 'Alan',
        lastName: 'Goodwin',
        currentLocationId: 7018,
        assignedLivingUnitId: 7018,
      },
    	{ id: 38393, inmateId: 38393, bookingNo: '2015-037382', offenderNo: '39208', firstName: 'Marquise', lastName: 'Mullen', currentLocationId: 7018, assignedLivingUnitId: 7018, },
    	{ id: 38433, inmateId: 38433, bookingNo: '2015-037422', offenderNo: '39268', firstName: 'Kendall', lastName: 'Patterson', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38293, inmateId: 38293, bookingNo: '2015-037282', offenderNo: '39108', firstName: 'Arjun', lastName: 'Davenport', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38294, inmateId: 38294, bookingNo: '2015-037283', offenderNo: '39110', firstName: 'Rhett', lastName: 'Barrett', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38354, inmateId: 38354, bookingNo: '2015-037343', offenderNo: '39168', firstName: 'Ryan', lastName: 'Hughes', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38373, inmateId: 38373, bookingNo: '2015-037362', offenderNo: '39188', firstName: 'Lorenzo', lastName: 'Palmer', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38415, inmateId: 38415, bookingNo: '2015-037404', offenderNo: '39248', firstName: 'Maxwell', lastName: 'Larsen', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38414, inmateId: 38414, bookingNo: '2015-037403', offenderNo: '5671', firstName: 'Houston', lastName: 'Long', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38272, inmateId: 38272, bookingNo: '2015-037262', offenderNo: '13179', firstName: 'Uriel', lastName: 'Brandt', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38333, inmateId: 38333, bookingNo: '2015-037322', offenderNo: '39148', firstName: 'Matteo', lastName: 'Watson', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38271, inmateId: 38271, bookingNo: '2015-037261', offenderNo: '39107', firstName: 'Regan', lastName: 'West',  currentLocationId: 7032, assignedLivingUnitId: 7032, },
    	{ id: 38270, inmateId: 38270, bookingNo: '2015-037260', offenderNo: '39106', firstName: 'Summer', lastName: 'Thompson', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38553, inmateId: 38553, bookingNo: '2016-037542', offenderNo: '39448', firstName: 'Andres', lastName: 'Howard', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38573, inmateId: 38573, bookingNo: '2016-037562', offenderNo: '5600', firstName: 'Gilberto', lastName: 'Mckee', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38458, inmateId: 38458, bookingNo: '2016-037447', offenderNo: '39315', firstName: 'Vance', lastName: 'Allen', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38459, inmateId: 38459, bookingNo: '2016-037448', offenderNo: '39324', firstName: 'Miguel', lastName: 'Terry', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38533, inmateId: 38533, bookingNo: '2016-037522', offenderNo: '39428', firstName: 'Rolando', lastName: 'Proctor', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38455, inmateId: 38455, bookingNo: '2016-037444', offenderNo: '20335', firstName: 'Karla', lastName: 'Solis', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38613, inmateId: 38613, bookingNo: '2016-037602', offenderNo: '39488', firstName: 'Shyla', lastName: 'Mcknight', currentLocationId: 8724, assignedLivingUnitId: 8724,  },
    	{ id: 38753, inmateId: 38753, bookingNo: '2016-037742', offenderNo: '39668', firstName: 'Camren', lastName: 'Gibbs', currentLocationId: 8212,  },
    	{ id: 38633, inmateId: 38633, bookingNo: '2016-037622', offenderNo: '39528', firstName: 'Isiah', lastName: 'Burton', currentLocationId: 8212,  },
    	{ id: 38713, inmateId: 38713, bookingNo: '2016-037702', offenderNo: '23527', firstName: 'Makhi', lastName: 'Richard', currentLocationId: 8212,  },
    	{ id: 38733, inmateId: 38733, bookingNo: '2016-037722', offenderNo: '39628', firstName: 'Bryant', lastName: 'Glass', currentLocationId: 8212,  },
    	{ id: 38457, inmateId: 38457, bookingNo: '2016-037446', offenderNo: '39322', firstName: 'Gilbert', lastName: 'Reilly', currentLocationId: 8293,  },
    	{ id: 38460, inmateId: 38460, bookingNo: '2016-037449', offenderNo: '39325', firstName: 'Dylan', lastName: 'Butler', currentLocationId: 8293,  },
    	{ id: 38473, inmateId: 38473, bookingNo: '2016-037462', offenderNo: '39328', firstName: 'Maximilian', lastName: 'Hampton', currentLocationId: 8293,  },
    	{ id: 38495, inmateId: 38495, bookingNo: '2016-037484', offenderNo: '1234', firstName: 'Noe', lastName: 'Rowe', currentLocationId: 8293,  },
    	{ id: 38496, inmateId: 38496, bookingNo: '2016-037485', offenderNo: '8521', firstName: 'Lizeth', lastName: 'Bartlett', currentLocationId: 8292,  },
    	{ id: 38653, inmateId: 38653, bookingNo: '2016-037642', offenderNo: '16648', firstName: 'Javon', lastName: 'Hood', currentLocationId: 8292,  },
    	{ id: 38693, inmateId: 38693, bookingNo: '2016-037682', offenderNo: '39588', firstName: 'Jasmin', lastName: 'Parrish', currentLocationId: 8292,  },
    	{ id: 38513, inmateId: 38513, bookingNo: '2016-037502', offenderNo: '39408', firstName: 'Clarence', lastName: 'Rivers', currentLocationId: 8203,  },
    	{ id: 38673, inmateId: 38673, bookingNo: '2016-037662', offenderNo: '39568', firstName: 'Madeline', lastName: 'Thomas', currentLocationId: 8203,  },
    	{ id: 38674, inmateId: 38674, bookingNo: '2016-037663', offenderNo: '39569', firstName: 'Gilberto', lastName: 'Strong', currentLocationId: 8203,  },
    	{ id: 38493, inmateId: 38493, bookingNo: '2016-037482', offenderNo: '39508', firstName: 'Jude', lastName: 'Elliott', currentLocationId: 8203,  },
    	{ id: 38494, inmateId: 38494, bookingNo: '2016-037483', offenderNo: '13851', firstName: 'Rhett', lastName: 'Mccoy', currentLocationId: 8203,  },
    	{ id: 38593, inmateId: 38593, bookingNo: '2016-037582', offenderNo: '39468', firstName: 'Demarion', lastName: 'Parks', currentLocationId: 8203,  },
    	{ id: 38594, inmateId: 38594, bookingNo: '2016-037583', offenderNo: '39469', firstName: 'Lawrence', lastName: 'Esparza', currentLocationId: 8203,  },
    	{ id: 38595, inmateId: 38595, bookingNo: '2016-037584', offenderNo: '39470', firstName: 'Ricky', lastName: 'Marsh', currentLocationId: 8203,  },
    ];

    let locations = [
      { id: 8210, locationId: 8210, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL', currentOccupancy: 0, livingUnit: false,  },
      { id: 8211, locationId: 8211, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL-LIBRARY', parentLocationId: 8210, currentOccupancy: 0, livingUnit: false,  },
      { id: 8212, locationId: 8212, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL-CLASSRM', parentLocationId: 8210, currentOccupancy: 0, livingUnit: false,  },
      { id: 8294, locationId: 8294, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-CRAFT RM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8295, locationId: 8295, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-BATHROOM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8296, locationId: 8296, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-CONTROL RM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8290, locationId: 8290, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-HALLWAY', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8291, locationId: 8291, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-WEIGHT RM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8292, locationId: 8292, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-MUSIC', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8293, locationId: 8293, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-GYM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      {
        id: 8724,
        locationId: 8724,
        agencyId: 'SDP',
        locationType: 'TIER',
        description: 'SDP-PEN-DEATH ROW',
        parentLocationId: 8723,
        operationalCapacity: 1,
        currentOccupancy: 1,
        livingUnit: true,
        housingUnitType: 'DR',
      },
      { id: 7018, locationId: 7018, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 1', parentLocationId: 6921, operationalCapacity: 26, currentOccupancy: 19, livingUnit: true, housingUnitType: 'SPEC NEEDS',  },
      {
        id: 7032,
        locationId: 7032,
        agencyId: 'SDP',
        locationType: 'TIER',
        description: 'SDP-WEST-SOUTH 2',
        parentLocationId: 6921,
        operationalCapacity: 34,
        currentOccupancy: 26,
        livingUnit: true,
        housingUnitType: 'SPEC NEEDS',
      },
      { id: 7110, locationId: 7110, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-UPPER EAST', parentLocationId: 7099, operationalCapacity: 18, currentOccupancy: 10, livingUnit: true, housingUnitType: 'DISG SEG',  },
      { id: 7120, locationId: 7120, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-LOW WEST', parentLocationId: 7099, operationalCapacity: 18, currentOccupancy: 13, livingUnit: true, housingUnitType: 'DISG SEG',  },
      { id: 7130, locationId: 7130, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-UPPER WEST', parentLocationId: 7099, operationalCapacity: 22, currentOccupancy: 14, livingUnit: true, housingUnitType: 'DISG SEG',  },
      { id: 6341, locationId: 6341, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 1', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 49, livingUnit: true, housingUnitType: 'GP',  },
      { id: 11734, locationId: 11734, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-TMPA-TMPA', parentLocationId: 11733, operationalCapacity: 0, currentOccupancy: 9, livingUnit: true, housingUnitType: 'GEN',  },
      { id: 7061, locationId: 7061, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 3', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 31, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6651, locationId: 6651, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 3', parentLocationId: 5001, operationalCapacity: 20, currentOccupancy: 18, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6662, locationId: 6662, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 4', parentLocationId: 5001, operationalCapacity: 20, currentOccupancy: 20, livingUnit: true, housingUnitType: 'GP',  },
      { id: 7080, locationId: 7080, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 4', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 27, livingUnit: true, housingUnitType: 'GP',  },
      { id: 7100, locationId: 7100, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-LOW EAST', parentLocationId: 7099, operationalCapacity: 9, currentOccupancy: 8, livingUnit: true, housingUnitType: 'DISG SEG',  },
      { id: 6999, locationId: 6999, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 4', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 30, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6362, locationId: 6362, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 2', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 38, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6383, locationId: 6383, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 3', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 39, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6624, locationId: 6624, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 2', parentLocationId: 5001, operationalCapacity: 20, currentOccupancy: 20, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6600, locationId: 6600, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 5', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 38, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6673, locationId: 6673, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 5', parentLocationId: 5001, operationalCapacity: 20, currentOccupancy: 17, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6684, locationId: 6684, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 1', parentLocationId: 6483, operationalCapacity: 4, currentOccupancy: 4, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6689, locationId: 6689, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 2', parentLocationId: 6483, operationalCapacity: 20, currentOccupancy: 18, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6421, locationId: 6421, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 4', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 40, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6442, locationId: 6442, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 5', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 38, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6484, locationId: 6484, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 1', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 35, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6505, locationId: 6505, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 2', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 26, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6526, locationId: 6526, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 3', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 39, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6547, locationId: 6547, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 4', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 37, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6700, locationId: 6700, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 3', parentLocationId: 6483, operationalCapacity: 20, currentOccupancy: 18, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6711, locationId: 6711, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 4', parentLocationId: 6483, operationalCapacity: 20, currentOccupancy: 15, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6722, locationId: 6722, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 5', parentLocationId: 6483, operationalCapacity: 20, currentOccupancy: 13, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6482, locationId: 6482, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 1', parentLocationId: 5001, operationalCapacity: 8, currentOccupancy: 9, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6947, locationId: 6947, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 1', parentLocationId: 6921, operationalCapacity: 25, currentOccupancy: 22, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6961, locationId: 6961, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 2', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 30, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6980, locationId: 6980, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 3', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 29, livingUnit: true, housingUnitType: 'GP',  },
      { id: 10148, locationId: 10148, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-VISIT_ROOM_HOLIDAY', currentOccupancy: 0, livingUnit: false,  },
      { id: 10151, locationId: 10151, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CLASS_II_HOLIDAY', currentOccupancy: 0, livingUnit: false,  },
      { id: 11562, locationId: 11562, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CONV1', currentOccupancy: 0, livingUnit: false,  },
      { id: 8381, locationId: 8381, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CLASS_II_VISIT', currentOccupancy: 0, livingUnit: false,  },
      { id: 8207, locationId: 8207, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-VISIT_ROOM', currentOccupancy: 0, livingUnit: false,  },
      { id: 8203, locationId: 8203, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-KITCHEN', currentOccupancy: 0, livingUnit: false,  },
      { id: 8209, locationId: 8209, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-LAUNDRY', currentOccupancy: 0, livingUnit: false,  },
      { id: 8281, locationId: 8281, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-LICENSE_PLATES', currentOccupancy: 0, livingUnit: false,  },
      { id: 8282, locationId: 8282, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-BRAILLE', currentOccupancy: 0, livingUnit: false,  },
      { id: 8283, locationId: 8283, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-CARPENTRY', currentOccupancy: 0, livingUnit: false,  },
      { id: 8284, locationId: 8284, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-PRINT_SHOP', currentOccupancy: 0, livingUnit: false,  },
      { id: 8285, locationId: 8285, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-DOT_SIGN_SHOP', currentOccupancy: 0, livingUnit: false,  },
      { id: 8286, locationId: 8286, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-MACHINE_SHOP', currentOccupancy: 0, livingUnit: false,  },
      { id: 8287, locationId: 8287, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-WHEELCHAIR', currentOccupancy: 0, livingUnit: false,  },
      { id: 8288, locationId: 8288, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-MCI', currentOccupancy: 0, livingUnit: false,  },
    ];

    return {inmates, locations, counts, agencies};
  }
}
