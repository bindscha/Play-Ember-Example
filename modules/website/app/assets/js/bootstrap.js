/**
 * Bootstrap-related javascript
 */

jQuery(document).ready(function() {
    
	// Tooltip
	$("a[rel=tooltip]").tooltip()
	
	// Popover
	$("a[rel=popover]").popover().click(function(e) {
		e.preventDefault()
	})
	
	// Setup drop down menu
	$('.dropdown-toggle').dropdown();
	 
	// Fix input element click problem
	$('.dropdown input, .dropdown label').click(function(e) {
	  e.stopPropagation();
	});
    
});
