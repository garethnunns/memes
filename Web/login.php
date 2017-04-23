<?php
	require_once 'site/web.php';

	if(isset($_POST['goingto'])) $goingto = filter_var($_POST['goingto'], FILTER_SANITIZE_URL);

	try {
		$sql = "SELECT user.password, user.iduser, user.ukey, user.emailcode
				FROM user
				WHERE username = ?";

		$sth = $dbh->prepare($sql);

		$sth->execute(array($_POST['username'])); // sanitise user input

		if($sth->rowCount()==0) { // username not found
			if(isset($goingto)) header("Location: /?loginerror&goingto=".$goingto);
			else header("Location: /?loginerror");
			die("Login failed");
		}
		else {
			$user = $sth->fetch(PDO::FETCH_OBJ);

			if(password_verify($_POST['password'],$user->password)) { // password correct
				if(!empty($user->emailcode)) { // they haven't verified their email yet
					if(isset($goingto)) header("Location: /?emailerror&goingto=".$goingto);
					else header("Location: /?emailerror");
					die("Email not verified");
				}
				$_SESSION['user'] = $user->iduser;
				$_SESSION['key'] = $user->ukey;
				if(isset($goingto)) header("Location: ".$goingto);
				else header("Location: /");
			}
			else {
				if(isset($goingto)) header("Location: /?loginerror&goingto=".$goingto);
				else header("Location: /?loginerror");
				die("Login failed");
			}
		}
	}
	catch (PDOException $e) {
		if(isset($goingto)) header("Location: /?loginerror&goingto=".$goingto);
		else header("Location: /?loginerror");
		die("Login failed");
	}
?>