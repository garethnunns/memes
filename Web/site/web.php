<?php
	// these are the functions that are only used on the website
	// they build upon the core functions:
	require_once dirname(__FILE__).'/functions.php';

	function check() {
		// checks the user is logged in
		// redirects them to the home page if not

		if(!loggedIn()) { // user not logged in
			header("Location: /?goingto=".filter_var($_SERVER['REQUEST_URI'], FILTER_SANITIZE_URL));
			die('Please login'); //stop the rest of the script executing
		}
	}

	function loggedIn() {
		// returns bool - true if logged in

		if(!isset($_SESSION['user']) || !isset($_SESSION['key']))
			return false;
		if(($user = userDetails($_SESSION['key'])) === false) { // couldn't find user with that ket
			session_destroy();
			die('The account you are logged in on no longer exists');
		}
		return true;
	}

	function displayMeme($meme) {
		// expects a $meme like those in the $memes array produced by a function like memeFeed()

		echo "
		<img src='{$meme['poster']['pic']}' alt='{$meme['poster']['username']} profile picture' />
		<h3>".(($meme['original']) ? 'Reposted by' : '')."
			<a href='{$web}{$meme['poster']['username']}' title='{$meme['poster']['name']}'>
				{$meme['poster']['username']}
			</a>
		</h3>
		<p>".($meme['original'] ? "Original by {$meme['original']['username']}" : "Posted by {$meme['poster']['name']}")."</p>
		<p><i>{$meme['time']['ago']}</i></p>
		<img src='{$meme['images']['full']}' alt='Meme ".($meme['original'] ? 'reposted' : 'posted')." by {$meme['poster']['username']}'>
		<p><b>".htmlspecialchars($meme['caption'])."</b></p>
		<p>{$meme['comments-num']} comments</p>
		<p>{$meme['stars-num']} stars</p>
		<p>{$meme['reposts-num']} reposts</p>";
		if($meme['comments-num'] && $meme['comments']) 
			foreach($meme['comments'] as $comment)
				echo "
				<img src='{$comment['commenter']['pic']}' alt='{$meme['poster']['username']} profile picture' />
				<h4><a href='{$web}{$comment['commenter']['username']}' title='{$comment['commenter']['name']}'>
					{$comment['commenter']['username']}
				</a></h4>
				<p><i>{$comment['time']['ago']}</i></p>
				<p>".htmlspecialchars($comment['comment'])."</p>";
		echo "<hr>";
	}
?>