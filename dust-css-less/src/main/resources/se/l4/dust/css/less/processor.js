var compileResource = function(css) {
	// TODO: Ability to use LESS imports
	
	var result;
	var parser = new window.less.Parser({ optimization: 3 });
	parser.parse(css, function(ex, root) {
		if(ex instanceof Object) throw ex;
		result = root.toCSS();
	});
	
	return result;
}
