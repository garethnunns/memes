<?php
	require_once 'site/functions.php';

	try {
		$sql = "SELECT user.password, user.iduser, user.ukey
				FROM user
				WHERE username = ?";

		$sth = $dbh->prepare($sql);

		$sth->execute(array($_POST['username'])); // sanitise user input

		if($sth->rowCount()==0) { // username not found
			if(isset($_POST['goingto'])) header("Location: /?loginerror&goingto=".filter_var($_POST['goingto'], FILTER_SANITIZE_URL));
			else header("Location: /?loginerror");
			die("Login failed");
		}
		else {
			$user = $sth->fetch(PDO::FETCH_OBJ);

			if(password_verify($_POST['password'],$user->password)) { // password correct
				$_SESSION['user'] = $user->iduser;
				$_SESSION['key'] = $user->ukey;
				if(isset($_POST['goingto'])) header("Location: ".filter_var($_POST['goingto'], FILTER_SANITIZE_URL));
				else header("Location: /");
			}
			else {
				if(isset($_POST['goingto'])) header("Location: /?loginerror&goingto=".filter_var($_POST['goingto'], FILTER_SANITIZE_URL));
				else header("Location: /?loginerror");
				die("Login failed");
			}
		}
	}
	catch (PDOException $e) {
		if(isset($_POST['goingto'])) header("Location: /?loginerror&goingto=".filter_var($_POST['goingto'], FILTER_SANITIZE_URL));
		else header("Location: /?loginerror");
		die("Login failed");
	}
?>