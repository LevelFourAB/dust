// Define a few things that LESS assumes exist

// Window definition, fake location and put into production mode
var window = {
	location: {
		protocol: 'http'
	},
	
	less: {
		// Always use in production mode as otherwise a timer is used
		env: 'production'
	}
};

// Fake the location
var location = window.location;

// Fake getElementByTagName
var document = {
	getElementsByTagName: function() { return []; }
};