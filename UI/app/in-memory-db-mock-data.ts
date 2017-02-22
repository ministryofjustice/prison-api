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
    		bookingId: '2015-037258',
    		offenderId: '16480',
    		firstName: 'Giovani',
    		middleName: 'Louis',
    		lastName: 'Jenkins',
    		alertCodes: [ 'V', 'R', 'X'],
    		dateOfBirth: '1982-07-21',
    		age: 35,
        currentLocationId: 7018,
        assignedLivingUnitId: 7018,
        facialImageId: 1,
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
        bookingId: '2015-037259',
        offenderId: '39105',
        firstName: 'Alan',
        lastName: 'Goodwin',
        currentLocationId: 7018,
        assignedLivingUnitId: 7018,
      },
    	{ id: 38393, bookingId: '2015-037382', offenderId: '39208', firstName: 'Marquise', lastName: 'Mullen', currentLocationId: 7018, assignedLivingUnitId: 7018, },
    	{ id: 38433, bookingId: '2015-037422', offenderId: '39268', firstName: 'Kendall', lastName: 'Patterson', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38293, bookingId: '2015-037282', offenderId: '39108', firstName: 'Arjun', lastName: 'Davenport', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38294, bookingId: '2015-037283', offenderId: '39110', firstName: 'Rhett', lastName: 'Barrett', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38354, bookingId: '2015-037343', offenderId: '39168', firstName: 'Ryan', lastName: 'Hughes', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38373, bookingId: '2015-037362', offenderId: '39188', firstName: 'Lorenzo', lastName: 'Palmer', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38415, bookingId: '2015-037404', offenderId: '39248', firstName: 'Maxwell', lastName: 'Larsen', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38414, bookingId: '2015-037403', offenderId: '5671', firstName: 'Houston', lastName: 'Long', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38272, bookingId: '2015-037262', offenderId: '13179', firstName: 'Uriel', lastName: 'Brandt', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38333, bookingId: '2015-037322', offenderId: '39148', firstName: 'Matteo', lastName: 'Watson', currentLocationId: 7018, assignedLivingUnitId: 7018,  },
    	{ id: 38271, bookingId: '2015-037261', offenderId: '39107', firstName: 'Regan', lastName: 'West',  currentLocationId: 7032, assignedLivingUnitId: 7032, },
    	{ id: 38270, bookingId: '2015-037260', offenderId: '39106', firstName: 'Summer', lastName: 'Thompson', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38553, bookingId: '2016-037542', offenderId: '39448', firstName: 'Andres', lastName: 'Howard', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38573, bookingId: '2016-037562', offenderId: '5600', firstName: 'Gilberto', lastName: 'Mckee', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38458, bookingId: '2016-037447', offenderId: '39315', firstName: 'Vance', lastName: 'Allen', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38459, bookingId: '2016-037448', offenderId: '39324', firstName: 'Miguel', lastName: 'Terry', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38533, bookingId: '2016-037522', offenderId: '39428', firstName: 'Rolando', lastName: 'Proctor', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38455, bookingId: '2016-037444', offenderId: '20335', firstName: 'Karla', lastName: 'Solis', currentLocationId: 7032, assignedLivingUnitId: 7032,  },
    	{ id: 38613, bookingId: '2016-037602', offenderId: '39488', firstName: 'Shyla', lastName: 'Mcknight', currentLocationId: 8724, assignedLivingUnitId: 8724,  },
    	{ id: 38753, bookingId: '2016-037742', offenderId: '39668', firstName: 'Camren', lastName: 'Gibbs', currentLocationId: 8212,  },
    	{ id: 38633, bookingId: '2016-037622', offenderId: '39528', firstName: 'Isiah', lastName: 'Burton', currentLocationId: 8212,  },
    	{ id: 38713, bookingId: '2016-037702', offenderId: '23527', firstName: 'Makhi', lastName: 'Richard', currentLocationId: 8212,  },
    	{ id: 38733, bookingId: '2016-037722', offenderId: '39628', firstName: 'Bryant', lastName: 'Glass', currentLocationId: 8212,  },
    	{ id: 38457, bookingId: '2016-037446', offenderId: '39322', firstName: 'Gilbert', lastName: 'Reilly', currentLocationId: 8293,  },
    	{ id: 38460, bookingId: '2016-037449', offenderId: '39325', firstName: 'Dylan', lastName: 'Butler', currentLocationId: 8293,  },
    	{ id: 38473, bookingId: '2016-037462', offenderId: '39328', firstName: 'Maximilian', lastName: 'Hampton', currentLocationId: 8293,  },
    	{ id: 38495, bookingId: '2016-037484', offenderId: '1234', firstName: 'Noe', lastName: 'Rowe', currentLocationId: 8293,  },
    	{ id: 38496, bookingId: '2016-037485', offenderId: '8521', firstName: 'Lizeth', lastName: 'Bartlett', currentLocationId: 8292,  },
    	{ id: 38653, bookingId: '2016-037642', offenderId: '16648', firstName: 'Javon', lastName: 'Hood', currentLocationId: 8292,  },
    	{ id: 38693, bookingId: '2016-037682', offenderId: '39588', firstName: 'Jasmin', lastName: 'Parrish', currentLocationId: 8292,  },
    	{ id: 38513, bookingId: '2016-037502', offenderId: '39408', firstName: 'Clarence', lastName: 'Rivers', currentLocationId: 8203,  },
    	{ id: 38673, bookingId: '2016-037662', offenderId: '39568', firstName: 'Madeline', lastName: 'Thomas', currentLocationId: 8203,  },
    	{ id: 38674, bookingId: '2016-037663', offenderId: '39569', firstName: 'Gilberto', lastName: 'Strong', currentLocationId: 8203,  },
    	{ id: 38493, bookingId: '2016-037482', offenderId: '39508', firstName: 'Jude', lastName: 'Elliott', currentLocationId: 8203,  },
    	{ id: 38494, bookingId: '2016-037483', offenderId: '13851', firstName: 'Rhett', lastName: 'Mccoy', currentLocationId: 8203,  },
    	{ id: 38593, bookingId: '2016-037582', offenderId: '39468', firstName: 'Demarion', lastName: 'Parks', currentLocationId: 8203,  },
    	{ id: 38594, bookingId: '2016-037583', offenderId: '39469', firstName: 'Lawrence', lastName: 'Esparza', currentLocationId: 8203,  },
    	{ id: 38595, bookingId: '2016-037584', offenderId: '39470', firstName: 'Ricky', lastName: 'Marsh', currentLocationId: 8203,  },
    ];

    let locations = [
      { id: 8210, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL', currentOccupancy: 0, livingUnit: false,  },
      { id: 8211, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL-LIBRARY', parentLocationId: 8210, currentOccupancy: 0, livingUnit: false,  },
      { id: 8212, agencyId: 'SDP', locationType: 'CLAS', description: 'SDP-SCHOOL-CLASSRM', parentLocationId: 8210, currentOccupancy: 0, livingUnit: false,  },
      { id: 8294, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-CRAFT RM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8295, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-BATHROOM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8296, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-CONTROL RM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8290, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-HALLWAY', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8291, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-WEIGHT RM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8292, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-MUSIC', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      { id: 8293, agencyId: 'SDP', locationType: 'EXER', description: 'SDP-REC BUILDING-GYM', parentLocationId: 8289, currentOccupancy: 0, livingUnit: false,  },
      {
        id: 8724,
        agencyId: 'SDP',
        locationType: 'TIER',
        description: 'SDP-PEN-DEATH ROW',
        parentLocationId: 8723,
        operationalCapacity: 1,
        currentOccupancy: 1,
        livingUnit: true,
        housingUnitType: 'DR',
        inmateActiveCountStatus: 'Counted',
      },
      { id: 7018, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 1', parentLocationId: 6921, operationalCapacity: 26, currentOccupancy: 19, livingUnit: true, housingUnitType: 'SPEC NEEDS', inmateActiveCountStatus: 'Recount', },
      {
        id: 7032,
        agencyId: 'SDP',
        locationType: 'TIER',
        description: 'SDP-WEST-SOUTH 2',
        parentLocationId: 6921,
        operationalCapacity: 34,
        currentOccupancy: 26,
        livingUnit: true,
        housingUnitType: 'SPEC NEEDS',
        inmateActiveCountStatus: 'NotCounted',
      },
      { id: 7110, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-UPPER EAST', parentLocationId: 7099, operationalCapacity: 18, currentOccupancy: 10, livingUnit: true, housingUnitType: 'DISG SEG',  },
      { id: 7120, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-LOW WEST', parentLocationId: 7099, operationalCapacity: 18, currentOccupancy: 13, livingUnit: true, housingUnitType: 'DISG SEG',  },
      { id: 7130, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-UPPER WEST', parentLocationId: 7099, operationalCapacity: 22, currentOccupancy: 14, livingUnit: true, housingUnitType: 'DISG SEG',  },
      { id: 6341, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 1', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 49, livingUnit: true, housingUnitType: 'GP',  },
      { id: 11734, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-TMPA-TMPA', parentLocationId: 11733, operationalCapacity: 0, currentOccupancy: 9, livingUnit: true, housingUnitType: 'GEN',  },
      { id: 7061, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 3', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 31, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6651, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 3', parentLocationId: 5001, operationalCapacity: 20, currentOccupancy: 18, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6662, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 4', parentLocationId: 5001, operationalCapacity: 20, currentOccupancy: 20, livingUnit: true, housingUnitType: 'GP',  },
      { id: 7080, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-SOUTH 4', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 27, livingUnit: true, housingUnitType: 'GP',  },
      { id: 7100, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SHU-LOW EAST', parentLocationId: 7099, operationalCapacity: 9, currentOccupancy: 8, livingUnit: true, housingUnitType: 'DISG SEG',  },
      { id: 6999, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 4', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 30, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6362, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 2', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 38, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6383, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 3', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 39, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6624, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 2', parentLocationId: 5001, operationalCapacity: 20, currentOccupancy: 20, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6600, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 5', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 38, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6673, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 5', parentLocationId: 5001, operationalCapacity: 20, currentOccupancy: 17, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6684, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 1', parentLocationId: 6483, operationalCapacity: 4, currentOccupancy: 4, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6689, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 2', parentLocationId: 6483, operationalCapacity: 20, currentOccupancy: 18, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6421, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 4', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 40, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6442, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-EAST 5', parentLocationId: 5001, operationalCapacity: 40, currentOccupancy: 38, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6484, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 1', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 35, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6505, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 2', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 26, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6526, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 3', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 39, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6547, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-EAST 4', parentLocationId: 6483, operationalCapacity: 40, currentOccupancy: 37, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6700, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 3', parentLocationId: 6483, operationalCapacity: 20, currentOccupancy: 18, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6711, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 4', parentLocationId: 6483, operationalCapacity: 20, currentOccupancy: 15, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6722, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-SOUTH-FEDERAL 5', parentLocationId: 6483, operationalCapacity: 20, currentOccupancy: 13, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6482, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-NORTH-FEDERAL 1', parentLocationId: 5001, operationalCapacity: 8, currentOccupancy: 9, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6947, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 1', parentLocationId: 6921, operationalCapacity: 25, currentOccupancy: 22, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6961, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 2', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 30, livingUnit: true, housingUnitType: 'GP',  },
      { id: 6980, agencyId: 'SDP', locationType: 'TIER', description: 'SDP-WEST-NORTH 3', parentLocationId: 6921, operationalCapacity: 36, currentOccupancy: 29, livingUnit: true, housingUnitType: 'GP',  },
      { id: 10148, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-VISIT_ROOM_HOLIDAY', currentOccupancy: 0, livingUnit: false,  },
      { id: 10151, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CLASS_II_HOLIDAY', currentOccupancy: 0, livingUnit: false,  },
      { id: 11562, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CONV1', currentOccupancy: 0, livingUnit: false,  },
      { id: 8381, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-CLASS_II_VISIT', currentOccupancy: 0, livingUnit: false,  },
      { id: 8207, agencyId: 'SDP', locationType: 'VISIT', description: 'SDP-VISIT_ROOM', currentOccupancy: 0, livingUnit: false,  },
      { id: 8203, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-KITCHEN', currentOccupancy: 0, livingUnit: false,  },
      { id: 8209, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-LAUNDRY', currentOccupancy: 0, livingUnit: false,  },
      { id: 8281, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-LICENSE_PLATES', currentOccupancy: 0, livingUnit: false,  },
      { id: 8282, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-BRAILLE', currentOccupancy: 0, livingUnit: false,  },
      { id: 8283, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-CARPENTRY', currentOccupancy: 0, livingUnit: false,  },
      { id: 8284, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-PRINT_SHOP', currentOccupancy: 0, livingUnit: false,  },
      { id: 8285, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-DOT_SIGN_SHOP', currentOccupancy: 0, livingUnit: false,  },
      { id: 8286, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-MACHINE_SHOP', currentOccupancy: 0, livingUnit: false,  },
      { id: 8287, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-WHEELCHAIR', currentOccupancy: 0, livingUnit: false,  },
      { id: 8288, agencyId: 'SDP', locationType: 'WORK', description: 'SDP-MCI', currentOccupancy: 0, livingUnit: false,  },
    ];

    let images = [
      {
        id:1,
        data: 'data:image/jpeg;base64,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'}
    ];

    return {inmates, locations, counts, agencies, images};
  }
}
