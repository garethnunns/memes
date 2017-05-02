<?php
	require_once 'site/web.php';

	$found = true;

	if(($user = userDetailsFromUsername($_GET['username']))===false) $found = false;

	else {
		try { // extra check for web to check that meme is associated with that username
			$sql = "SELECT meme.idmeme
					FROM meme
					WHERE meme.idmeme = ?
					AND meme.iduser = ?";

			$sth = $dbh->prepare($sql);

			$sth->execute(array($_GET['meme'],$user->iduser)); // sanitise user input

			if($sth->rowCount()==0 || $sth->rowCount() > 1) $found = false;
		}
		catch (PDOException $e) {
			$found = false;
		}

		$meme = meme($_SESSION['key'],$_GET['meme'],null,1000,false);

		$found = $meme['success'];
	}

	if($found) $title = "Post by {$meme['meme']['poster']['username']}";
	else $title = "Post not found";
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo $title . ' · ' . $sitename; ?></title>

		<?php include 'site/head.php'; ?>
	</head>

	<body>
		<?php include 'site/header.php'; ?>

		<div class="wrapper">
<?php
	if($found)
		displayMeme($meme['meme']);
	else
		echo $pageError;
?>
		</div>

		<?php include 'site/footer.php'; ?>
	</body>
</html>