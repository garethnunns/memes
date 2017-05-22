<?php
	require_once 'site/web.php';
	if(!loggedIn()) $_SESSION['key'] = 'public';

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

		if($found) {
			$meme = meme($_SESSION['key'],$_GET['meme'],null,1000,false);
			$found = $meme['success'];
		}
	}

	if($found) $title = "Post by {$meme['meme']['poster']['username']} · $sitename";
	else $title = "Post not found · $sitename";
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo $title ?></title>

		<?php
			include 'site/head.php';
			if($found) {
				$link = $meme['meme']['link'];
				$image = $meme['meme']['images']['full'];
				if(empty($meme['meme']['caption']))
					$desc = possessive($meme['meme']['poster']['firstName'])." post on {$sitename} - {$tagline}";
				else
					$desc = possessive($meme['meme']['poster']['firstName'])." post on {$sitename} - &quot;".htmlspecialchars($meme['meme']['caption'])."&quot;";

				echo "
				<meta property=\"og:url\" content=\"{$link}\">
				<meta property=\"og:title\" content=\"{$title}\">
				<meta property=\"og:description\" content=\"{$desc}\">
				<meta property=\"og:image\" content=\"{$image}\">

				<meta property=\"twitter:title\" content=\"{$title}\">
				<meta property=\"twitter:description\" content=\"{$desc}\">
				<meta property=\"twitter:image\" content=\"{$image}\">

				<link rel=\"canonical\" href=\"{$link}\"/>
				<meta name=\"description\" content=\"{$desc}\">";
			}
		?>
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