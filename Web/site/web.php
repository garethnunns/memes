<?php
	// these are the functions that are only used on the website
	// they build upon the core functions:
	require_once dirname(__FILE__).'/functions.php';

	function check() {
		// checks the user is logged in
		// redirects them to the home page if not

		if(!loggedIn()) { // user not logged in
			header("Location: /?goingto=".filter_var($_SERVER['REQUEST_URI'], FILTER_SANITIZE_URL));
			die('Please login'); //stop the rest of the script executing
		}
	}

	function loggedIn() {
		// returns bool - true if logged in

		if(!isset($_SESSION['user']) || !isset($_SESSION['key']))
			return false;
		if(($user = userDetails($_SESSION['key'])) === false) { // couldn't find user with that ket
			session_destroy();
			die('The account you are logged in on no longer exists');
		}
		return true;
	}
?>