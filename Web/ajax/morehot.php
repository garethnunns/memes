<?php
	require_once '../site/web.php';
	check();

	$memes = memeHotFeed($_SESSION['key'],$_POST['page']);

	if($memes['success'] && count($memes['memes']))
		foreach ($memes['memes'] as $meme) 
			displayMeme($meme);
?>