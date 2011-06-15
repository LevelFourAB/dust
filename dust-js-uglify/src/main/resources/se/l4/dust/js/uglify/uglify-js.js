//convienence function(src, [options]);

function uglify(orig_code, options){
  options || (options = {});

  var ast = jsp.parse(orig_code, options.strict_semicolons); // parse code and get the initial AST
  ast = pro.ast_mangle(ast, options.mangle_options); // get a new AST with mangled names
  ast = pro.ast_squeeze(ast, options.squeeze_options); // get an AST with compression optimizations
  var final_code = pro.gen_code(ast, options.gen_options); // compressed code here
  return final_code;
};

// This is needed as Array.reduce is used in process.js
// See https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/Reduce
if ( !Array.prototype.reduce ) {
  Array.prototype.reduce = function reduce(accumlator){
        var i, l = this.length, curr;
        
        if(typeof accumlator !== "function") // ES5 : "If IsCallable(callbackfn) is false, throw a TypeError exception."
          throw new TypeError("First argument is not callable");

        if((l == 0 || l === null) && (arguments.length <= 1))// == on purpose to test 0 and false.
          throw new TypeError("Array length is 0 and no second argument");
        
        if(arguments.length <= 1){
          for(i=0 ; i=l; ) // empty array
            throw new TypeError("Empty array and no second argument");
            
          curr = this[i++]; // Increase i to start searching the secondly defined element in the array
        }
        else{
          curr = arguments[1];
        }
        
        for(i = i || 0 ; i < l ; i++){
          if(i in this)
            curr = accumlator.call(undefined, curr, this[i], i, this);
        }
        
        return curr;
      };
  }