<?php
	require_once 'site/web.php';

	if(isset($_POST['goingto'])) $goingto = filter_var($_POST['goingto'], FILTER_SANITIZE_URL);

	$login = login($_POST['username'],$_POST['password']);

	if($login['success']) {
		$_SESSION['user'] = $login['user'];
		$_SESSION['key'] = $login['key'];
		if(isset($goingto)) header("Location: ".$goingto);
		else header("Location: /");
		die('Logged in - go to home page');
	}
	elseif($login['email']) {
		if(isset($goingto)) header("Location: /?emailerror&goingto=".$goingto);
		else header("Location: /?emailerror");
		die("Email not verified");
	}
	else {
		if(isset($goingto)) header("Location: /?loginerror&goingto=".$goingto);
		else header("Location: /?loginerror");
		die("Login failed");
	}
?>