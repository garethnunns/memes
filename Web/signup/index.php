<?php
	require_once '../site/web.php';
	require_once '../site/PHPMailer/PHPMailerAutoload.php';

	if(loggedIn()) // user already logged in
		header("Location: /");

	if(isset($_POST['signup'])) { // adding a user
		// initalise an empty errors array that could crop up
		$errors = array();

		// verify captcha
		$post_data = http_build_query(
			array(
				'secret' => CAPTCHA_SECRET,
				'response' => $_POST['g-recaptcha-response'],
				'remoteip' => $_SERVER['REMOTE_ADDR']
			)
		);

		$opts = array('http' =>
			array(
				'method'  => 'POST',
				'header'  => 'Content-type: application/x-www-form-urlencoded',
				'content' => $post_data
			)
		);

		$context  = stream_context_create($opts);
		$response = file_get_contents('https://www.google.com/recaptcha/api/siteverify', false, $context);
		$result = json_decode($response);

		if (!$result->success) 
			$errors['recaptcha'] = "There was an error with the reCAPTCHA, have another go&hellip;";

		$fields = array( // simple array of all of the inputs and what fields they are for
			'user.firstName' => $_POST['firstName'],
			'user.surname' => $_POST['surname'],
			'user.email' => $_POST['email'],
			'user.username' => $_POST['username'],
			'user.password' => $_POST['password1']
		);

		// check they are valid string inputs in terms of length
		foreach ($fields as $field => $text)
			if(valid($field,$text) !== true)
				$errors[$field] .= " ".valid($field,$text);

		// check passwords are the same
		if($_POST['password1'] != $_POST['password2'])
			$errors['user.password'] .= " The passwords don't match.";


		if(!$errors) { // we've made it through all the checks and we want to add to the database

			// generate unique key that isn't in the db yet
			do {
				$notUnique = true;

				$key = bin2hex(openssl_random_pseudo_bytes(125));

				try {
					$sql = "SELECT COUNT(iduser) FROM user WHERE ukey = ?";

					$sth = $dbh->prepare($sql);

					$sth->execute(array($key));

					if($sth->fetchColumn()==0) 
						$notUnique = false;
				}
				catch (PDOException $e) {
					$dbError = $e;
				}
			} while ($notUnique);

			// we've got everything we need so we'll add them to the database
			try {
				$emailcode = bin2hex(openssl_random_pseudo_bytes(20));

				$sth = $dbh->prepare("INSERT INTO user (ukey, username, password, email, firstName, surname, picUri, emailcode) 
					VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

				$sth->execute(array(
					$key,
					$_POST['username'],
					password_hash($_POST['password1'],PASSWORD_DEFAULT),
					$_POST['email'],
					$_POST['firstName'],
					$_POST['surname'],
					$defaultPics[mt_rand(0, count($defaultPics) - 1)],
					$emailcode
				));

				// confirmation email
				$name = $_POST['firstName'] . ' ' . $_POST['surname'];
				$email = $_POST['email'];
				$id = $dbh->lastInsertId();
				$link = $web."confirm/?id={$id}&code={$emailcode}";

				$html = "
<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">
<html>
	<head>
		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">
		<title>Welcome to $sitename</title>
	</head>
	<body>
		<div style=\"width: 640px; font-family: 'Open Sans', Avenir, Helvetica, sans-serif; font-size: 14px;\">
			<div style=\"text-align: center\">
				<h1>Welcome to $sitename</h1>
				<img src=\"{$res}email/confirm.jpg\" height=\"360\" width=\"640\" alt=\"This is a pretty funny meme that you're missing out on\">
			</div>

			<p>Hey $name,</p>

			<p>Thank you for signing up to $sitename. Your account ({$_POST['username']}) is almost created, just one last thing&hellip;</p>

			<p><b>Finish setting up your account by going to this link:<br>
			<a href=\"{$link}\">{$link}</a></b></p>

			<p>See you soon on {$sitename},<br>
			The {$sitename} team</p>

			<p><i>Didn't sign up for an account and not expecting this email? You might as well set up an account&hellip;
			Or just ignore this message</i></p>
		</div>
	</body>
</html>
";

$text = "
Welcome to $sitename

Hey $name,

Thank you for signing up to $sitename. Your account ({$_POST['username']}) is almost created, just one last thing...

Finish setting up your account by going to this link:
{$link}

See you soon on {$sitename},
The {$sitename} team

Didn't sign up for an account and not expecting this email? You might as well set up an account...
Or just ignore this message";

				// send the user an email to verify their email address

				$mail = new PHPMailer; // to do this we're using the PHPMailer lib
				$mail->isSMTP(); // set it to SMTP
				$mail->SMTPDebug = 0; // no debugging (change to 2 if needed)
				$mail->Debugoutput = 'html'; // to make debugging easier
				// there are login details for a free GMail account in secure.php
				// this makes it easiest to avoid spam
				// so we'll be using those details and sending it through the google server
				$mail->Host = 'smtp.gmail.com'; // hostname of the mail server
				$mail->Port = 587; // SMTP port number - 587 for authenticated TLS
				$mail->SMTPSecure = 'tls'; // the encryption to be used
				$mail->SMTPAuth = true; // whether to use SMTP authentication
				$mail->Username = GOOGLE_EMAIL; // Username to use for SMTP authentication - full email address for gmail
				$mail->Password = GOOGLE_PASSWORD; // Password to use for SMTP authentication
				$mail->setFrom(GOOGLE_EMAIL, $sitename); // who the message is sent from
				$mail->addAddress($email, $name); // Set who the message is to be sent to

				// the email
				$mail->Subject = "Welcome to $sitename";
				$mail->msgHTML($html);
				$mail->AltBody = $text;

				if ($mail->send()) { // send the email
					header("Location: /confirm?new");
				}
				else {
					$errors['mail'] = "There was an error sending your confirmation email, this was probably because the email address you enterred was invalid, but it may have been an internal error - either way we've reserved your username - <a href='/contact'>contact us if you think there's a mistake</a>";
				}
			}
			catch (PDOException $e) {
				$errors['dbError'] = "There was an problem adding you to the database, have another go&hellip;";
			}
		}
	}

?><!DOCTYPE html>
<html>
	<head>
		<title>Signup · <?php echo $sitename; ?></title>

		<script src='https://www.google.com/recaptcha/api.js?onload=onloadCallback&render=explicit' async defer></script>

		<?php include '../site/head.php'; ?>

		<script type="text/javascript">
			var onSubmit = function(token) {
				$("#signup").submit();
			};

			var onloadCallback = function() {
				grecaptcha.render('signupbtn', {
				  'sitekey' : '<?php echo CAPTCHA_KEY; // defined in secure.php ?>',
				  'badge' : 'bottomleft',
				  'callback' : onSubmit
				});
			};
		</script>
	</head>

	<body>
		<?php include '../site/header.php'; ?>

		<div class="wrapper">
			<form id="signup" method="POST">
				<table>
					<tr>
						<td colspan="2">
							<h1>Welcome to <?php echo $sitename; ?></h1>

							<p>We only collect the essential information here</p>

							<?php 
								if(!empty($errors['dbError'])) echo "<p class='error'>{$errors['dbError']}</p>";
								if(!empty($errors['mail'])) echo "<p class='error'>{$errors['mail']}</p>";
							?>
						</td>
					</tr>
					<tr>
						<td>First Name</td>
						<td>
							<input type="text" name="firstName" placeholder="First name" minlength="1" maxlength="20" value="<?php if(isset($_POST['firstName'])) echo $_POST['firstName'] ?>" pattern="^[a-zA-Z\s'\.-]{1,20}$" title="The first name can only contain letters, spaces, apostrophes, hyphens and full stops." required>
							<?php if(isset($errors['user.firstName'])) echo "<p class='error'>{$errors['user.firstName']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Surname</td>
						<td>
							<input type="text" name="surname" placeholder="Surname" minlength="1" maxlength="20" value="<?php if(isset($_POST['surname'])) echo $_POST['surname'] ?>" pattern="^[a-zA-Z\s'\.-]{1,20}$" title="The surname can only contain letters, spaces, apostrophes, hyphens and full stops." required>
							<?php if(isset($errors['user.surname'])) echo "<p class='error'>{$errors['user.surname']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Email</td>
						<td>
							<input type="email" name="email" placeholder="Email address" maxlength="100" value="<?php if(isset($_POST['email'])) echo $_POST['email'] ?>" required>
							<?php if(isset($errors['user.email'])) echo "<p class='error'>{$errors['user.email']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Username</td>
						<td>
							<input type="text" name="username" placeholder="Pick a username" minlength="3" maxlength="20" value="<?php if(isset($_POST['username'])) echo $_POST['username'] ?>" pattern="^([_|-]?[a-zA-Z0-9][_|-]?){0,20}" title="The username can only contain alphanumeric characters (letters and numbers), as well as underscores and dashes (_ or -). You can't have two underscores or dashes next to each other." required>
							<?php if(isset($errors['user.username'])) echo "<p class='error'>{$errors['user.username']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Password</td>
						<td>
							<input type="password" name="password1" placeholder="Password" minlength="8" maxlength="50" pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9]{8,50}$" title="The password must contain a lowercase letter, an uppercase letter and a number" required>
							<?php if(isset($errors['user.password'])) echo "<p class='error'>{$errors['user.password']}</p>" ?>
						</td>
					</tr>
					<tr>
						<td>Confirm Password</td>
						<td>
							<input type="password" name="password2" placeholder="Confirm password" minlength="8" maxlength="50" required>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<input type="hidden" name="signup">
							<?php
								if(isset($errors['recaptcha'])) echo "<p class='error'>{$errors['recaptcha']}</p>";
							?>
							<p><input type="submit" id="signupbtn" value="Join now"></p>

							<p><a href="/">Already have an account?</a></p>
						</td>
					</tr>
				</table>
			</form>
		</div>
	</body>
</html>