window.less.Parser.importer = function(path, paths, callback, env) {
	var data = importer.read(path) + ""; // "" is to force JS conversion
	new window.less.Parser({
		optimization: development ? 0 : 3,
	}).parse(data, function(ex, root) {
		if(ex instanceof Object) throw e;
		
		callback(root, path, { local: false });
	});
};

// Actual compile function
var compileResource = function(css) {
	// TODO: Ability to use LESS imports
	
	var result;
	var parser = new window.less.Parser({ optimization: development ? 0 : 3 });
	parser.parse(css, function(ex, root) {
		if(ex instanceof Object) throw ex;
		result = root.toCSS();
	});
	
	return result;
}
