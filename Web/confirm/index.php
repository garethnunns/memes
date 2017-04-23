<?php
	require_once '../site/web.php';

	if(loggedIn()) // user already logged in
		header("Location: /");

	if(isset($_GET['id']) && isset($_GET['code'])) {
		try {
			$sql = "SELECT user.iduser, user.ukey
					FROM user
					WHERE iduser = ?
					AND emailcode = ?";

			$sth = $dbh->prepare($sql);

			$sth->execute(array($_GET['id'],$_GET['code'])); // sanitise user input

			if($sth->rowCount()==1) {
				$user = $sth->fetch(PDO::FETCH_OBJ);

				$dbh->exec("UPDATE user SET emailcode = null WHERE iduser = {$user->iduser}");

				// log them in
				$_SESSION['user'] = $user->iduser;
				$_SESSION['key'] = $user->ukey;

				// follow the first account on the system so their feed isn't empty
				follow($user->ukey,1);

				// send them to their feed
				header("Location: /?new");
				die("You're logged in, go to the home page");
			}
		}
		catch (PDOException $e) {
			// it will just run into the page
		}


		$_SESSION['user'] = $dbh->lastInsertId();
	}

	$title = isset($_GET['new']) ? 'Thanks' : 'Confirm your account';
?>
<!DOCTYPE html>
<html>
	<head>
		<title><?php echo $title . ' · ' .$sitename; ?></title>

		<?php include '../site/head.php'; ?>
	</head>
	<body>
		<?php include '../site/header.php'; ?>
		
		<div class="wrapper center">
<?php 
	if(isset($_GET['new'])) {
?>
			<h1>Thanks!</h1>

			<h2>The memes are just moments away!</h2>

			<p>We just sent you an email, just follow the confirmation link in it and enjoy</p>

			<h3><i>Can't see the email?</i></h3>

			<p>Dang. Well it might be in your spam/junk folder, so have a look in there.<br>
			If not, then it might be our bad - <a href="/contact">contact us</a> and we'll try sort it out</p>
<?php
	}
	elseif(isset($_GET['id']) && isset($_GET['code'])) {
?>
			<h1>Whoops</h1>

			<h2>Something's gone wrong</h2>

			<p>You're seeing this page because we didn't recognise the link you used - <strong>make sure it's definitely what was written in the email</strong></p>

			<p>If you think we've made a mistake <a href="/contact">contact us</a> and we'll try sort it out</p>
<?php
	}
	else {
?>
			<h1>Confirm your account</h1>

			<h2>This is where we double check your email</h2>

			<p>If you're here from the email we sent, make sure you've copied all of the link (it looks like you missed a bit)</p>

			<p>If not, why not <a href="/">pop back to the home page</a></p>
<?php
	}
?>
		</div>
	</body>
</html>