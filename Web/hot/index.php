<?php
	require_once '../site/web.php';
	check();
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo 'Hot · ' . $sitename; ?></title>

		<?php include '../site/head.php'; ?>
	</head>

	<body>
		<?php include '../site/header.php'; ?>

		<div class="wrapper">
			<h1><span class="icon-hot"></span> hot memes</h1>
<?php
	$memes = memeHotFeed($_SESSION['key'],0,400);

	if(!$memes['success']) // something went wrong
		echo "<p class='error'>".(isset($memes['error']) ? $memes['error'] : "There was an error fetching the memes")."</p>";
	elseif(!count($memes['memes']))
		echo "<p><i>There are no memes to show at the moment, why not <a href='/add'>post a meme</a>?</p>";
	else 
		foreach ($memes['memes'] as $meme) 
			displayMeme($meme);
?>
		</div>

		<?php include '../site/footer.php'; ?>
	</body>
</html>