import { Inmate } from './inmate';

export const INMATES: Inmate[] = [
	{
		inmateId: 38268,
		bookingId: '2015-037258',
		offenderId: '16480',
		firstName: 'Giovani',
		middleName: 'Louis',
		lastName: 'Jenkins',
		alertCodes: [ 'V', 'R', 'X'],
		dateOfBirth: '1982-07-21',
		age: 35,
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
	{ inmateId: 38269, bookingId: '2015-037259', offenderId: '39105', firstName: 'Alan', lastName: 'Goodwin',  },
	{ inmateId: 38393, bookingId: '2015-037382', offenderId: '39208', firstName: 'Marquise', lastName: 'Mullen',  },
	{ inmateId: 38433, bookingId: '2015-037422', offenderId: '39268', firstName: 'Kendall', lastName: 'Patterson',  },
	{ inmateId: 38293, bookingId: '2015-037282', offenderId: '39108', firstName: 'Arjun', lastName: 'Davenport',  },
	{ inmateId: 38294, bookingId: '2015-037283', offenderId: '39110', firstName: 'Rhett', lastName: 'Barrett',  },
	{ inmateId: 38354, bookingId: '2015-037343', offenderId: '39168', firstName: 'Ryan', lastName: 'Hughes',  },
	{ inmateId: 38373, bookingId: '2015-037362', offenderId: '39188', firstName: 'Lorenzo', lastName: 'Palmer',  },
	{ inmateId: 38415, bookingId: '2015-037404', offenderId: '39248', firstName: 'Maxwell', lastName: 'Larsen',  },
	{ inmateId: 38414, bookingId: '2015-037403', offenderId: '5671', firstName: 'Houston', lastName: 'Long',  },
	{ inmateId: 38272, bookingId: '2015-037262', offenderId: '13179', firstName: 'Uriel', lastName: 'Brandt',  },
	{ inmateId: 38333, bookingId: '2015-037322', offenderId: '39148', firstName: 'Matteo', lastName: 'Watson',  },
	{ inmateId: 38271, bookingId: '2015-037261', offenderId: '39107', firstName: 'Regan', lastName: 'West',  },
	{ inmateId: 38270, bookingId: '2015-037260', offenderId: '39106', firstName: 'Summer', lastName: 'Thompson',  },
	{ inmateId: 38553, bookingId: '2016-037542', offenderId: '39448', firstName: 'Andres', lastName: 'Howard',  },
	{ inmateId: 38573, bookingId: '2016-037562', offenderId: '5600', firstName: 'Gilberto', lastName: 'Mckee',  },
	{ inmateId: 38458, bookingId: '2016-037447', offenderId: '39315', firstName: 'Vance', lastName: 'Allen',  },
	{ inmateId: 38459, bookingId: '2016-037448', offenderId: '39324', firstName: 'Miguel', lastName: 'Terry',  },
	{ inmateId: 38533, bookingId: '2016-037522', offenderId: '39428', firstName: 'Rolando', lastName: 'Proctor',  },
	{ inmateId: 38455, bookingId: '2016-037444', offenderId: '20335', firstName: 'Karla', lastName: 'Solis',  },
	{ inmateId: 38613, bookingId: '2016-037602', offenderId: '39488', firstName: 'Shyla', lastName: 'Mcknight',  },
	{ inmateId: 38753, bookingId: '2016-037742', offenderId: '39668', firstName: 'Camren', lastName: 'Gibbs',  },
	{ inmateId: 38633, bookingId: '2016-037622', offenderId: '39528', firstName: 'Isiah', lastName: 'Burton',  },
	{ inmateId: 38713, bookingId: '2016-037702', offenderId: '23527', firstName: 'Makhi', lastName: 'Richard',  },
	{ inmateId: 38733, bookingId: '2016-037722', offenderId: '39628', firstName: 'Bryant', lastName: 'Glass',  },
	{ inmateId: 38457, bookingId: '2016-037446', offenderId: '39322', firstName: 'Gilbert', lastName: 'Reilly',  },
	{ inmateId: 38460, bookingId: '2016-037449', offenderId: '39325', firstName: 'Dylan', lastName: 'Butler',  },
	{ inmateId: 38473, bookingId: '2016-037462', offenderId: '39328', firstName: 'Maximilian', lastName: 'Hampton',  },
	{ inmateId: 38495, bookingId: '2016-037484', offenderId: '1234', firstName: 'Noe', lastName: 'Rowe',  },
	{ inmateId: 38496, bookingId: '2016-037485', offenderId: '8521', firstName: 'Lizeth', lastName: 'Bartlett',  },
	{ inmateId: 38653, bookingId: '2016-037642', offenderId: '16648', firstName: 'Javon', lastName: 'Hood',  },
	{ inmateId: 38693, bookingId: '2016-037682', offenderId: '39588', firstName: 'Jasmin', lastName: 'Parrish',  },
	{ inmateId: 38513, bookingId: '2016-037502', offenderId: '39408', firstName: 'Clarence', lastName: 'Rivers',  },
	{ inmateId: 38673, bookingId: '2016-037662', offenderId: '39568', firstName: 'Madeline', lastName: 'Thomas',  },
	{ inmateId: 38674, bookingId: '2016-037663', offenderId: '39569', firstName: 'Gilberto', lastName: 'Strong',  },
	{ inmateId: 38493, bookingId: '2016-037482', offenderId: '39508', firstName: 'Jude', lastName: 'Elliott',  },
	{ inmateId: 38494, bookingId: '2016-037483', offenderId: '13851', firstName: 'Rhett', lastName: 'Mccoy',  },
	{ inmateId: 38593, bookingId: '2016-037582', offenderId: '39468', firstName: 'Demarion', lastName: 'Parks',  },
	{ inmateId: 38594, bookingId: '2016-037583', offenderId: '39469', firstName: 'Lawrence', lastName: 'Esparza',  },
	{ inmateId: 38595, bookingId: '2016-037584', offenderId: '39470', firstName: 'Ricky', lastName: 'Marsh',  },
];
