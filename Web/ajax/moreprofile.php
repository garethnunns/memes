<?php
	require_once '../site/web.php';
	check();

	$memes = profile($_SESSION['key'],$_POST['profile'],$_POST['page']);

	if($memes['success'] && count($memes['memes']))
		foreach ($memes['memes'] as $meme) 
			displayMemeGrid($meme);
?>